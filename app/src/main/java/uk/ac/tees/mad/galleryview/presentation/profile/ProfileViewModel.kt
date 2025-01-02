package uk.ac.tees.mad.galleryview.presentation.profile

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import uk.ac.tees.mad.galleryview.data.AppDatabase
import uk.ac.tees.mad.galleryview.data.PhotoDao
import uk.ac.tees.mad.galleryview.data.toImageData
import uk.ac.tees.mad.galleryview.data.toPhotoEntity
import uk.ac.tees.mad.galleryview.presentation.galleryview.ImageData

class ProfileViewModel : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val userId = firebaseAuth.currentUser?.uid

    private val _userState = MutableStateFlow<UserState>(UserState.Loading)
    val userState: StateFlow<UserState> = _userState

    init {
        fetchUserData()
    }


    fun doDatabaseOperation(context: Context, getAllPhoto: Boolean, photo: ImageData? = null) {
        val database: AppDatabase? = AppDatabase.getInstance(context).also {
            if (it == null) {
                Log.e("DLS", "Database initialization failed, database is null.")
            }
        }

        val photoDao: PhotoDao? = database?.getDao().also {
            if (it == null) {
                Log.e("DLS", "DAO initialization failed, photoDao is null.")
            }
        }
        if (getAllPhoto) {
            viewModelScope.launch {
                try {
                    photoDao?.let { dao ->
                        dao.getAllPhotos()
                            .flowOn(Dispatchers.IO)
                            .collect { photosList ->
                                _photos.value = photosList.map { it.toImageData() }
                            }
                        Log.d("DLS", "Fetching photos successful.")
                    } ?: Log.e("DLS", "PhotoDao is null, cannot fetch photos")
                } catch (e: Exception) {
                    Log.e("DLS", "Error fetching photos", e)
                }
            }
        } else {
            photo?.let {
                viewModelScope.launch(Dispatchers.IO) {
                    photoDao?.deletePhoto(it.toPhotoEntity())
                }
            }
        }
    }

    private val _photos = MutableStateFlow<List<ImageData>>(listOf())
    val photos = _photos.asStateFlow()

    fun fetchUserData() {
        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val name = document.getString("name") ?: ""
                        val email = document.getString("email") ?: ""
                        val profilePictureUrl = document.getString("profilePictureUrl") ?: ""
                        _userState.value = UserState.Success(name, email, profilePictureUrl)
                    } else {
                        _userState.value = UserState.Error("No user data found")
                    }
                }
                .addOnFailureListener {
                    _userState.value = UserState.Error(it.message ?: "Error fetching user data")
                }
        } else {
            _userState.value = UserState.Error("User not logged in")
        }
    }

    fun updateUserData(name: String, profilePictureUrl: String) {
        if (userId != null) {
            val userUpdates = mapOf(
                "name" to name,
                "profilePictureUrl" to profilePictureUrl
            )
            firestore.collection("users").document(userId).update(userUpdates)
                .addOnSuccessListener {
                    fetchUserData() // Refresh user data after updating
                }
                .addOnFailureListener {
                    _userState.value = UserState.Error(it.message ?: "Error updating profile")
                }
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }
}

sealed class UserState {
    object Loading : UserState()
    data class Success(val name: String, val email: String, val profilePictureUrl: String) :
        UserState()

    data class Error(val message: String) : UserState()
}
