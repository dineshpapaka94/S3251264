package uk.ac.tees.mad.galleryview.presentation.galleryview

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person2
import androidx.compose.material.icons.filled.PersonPin
import androidx.compose.material.icons.rounded.Person3
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import uk.ac.tees.mad.galleryview.R
import uk.ac.tees.mad.galleryview.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    navController: NavHostController,
    viewModel: GalleryViewModel = viewModel()
) {
    val images = viewModel.images
    val isLoadin = viewModel.isLoading

    LaunchedEffect(Unit) {
        viewModel.fetchUserImages()
    }

    Scaffold(

        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.ProfileScreen.route) }) {
                        Icon(Icons.Rounded.Person3, contentDescription = "profile")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.ClickPictureScreen.route) }
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "click picture")
            }
        },

        ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (isLoadin.value) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            if (images.isEmpty()) {
                Text(
                    text = "No images found",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                columns = GridCells.Adaptive(100.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {

                items(images.size) {
                    val image = images[it]
                    Log.d("IMAGES_MIN", image.toString())
                    ImageItem(image = image.imageUrl, onClick = {
                        navController.currentBackStackEntry?.savedStateHandle?.set(
                            "photo_detail",
                            image
                        )
                        navController.navigate(Screen.PhotoDetailViewScreen.route)
                    })
                }

            }
        }

    }
}


@Composable
fun ImageItem(image: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(Color.LightGray)
    ) {
        Image(
            painter = rememberAsyncImagePainter(image),
            contentDescription = "Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}