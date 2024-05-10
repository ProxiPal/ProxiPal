package com.mongodb.app.friends

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mongodb.app.R
import com.mongodb.app.data.messages.MessagesData
import com.mongodb.app.home.HomeScreen
import com.mongodb.app.navigation.Routes
import com.mongodb.app.presentation.blocking_censoring.BlockingAction
import com.mongodb.app.presentation.blocking_censoring.BlockingViewModel
import com.mongodb.app.presentation.tasks.ToolbarViewModel
import com.mongodb.app.presentation.userprofiles.UserProfileViewModel
import com.mongodb.app.ui.blocking_censoring.BlockingAlert
import com.mongodb.app.ui.components.ProxiPalBottomAppBar
import com.mongodb.app.ui.tasks.TaskAppToolbar
import com.mongodb.app.ui.userprofiles.UserProfileBody
import com.mongodb.app.ui.userprofiles.calculateScreenHeight
import com.mongodb.app.userratingsystem.RateUserPopup
import com.mongodb.app.userratingsystem.UserRatingsDisplayScreen

// FriendsListLayout added by Marco to implement app bars
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun FriendsListLayout(
    navController: NavHostController,
    viewModel: UserProfileViewModel,
    friendRequestViewModel: FriendRequestViewModel,
    blockingViewModel: BlockingViewModel,
    toolbarViewModel: ToolbarViewModel
){
    Scaffold(
        topBar = {
            // This top bar is used because it already has logging out of account implemented
            TaskAppToolbar(viewModel = toolbarViewModel, navController = navController, title = "Friends")
        },
        bottomBar = { ProxiPalBottomAppBar(navController) }
    ) { innerPadding ->
        Column (
            Modifier.padding(innerPadding)
        ){
            Friendslist(
                navController = navController,
                viewModel = viewModel,
                friendRequestViewModel = friendRequestViewModel,
                blockingViewModel = blockingViewModel,
                toolbarViewModel = toolbarViewModel
            )
        }
    }
}

//ALL ADDED BY GEORGE FU
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Friendslist(
    navController: NavHostController,
    viewModel: UserProfileViewModel,
    friendRequestViewModel: FriendRequestViewModel,
    blockingViewModel: BlockingViewModel,
    toolbarViewModel: ToolbarViewModel
) {
    val context = LocalContext.current
    val searchText = remember { mutableStateOf(TextFieldValue()) }
    val feedback by friendRequestViewModel.feedback.collectAsState(initial = "")
    val currentUserId by viewModel.currentUserId
    val friendIds by viewModel.friendIdsList.collectAsState()




    LaunchedEffect(feedback) {
        if (feedback.isNotEmpty()) {
            Toast.makeText(context, feedback, Toast.LENGTH_SHORT).show()
            friendRequestViewModel.clearFeedback()  // Reset the feedback after showing toast
        }
    }

    Column (Modifier){
        SmallTopAppBar(
            title = { Text("Friends List") },
            navigationIcon = {
                IconButton(onClick = { navController.navigate(Routes.FriendRequestScreen.route) }) {
                    Icon(Icons.Filled.Mail, contentDescription = "Mail", tint = Color.Black)
                }
            },
            actions = {
                TextField(
                    value = searchText.value,
                    onValueChange = { newText ->
                        if (newText.text.length <= 30) searchText.value = newText
                    },
                    singleLine = true,
                    placeholder = { Text("Enter User ID to add") },
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 50.dp)
                )
            },
            colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        )

        Row {
            Button(onClick = {
                if (searchText.value.text.isNotEmpty()) {
                    friendRequestViewModel.onSendFriendRequestButtonClicked(searchText.value.text)
                }
            }) {
                Text("Send Friend Request")
            }

            Spacer(Modifier.width(8.dp))

            Button(onClick = { searchText.value = TextFieldValue("") }) {
                Text("Cancel")
            }
        }

        Text("Your User ID: $currentUserId", modifier = Modifier.padding(16.dp))

        LazyColumn {
            items(friendIds) { friendId ->
                FriendItem(
                    friendId = friendId,
                    viewModel = viewModel,
                    blockingViewModel = blockingViewModel,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun FriendItem(
    friendId: String,
    viewModel: UserProfileViewModel,
    blockingViewModel: BlockingViewModel,
    navController: NavHostController
) {
    var showMenu by remember { mutableStateOf(false) }
    var showRatingPopup by remember { mutableStateOf(false) }
    val friendName = viewModel.getFriendNameFromFriendId(friendId)
    val isUserBlocked = blockingViewModel.isUserBlocked(friendId)
    // This friend ID needs to be accessed by the compass and messages screens
    MessagesData.updateUserIdInFocus(friendId)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp)
            .clickable { /* Navigate to friend detail or perform action */ },
        shape = RoundedCornerShape(50.dp), // This creates the circular look
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = friendName,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showMenu = !showMenu }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "More Options")
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                if (!isUserBlocked){
                    DropdownMenuItem(
                        text = {
                            Text(stringResource(id = R.string.friends_list_navigate_to_compass_screen))
                        },
                        onClick = {
                            navController.navigate(Routes.CompassScreen.route)
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(stringResource(id = R.string.friends_list_navigate_to_messages_screen))
                               },
                        onClick = {
                            showMenu = false
                            navController.navigate(Routes.MessagesScreen.route)
                        }
                    )
                }
                DropdownMenuItem(
                    text = {
                        Text(
                            text =
                            if (isUserBlocked) stringResource(id = R.string.blocking_contextual_menu_unblock_user)
                            else stringResource(id = R.string.blocking_contextual_menu_block_user)
                        )
                           },
                    onClick = {
                        showMenu = false
                        if (isUserBlocked){
                            blockingViewModel.unblockUserStart(
                                userIdToUnblock = friendId
                            )
                            blockingViewModel.unblockUser()
                        }
                        else{
                            blockingViewModel.blockUserStart(
                                userIdToBlock = friendId
                            )
                            blockingViewModel.blockUser()
                        }
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete Friend") },
                    onClick = {
                        showMenu = false
                        viewModel.removeFriend(friendId)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Rate User") },
                    onClick = {
                        showRatingPopup = true
                    }
                )
            }
        }
    }
    if (showRatingPopup){
        RateUserPopup(otherUserOwnerId = friendId, userProfileViewModel = viewModel, onClose = {showRatingPopup = false})
    }
}
