package uk.ac.tees.mad.galleryview.presentation.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import uk.ac.tees.mad.galleryview.R
import uk.ac.tees.mad.galleryview.Screen
import uk.ac.tees.mad.galleryview.presentation.auth.AuthState
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
    Scaffold( snackbarHost = {
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
                            navController = navController
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
    navController: NavController
) {
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
                    .background(MaterialTheme.colorScheme.surface)
            )
            Column(horizontalAlignment = Alignment.End) {

                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
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
        Spacer(modifier = Modifier.height(16.dp))


        //Implement saved photos here
    }
}
