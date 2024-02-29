package com.mongodb.app.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.style.TextAlign
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.navigation.NavHostController
import com.mongodb.app.R
import com.google.accompanist.pager.*
import androidx.compose.foundation.layout.padding
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import android.content.Intent
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import com.mongodb.app.navigation.Routes

// HomeScreen Composable function, serves as the main screen of the app.
@Composable
fun HomeScreen(navController: NavHostController, viewModel: HomeViewModel) {
    // State for bio text with persistence over configuration changes.
    var bioText by rememberSaveable { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    // State for the list of images, initialized empty.
    val imagesList = viewModel.imagesList.value
    var replaceImageIndex by remember { mutableStateOf<Int?>(null) } // Track the index of the image to replace
    var addingNewPhoto by remember { mutableStateOf(false) }
    // Launcher for result to pick images from the gallery.
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val bitmap: Bitmap? = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            }
            bitmap?.let { newBitmap ->
                if (replaceImageIndex != null) {
                    viewModel.replaceImage(replaceImageIndex!!, newBitmap)
                    replaceImageIndex = null
                } else if (addingNewPhoto) {
                    viewModel.addImage(newBitmap)
                    addingNewPhoto = false
                }
            }
        }
    }
    //content area of the screen.
    Box(modifier = Modifier.fillMaxSize()) {
        // Scrollable content area
        Column(modifier = Modifier
            .matchParentSize()
            //.padding(bottom = 150.dp) add this back in if we need space between bottom nav and inbetween
            .verticalScroll(rememberScrollState())) {

            Spacer(modifier = Modifier.height(16.dp))
            // Bio section moved here, under the Header and before ProfilePhotosSection.

            // Section for managing profile photos.
            ProfilePhotosSection(
                bitmaps = imagesList,
                onImageReplace = { index ->
                    // Sets the index for an image to replace and launches image picker.
                    replaceImageIndex = index
                    launcher.launch("image/*")
                },
                onAddPhoto = {
                    addingNewPhoto = true
                    launcher.launch("image/*")
                }
            )
            SocialMediaOptions()

        }
    }
}


//displaying and updating user profile photos.
@OptIn(ExperimentalPagerApi::class)
@Composable
fun ProfilePhotosSection(
    bitmaps: List<Bitmap?>,
    onImageReplace: (Int) -> Unit, // For replacing an image
    onAddPhoto: () -> Unit // For adding a new image

) {
    Column(
        modifier = Modifier
            .background(Color.White)
            .fillMaxWidth()
            // Reduced overall padding for the section
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.profile_photos),
            color = Color.Black,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        // Displays profile photos in a horizontal pager
        if (bitmaps.isNotEmpty()) {
            val pagerState = rememberPagerState()

            HorizontalPager(
                count = bitmaps.size,
                state = pagerState,
                modifier = Modifier
                    .height(127.dp)
                    .padding(bottom = 1.dp)
            ) { page ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    bitmaps[page]?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Photo ${page + 1}",
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    onImageReplace(page)
                                }
                        )
                    }
                }
            }

            CustomPagerIndicator(pagerState = pagerState, totalDots = bitmaps.size)
        }

        // Upload button and photo description input
        if (bitmaps.size < 3) {  //limit to 3 photos
            Button(onClick = onAddPhoto, modifier = Modifier.padding(top = 8.dp)) {
                Text(text = stringResource(id = R.string.upload_photo))
            }
        } else {
            Text(
                "You can upload up to 3 photos.",
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

//displays a set of indicators for the photo pager.
@OptIn(ExperimentalPagerApi::class)
@Composable
fun CustomPagerIndicator(pagerState: PagerState, totalDots: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth() // This will make the Box fill the maximum width
            .padding(16.dp),
        contentAlignment = Alignment.Center // This will align the Row in the center of the Box
    ) {
        Row(
            horizontalArrangement = Arrangement.Center
        ) {
            // Creates a dot for each page
            for (i in 0 until totalDots) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(10.dp)
                        .background(
                            if (pagerState.currentPage == i) Color.Black else Color.LightGray,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}
@Composable
fun SocialMediaOptions() {
    // Use LazyRow for horizontal scrolling
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp) // Space between items
    ) {
        // List of social media options
        val socialMediaItems = listOf(
            Pair(R.drawable.x, "Twitter"),
            Pair(R.drawable.linkedin, "LinkedIn"),
            Pair(R.drawable.instagram, "Instagram"),
            Pair(R.drawable.linktree, "LinkTree")
        )

        items(socialMediaItems) { item ->
            SocialMediaOptionButton(
                icon = painterResource(id = item.first),
                text = item.second
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialMediaOptionButton(icon: Painter, text: String) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    // Determine the initial URL based on the button's text
    val initialUrl = when (text.lowercase()) {
        "twitter" -> "http://twitter.com/"
        "linkedin" -> "http://linkedin.com/"
        "instagram" -> "http://instagram.com/"
        "linktree" -> "http://linktr.ee/"
        else -> "http://"
    }
    var url by rememberSaveable { mutableStateOf(initialUrl) }
    var showError by rememberSaveable { mutableStateOf(false) }
    var urlConfirmed by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = stringResource(id = R.string.dialog_title, text)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = url,
                        onValueChange = {
                            if (!it.startsWith(initialUrl)) {
                                url = initialUrl
                            } else {
                                url = it
                            }
                            showError = false
                        },
                        label = { Text(text = stringResource(id = R.string.url_label)) },
                        isError = showError,
                    )
                    if (showError) {
                        Text(
                            text = stringResource(id = R.string.invalid_url_error),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (url.length > initialUrl.length) {
                            showDialog = false
                            urlConfirmed = true // User has confirmed the URL
                        } else {
                            showError = true
                        }
                    }
                ) { Text(text = stringResource(id = R.string.ok_button)) }
            }
        )
    }

    OutlinedButton(
        onClick = {
            // If the URL has been confirmed, navigate directly; otherwise, show dialog for editing
            if (urlConfirmed) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            } else {
                showDialog = true
            }
        },
        modifier = Modifier
            .width(250.dp)
            .height(56.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterStart
        ) {
            Icon(
                painter = icon,
                contentDescription = text,
                modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.CenterStart),
                tint = Color.Unspecified
            )
            Text(
                text = text,
                modifier = Modifier.align(Alignment.Center)
            )
            Icon(
                painter = painterResource(id = R.drawable.meatball),
                contentDescription = "Clickable Image",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable {
                        showDialog = true
                    }
                    .size(24.dp),
                tint = Color.Unspecified
            )
        }
    }
}