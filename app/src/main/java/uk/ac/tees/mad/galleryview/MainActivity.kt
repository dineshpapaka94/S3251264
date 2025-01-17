package uk.ac.tees.mad.galleryview

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import uk.ac.tees.mad.galleryview.presentation.SplashScreen
import uk.ac.tees.mad.galleryview.presentation.auth.AuthScreen
import uk.ac.tees.mad.galleryview.presentation.clickpicture.ClickPictureScreen
import uk.ac.tees.mad.galleryview.presentation.galleryview.GalleryScreen
import uk.ac.tees.mad.galleryview.presentation.galleryview.ImageData
import uk.ac.tees.mad.galleryview.presentation.picturedetail.PhotoDetailScreen
import uk.ac.tees.mad.galleryview.presentation.profile.EditProfileScreen
import uk.ac.tees.mad.galleryview.presentation.profile.ProfileScreen
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
            AuthScreen(navController = navController)
        }
        composable(Screen.ProfileScreen.route) {
            ProfileScreen(navController = navController)
        }
        composable(Screen.EditProfileScreen.route) {
            EditProfileScreen(navController = navController)
        }
        composable(Screen.ClickPictureScreen.route) {
            ClickPictureScreen(navController = navController)
        }
        composable(Screen.GalleryViewScreen.route) {
            GalleryScreen(navController = navController)
        }
        composable(
            route = Screen.PhotoDetailViewScreen.route
        ) { backstackEntry ->
            val imageDetail =
                navController.previousBackStackEntry?.savedStateHandle?.get<ImageData>("photo_detail")
            imageDetail?.let {
                Log.d("IMK", imageDetail.toString())
                PhotoDetailScreen(imageDetail = it, navController = navController)
            }
        }
    }
}

sealed class Screen(val route: String) {
    object SplashScreen : Screen("splash_screen")
    object AuthScreen : Screen("auth_screen")
    object ProfileScreen : Screen("profile_screen")
    object EditProfileScreen : Screen("edit_profile_screen")
    object ClickPictureScreen : Screen("click_picture_screen")
    object GalleryViewScreen : Screen("gallery_view_screen")
    object PhotoDetailViewScreen : Screen("photo_detail_view_screen/{photo_detail}") {
    }

    object SavedPhotoScreen : Screen("saved_photo_screen")
}