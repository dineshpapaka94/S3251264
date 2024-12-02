package uk.ac.tees.mad.galleryview.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import uk.ac.tees.mad.galleryview.Screen
import uk.ac.tees.mad.galleryview.ui.theme.BackgroundColor
import uk.ac.tees.mad.galleryview.ui.theme.PrimaryColor
import uk.ac.tees.mad.galleryview.ui.theme.SecondaryColor

@Composable
fun AuthScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val snackbarHostState = remember {
        SnackbarHostState()
    }
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignInMode by remember { mutableStateOf(true) }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        modifier = Modifier.background(BackgroundColor)
    ) { p ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(p)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = if (isSignInMode) "Welcome Back!" else "Join Us!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isSignInMode) "Sign in to continue" else "Create an account to get started",
                    fontSize = 16.sp,
                    color = SecondaryColor
                )
                Spacer(modifier = Modifier.height(32.dp))
                if (!isSignInMode) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryColor,
                            unfocusedBorderColor = SecondaryColor
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                // Email input
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        unfocusedBorderColor = SecondaryColor
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password input
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        unfocusedBorderColor = SecondaryColor
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (isSignInMode) {
                            viewModel.signIn(email, password)
                        } else {
                            viewModel.signUp(name, email, password)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(PrimaryColor)
                ) {
                    Text(text = if (isSignInMode) "Sign In" else "Sign Up")
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { isSignInMode = !isSignInMode },

                    ) {
                    Text(
                        text = if (isSignInMode) "Don't have an account? Sign Up" else "Already have an account? Sign In",
                        color = PrimaryColor
                    )
                }
            }

            // Show loading or error messages
            when (authState) {
                is AuthState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is AuthState.Error -> {
                    LaunchedEffect(Unit) {
                        snackbarHostState.showSnackbar(
                            (authState as AuthState.Error).message,
                            actionLabel = "Dismiss"
                        )
                    }
                }

                is AuthState.Success -> {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.ProfileScreen.route) {
                            popUpTo(Screen.AuthScreen.route) { inclusive = true }
                        }
                    }
                }

                else -> {}
            }
        }
    }
}
