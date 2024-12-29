package uk.ac.tees.mad.galleryview.presentation.clickpicture

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Looper
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

class ClickPictureViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // Function to set up the camera
    fun startCamera(
        context: Context,
        imageCapture: ImageCapture,
        lifecycleOwner: LifecycleOwner
    ) = viewModelScope.launch(Dispatchers.Main) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val cameraProvider = withContext(Dispatchers.IO) {
            cameraProviderFuture.get()
        }
        val previewView = PreviewView(context)

        val preview = androidx.camera.core.Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
        } catch (exc: Exception) {
            // Handle exception
        }
    }

    // Function to capture a photo and store it in the cloud backend
    fun capturePhoto(
        imageCapture: ImageCapture,
        outputDirectory: File,
        onPhotoCaptured: (Uri) -> Unit,
        onError: (ImageCaptureException) -> Unit
    ) {
        val file = File(
            outputDirectory,
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

        imageCapture.takePicture(
            outputOptions,
            Dispatchers.IO.asExecutor(),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    onError(exc)
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(file)
                    onPhotoCaptured(savedUri)
                    uploadImageToFirebase(savedUri)
                }
            }
        )
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val storageRef = storage.reference.child("images/${UUID.randomUUID()}.jpg")
        storageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    saveImageInfoToFirestore(downloadUrl.toString())
                }
            }
            .addOnFailureListener {
                // Handle any errors
            }
    }

    private fun saveImageInfoToFirestore(imageUrl: String) {
        val metadata = hashMapOf(
            "imageUrl" to imageUrl,
            "timestamp" to System.currentTimeMillis(),
            "tags" to emptyList<String>(),
            "description" to ""
        )
        firestore.collection("images")
            .add(metadata)
            .addOnSuccessListener {
                // Handle success
            }
            .addOnFailureListener {
                // Handle failure
            }
    }

    // Fetch the device's current location
    @SuppressLint("MissingPermission")
    fun fetchLocation(context: Context, onLocationFetched: (String) -> Unit) {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
            .setWaitForAccurateLocation(true)
            .setMinUpdateIntervalMillis(5000L)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                val locationString = "${location?.latitude}, ${location?.longitude}"
                onLocationFetched(locationString)
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }
}
