package uk.ac.tees.mad.galleryview.ui.navigation

sealed class Screen(val route: String) {
    object SplashScreen : Screen("splash_screen")
    object AuthScreen : Screen("auth_screen")
    object ProfileScreen : Screen("profile_screen")
    object EditProfileScreen : Screen("edit_profile_screen")
    object ClickPictureScreen : Screen("click_picture_screen")
    object GalleryViewScreen : Screen("gallery_view_screen")
    object PhotoDetailViewScreen : Screen("photo_detail_view_screen")
    object SavedPhotoScreen : Screen("saved_photo_screen")
}
