package uk.ac.tees.mad.galleryview.presentation.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import uk.ac.tees.mad.galleryview.R
import uk.ac.tees.mad.galleryview.Screen
import uk.ac.tees.mad.galleryview.presentation.auth.AuthState
import uk.ac.tees.mad.galleryview.presentation.galleryview.ImageData
import uk.ac.tees.mad.galleryview.presentation.galleryview.ImageItem
import uk.ac.tees.mad.galleryview.ui.theme.ErrorColor
import uk.ac.tees.mad.galleryview.ui.theme.PrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val userState by viewModel.userState.collectAsState()
    val snackbarHostState = remember {
        SnackbarHostState()
    }
    val savedPhotos by viewModel.photos.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.fetchUserData()
        viewModel.doDatabaseOperation(context, getAllPhoto = true)
    }
    Scaffold(snackbarHost = {
        SnackbarHost(snackbarHostState)
    },
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.signOut()
                            navController.navigate(Screen.AuthScreen.route) {
                                popUpTo(0)
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = ErrorColor
                        ),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Text("Logout")
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.navigateUp()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
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
                        ProfileContent(
                            name = user.name,
                            email = user.email,
                            profilePictureUrl = user.profilePictureUrl,
                            navController = navController,
                            photos = savedPhotos,
                            viewModel = viewModel
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
fun ProfileContent(
    name: String,
    email: String,
    profilePictureUrl: String?,
    navController: NavController,
    photos: List<ImageData>,
    viewModel: ProfileViewModel,

    ) {
    val context = LocalContext.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        val imagePainter = if (profilePictureUrl.isNullOrEmpty()) {
            painterResource(id = R.drawable.placeholder)
        } else {
            rememberAsyncImagePainter(model = profilePictureUrl)
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Image(
                painter = imagePainter,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentScale = ContentScale.Crop
            )
            Column(horizontalAlignment = Alignment.End) {

                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.End
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        OutlinedButton(
            onClick = { navController.navigate(Screen.EditProfileScreen.route) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = PrimaryColor
            ),
        ) {
            Text("Edit Profile")
        }
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Saved images",
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (photos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize()) {

                Text(
                    text = "No saved images",
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    color = Color.Gray
                )
            }

        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(photos) { photo ->
                val bitmap = BitmapFactory.decodeFile(photo.imageUrl)?.asImageBitmap()
                bitmap?.let {
                    SavedImageItem(
                        image = it,
                        onDelete = {
                            viewModel.doDatabaseOperation(
                                context = context,
                                getAllPhoto = false,
                                photo = photo
                            )
                        }
                    ) {
                        navController.currentBackStackEntry?.savedStateHandle?.set(
                            "photo_detail",
                            photo
                        )
                        navController.navigate(Screen.PhotoDetailViewScreen.route)
                    }
                }
            }
        }
    }
}

@Composable
fun SavedImageItem(image: ImageBitmap, onDelete: () -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .zIndex(10f)
                    .padding(8.dp)
                    .clickable { onDelete() },
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier
                        .background(Color.White, CircleShape),
                    tint = Color.Black
                )
            }

            Image(
                bitmap = image,
                contentDescription = "Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}
