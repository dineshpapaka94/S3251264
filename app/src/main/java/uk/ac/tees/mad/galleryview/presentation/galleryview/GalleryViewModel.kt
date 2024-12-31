package uk.ac.tees.mad.galleryview.presentation.galleryview

import android.os.Parcelable
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.parcelize.Parcelize

@Parcelize
data class ImageData(
    val id: String,
    val imageUrl: String,
    val tags: List<String>,
    val description: String,
    val location: String,
    val timestamp: Long
): Parcelable

class GalleryViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    val images = mutableStateListOf<ImageData>()
    val isLoading = mutableStateOf<Boolean>(true)

    fun fetchUserImages() {
        isLoading.value = true
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("images")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val imageList = querySnapshot.documents.mapNotNull { document ->
                    val imageUrl = document.getString("imageUrl") ?: return@mapNotNull null
                    val tags = document.get("tags") as? List<String> ?: emptyList()
                    val description = document.getString("description") ?: ""
                    ImageData(
                        id = document.id,
                        imageUrl = imageUrl,
                        tags = tags,
                        description = description,
                        location = document.getString("location") ?: "",
                        timestamp = document.getLong("timestamp") ?: 0L
                    )
                }
                Log.d("IMAGES", imageList.toString())
                images.clear()
                images.addAll(imageList)
                isLoading.value = false
            }
            .addOnFailureListener {
                it.printStackTrace()
                isLoading.value = false
            }
    }
}