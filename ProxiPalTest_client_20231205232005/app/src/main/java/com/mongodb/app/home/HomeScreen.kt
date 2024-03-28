package com.mongodb.app.home


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.mongodb.app.presentation.userprofiles.UserProfileViewModel

// HomeScreen Composable function, serves as the main screen of the app.
@Composable
//march7
fun HomeScreen(navController: NavHostController, viewModel: HomeViewModel, userProfileViewModel: UserProfileViewModel) {

    //George Fu - Added the different handling state for each Social Media
    val twitterHandleState = userProfileViewModel.userProfileTwitterHandle.value
    var twitterHandle by remember { mutableStateOf(twitterHandleState) }
    LaunchedEffect(key1 = twitterHandleState) {
        twitterHandle = twitterHandleState
    }
    val linktreeHandleState = userProfileViewModel.userProfileLinktreeHandle.value
    var linktreeHandle by remember { mutableStateOf(linktreeHandleState) }
    LaunchedEffect(key1 = linktreeHandleState) {
        linktreeHandle = linktreeHandleState
    }
    val linkedinHandleState = userProfileViewModel.userProfilelinkedinHandle.value
    var linkedinHandle by remember { mutableStateOf(linkedinHandleState) }
    LaunchedEffect(key1 = linkedinHandleState) {
        linkedinHandle = linkedinHandleState
    }

    val instagramHandleState = userProfileViewModel.userProfileInstagramHandle.value
    var instagramHandle by remember { mutableStateOf(instagramHandleState) }
    LaunchedEffect(key1 = instagramHandleState) {
        instagramHandle = instagramHandleState
    }
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
            SocialMediaOptions(userProfileViewModel = userProfileViewModel)

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
                    .height(150.dp)
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
//George Fu Added Everything Below
@Composable
fun SocialMediaOptions(userProfileViewModel: UserProfileViewModel) {
    val context = LocalContext.current // Obtain context directly within Composable

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
            when (item.second.lowercase()) {
                "instagram" -> {
                    InstagramOptionButton(
                        icon = painterResource(id = item.first),
                        text = item.second,
                        userProfileViewModel = userProfileViewModel
                    )
                }
                "twitter" -> {
                    TwitterOptionButton(
                        icon = painterResource(id = item.first),
                        text = item.second,
                        userProfileViewModel = userProfileViewModel
                    )
                }
                "linktree" -> {
                    LinktreeOptionButton(
                        icon = painterResource(id = item.first),
                        text = item.second,
                        userProfileViewModel = userProfileViewModel
                    )
                }
                "linkedin" -> {
                    LinkedinOptionButton(
                        icon = painterResource(id = item.first),
                        text = item.second,
                        userProfileViewModel = userProfileViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun InstagramOptionButton(icon: Painter, text: String, userProfileViewModel: UserProfileViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    val instagramHandle = userProfileViewModel.userProfileInstagramHandle.value
    val context = LocalContext.current

    OutlinedButton(
        onClick = {
            if (instagramHandle.isNotEmpty()) {
                // If Instagram handle exists, open Instagram profile
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://instagram.com/$instagramHandle"))
                context.startActivity(intent)
            } else {
                // If no handle, show dialog to input
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
                tint = Color.Unspecified // Maintain original icon colors
            )
            Text(
                text = text,
                modifier = Modifier.align(Alignment.Center)
            )
            Icon(
                painter = painterResource(id = R.drawable.meatball), // Ensure you have this icon in your drawable resources
                contentDescription = "Options",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable {
                        // Show dialog for input or additional options
                        showDialog = true
                    }
                    .size(24.dp),
                tint = Color.Unspecified // Maintain original icon colors
            )
        }
    }

    if (showDialog) {
        InputInstagramHandleDialog(
            currentHandle = instagramHandle,
            onHandleConfirm = { newHandle ->
                userProfileViewModel.setUserProfileInstagramHandle(newHandle)
                showDialog = false
            },
            onDismissRequest = { showDialog = false }
        )
    }
}
//this function is for the confirming the username button
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputInstagramHandleDialog(currentHandle: String, onHandleConfirm: (String) -> Unit, onDismissRequest: () -> Unit) {
    var textState by rememberSaveable { mutableStateOf(currentHandle) }
    AlertDialog(
        onDismissRequest = { onDismissRequest() },
        title = { Text("Enter Instagram Username") },
        text = {
            TextField(
                value = textState,
                onValueChange = { textState = it },
                label = { Text("Username") }
            )
        },
        confirmButton = {
            Button(onClick = { onHandleConfirm(textState) }) {
                Text("Confirm")
            }
        }
    )
}

@Composable
fun TwitterOptionButton(icon: Painter, text: String, userProfileViewModel: UserProfileViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    val twitterHandle = userProfileViewModel.userProfileTwitterHandle.value
    val context = LocalContext.current

    OutlinedButton(
        onClick = {
            if (twitterHandle.isNotEmpty()) {
                // If twitter handle exists, open Twitter profile
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://twitter.com/$twitterHandle"))
                context.startActivity(intent)
            } else {
                // If no handle, show dialog to input
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
                tint = Color.Unspecified // Maintain original icon colors
            )
            Text(
                text = text,
                modifier = Modifier.align(Alignment.Center)
            )
            Icon(
                painter = painterResource(id = R.drawable.meatball), // Ensure you have this icon in your drawable resources
                contentDescription = "Options",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable {
                        // Show dialog for input or additional options
                        showDialog = true
                    }
                    .size(24.dp),
                tint = Color.Unspecified // Maintain original icon colors
            )
        }
    }

    if (showDialog) {
        InputTwitterHandleDialog(
            currentHandle = twitterHandle,
            onHandleConfirm = { newHandle ->
                userProfileViewModel.setUserProfileTwitterHandle(newHandle)
                showDialog = false
            },
            onDismissRequest = { showDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputTwitterHandleDialog(currentHandle: String, onHandleConfirm: (String) -> Unit, onDismissRequest: () -> Unit) {
    var textState by rememberSaveable { mutableStateOf(currentHandle) }
    AlertDialog(
        onDismissRequest = { onDismissRequest() },
        title = { Text("Enter Twitter Username") },
        text = {
            TextField(
                value = textState,
                onValueChange = { textState = it },
                label = { Text("Username") }
            )
        },
        confirmButton = {
            Button(onClick = { onHandleConfirm(textState) }) {
                Text("Confirm")
            }
        }
    )
}
@Composable
fun LinktreeOptionButton(icon: Painter, text: String, userProfileViewModel: UserProfileViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    val linktreeHandle = userProfileViewModel.userProfileLinktreeHandle.value
    val context = LocalContext.current

    OutlinedButton(
        onClick = {
            if (linktreeHandle.isNotEmpty()) {
                // If linktree handle exists, open LinkTree profile
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://linktr.ee/$linktreeHandle"))
                context.startActivity(intent)
            } else {
                // If no handle, show dialog to input
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
                tint = Color.Unspecified // Maintain original icon colors
            )
            Text(
                text = text,
                modifier = Modifier.align(Alignment.Center)
            )
            Icon(
                painter = painterResource(id = R.drawable.meatball), // Ensure you have this icon in your drawable resources
                contentDescription = "Options",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable {
                        // Show dialog for input or additional options
                        showDialog = true
                    }
                    .size(24.dp),
                tint = Color.Unspecified // Maintain original icon colors
            )
        }
    }

    if (showDialog) {
        InputLinktreeHandleDialog(
            currentHandle = linktreeHandle,
            onHandleConfirm = { newHandle ->
                userProfileViewModel.setUserProfileLinktreeHandle(newHandle)
                showDialog = false
            },
            onDismissRequest = { showDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputLinktreeHandleDialog(currentHandle: String, onHandleConfirm: (String) -> Unit, onDismissRequest: () -> Unit) {
    var textState by rememberSaveable { mutableStateOf(currentHandle) }
    AlertDialog(
        onDismissRequest = { onDismissRequest() },
        title = { Text("Enter Linktree Username") },
        text = {
            TextField(
                value = textState,
                onValueChange = { textState = it },
                label = { Text("Username") }
            )
        },
        confirmButton = {
            Button(onClick = { onHandleConfirm(textState) }) {
                Text("Confirm")
            }
        }
    )
}

@Composable
fun LinkedinOptionButton(icon: Painter, text: String, userProfileViewModel: UserProfileViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    val linkedinHandle = userProfileViewModel.userProfilelinkedinHandle.value
    val context = LocalContext.current

    OutlinedButton(
        onClick = {
            if (linkedinHandle.isNotEmpty()) {
                // If linkedin handle exists, open LinkedIn profile
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://$linkedinHandle"))
                context.startActivity(intent)
            } else {
                // If no handle, show dialog to input
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
                tint = Color.Unspecified // Maintain original icon colors
            )
            Text(
                text = text,
                modifier = Modifier.align(Alignment.Center)
            )
            Icon(
                painter = painterResource(id = R.drawable.meatball), // Ensure you have this icon in your drawable resources
                contentDescription = "Options",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable {
                        // Show dialog for input or additional options
                        showDialog = true
                    }
                    .size(24.dp),
                tint = Color.Unspecified // Maintain original icon colors
            )
        }
    }

    if (showDialog) {
        InputlinkedinHandleDialog(
            currentHandle = linkedinHandle,
            onHandleConfirm = { newHandle ->
                userProfileViewModel.setUserProfilelinkedinHandle(newHandle)
                showDialog = false
            },
            onDismissRequest = { showDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputlinkedinHandleDialog(currentHandle: String, onHandleConfirm: (String) -> Unit, onDismissRequest: () -> Unit) {
    var textState by rememberSaveable { mutableStateOf(currentHandle) }
    AlertDialog(
        onDismissRequest = { onDismissRequest() },
        title = { Text("Enter LinkedIn URL") },
        text = {
            TextField(
                value = textState,
                onValueChange = { textState = it },
                label = { Text("Username") }
            )
        },
        confirmButton = {
            Button(onClick = { onHandleConfirm(textState) }) {
                Text("Confirm")
            }
        }
    )
}
