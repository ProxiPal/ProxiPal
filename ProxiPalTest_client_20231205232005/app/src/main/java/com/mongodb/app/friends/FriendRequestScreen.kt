package com.mongodb.app.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mongodb.app.friends.FriendRequestViewModel
import com.mongodb.app.friends.FriendshipRequest
import com.mongodb.app.friends.Friendslist
import com.mongodb.app.presentation.blocking_censoring.BlockingViewModel
import com.mongodb.app.presentation.tasks.ToolbarViewModel
import com.mongodb.app.presentation.userprofiles.UserProfileViewModel
import com.mongodb.app.ui.components.ProxiPalBottomAppBar
import com.mongodb.app.ui.components.ProxipalTopAppBarWithBackButton
import com.mongodb.app.ui.tasks.TaskAppToolbar
import com.mongodb.app.ui.userprofiles.calculateScreenHeight

// Added by Marco to implement the app bars, and a back button
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun FriendRequestScreenLayout(
    navController: NavHostController,
    viewModel: UserProfileViewModel,
    friendRequestViewModel: FriendRequestViewModel
){
    Scaffold(
        topBar = {
            // This top bar is used because it already has logging out of account implemented
            ProxipalTopAppBarWithBackButton(navController = navController, title = "Friend Requests")
        },
        bottomBar = { ProxiPalBottomAppBar(navController) }
    ) { innerPadding ->
        Column (
            Modifier.padding(innerPadding)
        ){
            FriendRequestScreen(
                friendRequestViewModel = friendRequestViewModel,
                userProfileViewModel = viewModel
            )
        }
    }
}


//ALL ADDED BY GEORGE FU
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendRequestScreen(friendRequestViewModel: FriendRequestViewModel, userProfileViewModel: UserProfileViewModel) {
    val uiState by friendRequestViewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Column() {
        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else if (uiState.error != null) {
            Text("Error: ${uiState.error}")
        } else {
            uiState.friendRequests.forEach { request ->
                FriendRequestItem(
                    request = request,
                    friendRequestViewModel = friendRequestViewModel,
                    userProfileViewModel = userProfileViewModel
                )
            }
        }
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun FriendRequestItem(
    request: FriendshipRequest,
    friendRequestViewModel: FriendRequestViewModel,
    userProfileViewModel: UserProfileViewModel
) {
    var senderName by remember { mutableStateOf("Loading...") }

    // Collecting from the readUserProfile flow to get the sender's name
    LaunchedEffect(request.senderId) {
        userProfileViewModel.readUserProfile(request.senderId).collect { userProfile ->
            senderName = "${userProfile?.firstName} ${userProfile?.lastName}"
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(senderName, modifier = Modifier.weight(1f))
        Button(onClick = {
            friendRequestViewModel.respondToFriendRequest(request._id, true, userProfileViewModel::refreshFriendsList)
        }) {
            Text("Accept")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = {
            friendRequestViewModel.respondToFriendRequest(request._id, false, {})
        }) {
            Text("Decline")
        }
    }
}
