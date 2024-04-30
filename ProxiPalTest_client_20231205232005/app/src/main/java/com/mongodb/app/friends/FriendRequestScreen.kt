package com.mongodb.app.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mongodb.app.friends.FriendRequestViewModel
import com.mongodb.app.friends.FriendshipRequest
import com.mongodb.app.presentation.userprofiles.UserProfileViewModel


//ALL ADDED BY GEORGE FU
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendRequestScreen(friendRequestViewModel: FriendRequestViewModel, userProfileViewModel: UserProfileViewModel) {
    val uiState by friendRequestViewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text(text = "Friend Requests") }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
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
