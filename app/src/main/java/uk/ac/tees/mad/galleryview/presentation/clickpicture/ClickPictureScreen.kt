package uk.ac.tees.mad.galleryview.presentation.clickpicture

import android.content.Context
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import uk.ac.tees.mad.galleryview.R
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ClickPictureScreen(
    viewModel: ClickPictureViewModel = viewModel(),
    navController: NavController
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    val outputDirectory = remember { getOutputDirectory(context) }
    var imageCapture by remember { mutableStateOf(ImageCapture.Builder().build()) }
    var capturedUri by remember { mutableStateOf<Uri?>(null) }
    var isMetadataDialogOpen by remember { mutableStateOf(false) }
    var tags by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    val previewView = PreviewView(context).apply {
        scaleType = PreviewView.ScaleType.FILL_CENTER
    }
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    val locationPermissionState =
        rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)

    LaunchedEffect(cameraPermissionState) {
        if (cameraPermissionState.status.isGranted) {
            viewModel.startCamera(context, imageCapture, lifecycleOwner, previewView)
        } else {
            cameraPermissionState.launchPermissionRequest()
        }
    }
    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (cameraPermissionState.status.isGranted) {
            viewModel.fetchLocation(context) {
                location = it
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Camera Preview
        AndroidView(
            factory = { context ->

                viewModel.startCamera(context, imageCapture, lifecycleOwner, previewView)
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Capture Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            IconButton(
                onClick = {
                    viewModel.capturePhoto(
                        imageCapture,
                        outputDirectory,
                        onPhotoCaptured = { uri ->
                            capturedUri = uri
                        },
                        onError = {
                            it.printStackTrace()
                        }
                    )
                },
                modifier = Modifier
                    .size(60.dp)
                    .background(Color.White, CircleShape)
                    .padding(16.dp)
            ) {
                Icon(
                    Icons.Default.Camera,
                    contentDescription = "Capture",
                    tint = Color.Black,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        // Show captured image preview
        capturedUri?.let { uri ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(0.7f)),
                verticalArrangement = Arrangement.Center,
            ) {

                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = "Captured Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        },
                        modifier = Modifier
                            .background(Color.White, CircleShape)
                            .size(60.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "clode",
                            tint = Color.Black,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            isMetadataDialogOpen = true
                        },
                        modifier = Modifier
                            .background(Color.White, CircleShape)
                            .size(60.dp)
                    ) {
                        Icon(
                            Icons.Default.EditNote,
                            contentDescription = "add metadata",
                            tint = Color.Black,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                }
            }
        }
    }

    // Metadata Dialog
    if (isMetadataDialogOpen) {
        AlertDialog(
            onDismissRequest = { isMetadataDialogOpen = false },
            title = { Text("Add Metadata") },
            text = {
                Column {
                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Tags") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (locationPermissionState.status.isGranted) {
                                        viewModel.fetchLocation(context) { fetchedLocation ->
                                            location = fetchedLocation
                                        }
                                    } else {
                                        locationPermissionState.launchPermissionRequest()
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = "location"
                                )
                            }
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        capturedUri?.let { uri ->
                            viewModel.uploadImageToFirebase(uri, tags, description, location)
                            isMetadataDialogOpen = false
                            navController.popBackStack()
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { isMetadataDialogOpen = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
}

private fun getOutputDirectory(context: Context): File {
    val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
        File(it, context.resources.getString(R.string.app_name)).apply { mkdirs() }
    }
    return if (mediaDir != null && mediaDir.exists()) mediaDir else context.filesDir
}
