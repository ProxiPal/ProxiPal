package com.mongodb.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mongodb.app.friends.FriendRequestViewModel
import com.mongodb.app.friends.FriendshipRequest
import com.mongodb.app.data.SyncRepository
import kotlinx.coroutines.flow.map


//ALL ADDED BY GEORGE FU
@Composable
fun FriendRequestItem(request: FriendshipRequest, friendRequestViewModel: FriendRequestViewModel, repository: SyncRepository) {
    val senderProfile by repository.readUserProfile(request.senderId).map { it.list.firstOrNull() }.collectAsState(initial = null)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        senderProfile?.let {
            Text("From: ${it.firstName} ${it.lastName}", modifier = Modifier.weight(1f))
        } ?: Text("Loading...", modifier = Modifier.weight(1f))

        Button(onClick = {
            friendRequestViewModel.respondToFriendRequest(request._id, true) // directly use request._id
        }) {
            Text("Accept")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = {
            friendRequestViewModel.respondToFriendRequest(request._id, false) // directly use request._id
        }) {
            Text("Decline")
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendRequestScreen(friendRequestViewModel: FriendRequestViewModel, repository: SyncRepository) {
    val uiState by friendRequestViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text(text = "Friend Requests") },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
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
                    FriendRequestItem(request, friendRequestViewModel, repository)
                }
            }
        }
    }
}

