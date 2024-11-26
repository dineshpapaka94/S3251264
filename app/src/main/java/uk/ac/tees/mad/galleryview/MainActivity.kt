package uk.ac.tees.mad.galleryview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import uk.ac.tees.mad.galleryview.presentation.SplashScreen
import uk.ac.tees.mad.galleryview.ui.navigation.Screen
import uk.ac.tees.mad.galleryview.ui.theme.GalleryViewTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GalleryViewTheme {
                GalleryViewNavigation()
            }
        }
    }
}

@Composable
fun GalleryViewNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.SplashScreen.route) {
        composable(Screen.SplashScreen.route) {
            SplashScreen(navController = navController)
        }
        composable(Screen.AuthScreen.route) {
        }
        composable(Screen.ProfileScreen.route) {
        }
        composable(Screen.EditProfileScreen.route) {
        }
        composable(Screen.ClickPictureScreen.route) {
        }
        composable(Screen.GalleryViewScreen.route) {
        }
        composable(Screen.PhotoDetailViewScreen.route) {
        }
        composable(Screen.SavedPhotoScreen.route) {
        }
    }
}