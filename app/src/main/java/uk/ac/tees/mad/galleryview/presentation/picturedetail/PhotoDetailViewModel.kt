package uk.ac.tees.mad.galleryview.presentation.picturedetail

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Environment
import androidx.lifecycle.ViewModel
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.content.Intent
import android.net.Uri
import android.util.Log
import uk.ac.tees.mad.galleryview.R
import uk.ac.tees.mad.galleryview.data.AppDatabase
import uk.ac.tees.mad.galleryview.data.PhotoDao
import uk.ac.tees.mad.galleryview.data.toPhotoEntity
import uk.ac.tees.mad.galleryview.presentation.galleryview.ImageData

class PhotoDetailViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()


    fun deleteImage(imageId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("images")
            .document(imageId)
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }


    suspend fun saveImageFromUrlLocally(
        context: Context,
        imageData: ImageData,
        fileName: String
    ): String? {
        val database = AppDatabase.getInstance(context)

        val photoDao = database.getDao()
        return withContext(Dispatchers.IO) {
            val bitmap = getBitmapFromUrl(context, imageData.imageUrl)
            if (bitmap != null) {
                val filePath = saveImageToGallery(context, bitmap, fileName)
                if (filePath != null) {
                    val image = imageData.copy(imageUrl = filePath).toPhotoEntity()
                    photoDao.insertPhoto(image)
                }
                filePath
            } else {
                null
            }
        }
    }


    fun saveImageToGallery(context: Context, bitmap: Bitmap, fileName: String): String? {
        // Create a directory in the external storage
        val imagesDir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            context.getString(R.string.app_name)
        )
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }

        // Define the file where the image will be saved
        val imageFile = File(imagesDir, "$fileName.jpg")

        // Save the bitmap to the file
        var fileOutputStream: FileOutputStream? = null
        try {
            fileOutputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.flush()
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } finally {
            try {
                fileOutputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        // Notify the media scanner about the new file so that it is immediately available to the user
        context.sendBroadcast(
            Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
                data = Uri.fromFile(imageFile)
            }
        )

        return imageFile.absolutePath
    }


}


suspend fun getBitmapFromUrl(context: Context, imageUrl: String): Bitmap? {
    val loader = ImageLoader(context)
    val request = ImageRequest.Builder(context)
        .data(imageUrl)
        .allowHardware(false) // Disable hardware bitmaps to ensure we get a software bitmap
        .build()

    return when (val result = loader.execute(request)) {
        is SuccessResult -> (result.drawable as BitmapDrawable).bitmap
        else -> null
    }
}
