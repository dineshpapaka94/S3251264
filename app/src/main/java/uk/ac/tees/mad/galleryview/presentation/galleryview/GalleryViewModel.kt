package uk.ac.tees.mad.galleryview.presentation.galleryview

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class ImageData(
    val id: String,
    val imageUrl: String,
    val tags: List<String>,
    val description: String
)

class GalleryViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    val images = mutableStateListOf<ImageData>()

    fun fetchUserImages() {
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
                        description = description
                    )
                }
                images.clear()
                images.addAll(imageList)
            }
            .addOnFailureListener {
                // Handle failure
            }
    }
}