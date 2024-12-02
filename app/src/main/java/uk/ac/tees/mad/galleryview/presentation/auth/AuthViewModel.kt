package uk.ac.tees.mad.galleryview.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun signIn(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _authState.value = AuthState.Success
                    } else {
                        _authState.value = AuthState.Error(task.exception?.message ?: "Unknown error")
                    }
                }
        }
    }

    fun signUp(name: String, email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = firebaseAuth.currentUser?.uid
                        if (userId != null) {
                            val user = hashMapOf(
                                "name" to name,
                                "email" to email
                            )
                            firestore.collection("users").document(userId)
                                .set(user)
                                .addOnSuccessListener {
                                    _authState.value = AuthState.Success
                                }
                                .addOnFailureListener {
                                    _authState.value = AuthState.Error(it.message ?: "Firestore error")
                                }
                        }
                    } else {
                        _authState.value = AuthState.Error(task.exception?.message ?: "Unknown error")
                    }
                }
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
