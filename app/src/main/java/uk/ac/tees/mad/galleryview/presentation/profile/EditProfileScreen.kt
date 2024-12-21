package uk.ac.tees.mad.galleryview.presentation.profile

import android.app.Activity
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.github.drjacky.imagepicker.ImagePicker
import com.github.drjacky.imagepicker.constant.ImageProvider
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import uk.ac.tees.mad.galleryview.R
import uk.ac.tees.mad.galleryview.presentation.auth.AuthState
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val userState by viewModel.userState.collectAsState()
    var name by remember { mutableStateOf(TextFieldValue("")) }
    var profilePictureUrl by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val storage = FirebaseStorage.getInstance()
    val snackbarHostState = remember {
        SnackbarHostState()
    }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            uri?.let {
                imageUri = it
                uploadImageToFirebaseStorage(
                    uri = it,
                    storage = storage,
                    onFailure = {

                    }
                ) { downloadUrl ->
                    profilePictureUrl = downloadUrl
                }
            }
        }
    }
    val intent = ImagePicker.with(context as ComponentActivity)
        .provider(ImageProvider.BOTH) // Both Camera and Gallery
        .createIntent()

    val permissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    ) {
        if (it) {
            imagePickerLauncher.launch(intent)
        }
    }


    Scaffold( snackbarHost = {
        SnackbarHost(snackbarHostState)
    },
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { padding ->
            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (userState) {
                    is UserState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    is UserState.Success -> {
                        val user = userState as UserState.Success
                        LaunchedEffect(Unit) {
                            name = TextFieldValue(user.name)
                            profilePictureUrl = user.profilePictureUrl
                        }
                        EditProfileContent(
                            name = name,
                            profilePictureUrl = profilePictureUrl,
                            onNameChange = { name = it },
                            onPickImageClick = {
                                if (permissionState.status.isGranted) {
                                    imagePickerLauncher.launch(intent)
                                } else {
                                    permissionState.launchPermissionRequest()
                                }

                            },
                            onSaveClick = {
                                coroutineScope.launch {
                                    viewModel.updateUserData(name.text, profilePictureUrl)
                                    navController.popBackStack()
                                }
                            }
                        )
                    }

                    is UserState.Error -> {
                        LaunchedEffect(Unit) {
                            snackbarHostState.showSnackbar(
                                (userState as UserState.Error).message,
                                actionLabel = "Dismiss"
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun EditProfileContent(
    name: TextFieldValue,
    profilePictureUrl: String,
    onNameChange: (TextFieldValue) -> Unit,
    onPickImageClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Image(
            painter = if (profilePictureUrl.isEmpty()) {
                painterResource(id = R.drawable.placeholder)
            } else {
                rememberImagePainter(data = profilePictureUrl)
            },
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .clickable { onPickImageClick() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Name") },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onSaveClick,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
        ) {
            Text(
                "Save Changes",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
            )
        }
    }
}

private fun uploadImageToFirebaseStorage(
    uri: Uri,
    storage: FirebaseStorage,
    onFailure: (String) -> Unit,
    onSuccess: (downloadUrl: String) -> Unit
) {
    val storageReference: StorageReference = storage.reference
        .child("profilePictures/${UUID.randomUUID()}.jpg")
    val uploadTask = storageReference.putFile(uri)
    uploadTask.addOnSuccessListener {
        storageReference.downloadUrl.addOnSuccessListener { downloadUrl ->
            onSuccess(downloadUrl.toString())
        }
    }.addOnFailureListener { ex ->
        onFailure(ex.message ?: "Error uploading image")
    }
}
