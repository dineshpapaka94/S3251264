package uk.ac.tees.mad.galleryview.presentation.clickpicture

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Looper
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.util.UUID

class ClickPictureViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun uploadImageToCloud(file: File, description: String, tags: String, location: String) {
        val storageRef = storage.reference.child("images/${UUID.randomUUID()}.jpg")
        val fileUri = Uri.fromFile(file)
        val uploadTask = storageRef.putFile(fileUri)

        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                saveMetadataToFirestore(uri.toString(), description, tags, location)
            }
        }.addOnFailureListener {
            // Handle error
        }
    }

    private fun saveMetadataToFirestore(
        imageUrl: String,
        description: String,
        tags: String,
        location: String
    ) {
        val data = mapOf(
            "imageUrl" to imageUrl,
            "description" to description,
            "tags" to tags,
            "location" to location,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("photos")
            .add(data)
            .addOnSuccessListener {

            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }
}
