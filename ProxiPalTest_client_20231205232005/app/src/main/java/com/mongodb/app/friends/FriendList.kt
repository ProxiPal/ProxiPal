package com.mongodb.app.friends

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mongodb.app.navigation.Routes
import com.mongodb.app.presentation.userprofiles.UserProfileViewModel
import android.widget.Toast
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.platform.LocalContext



//ALL ADDED BY GEORGE FU
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Friendslist(
    navController: NavController,
    viewModel: UserProfileViewModel,
    friendRequestViewModel: FriendRequestViewModel
) {
    val context = LocalContext.current
    val searchText = remember { mutableStateOf(TextFieldValue()) }
    val feedback by friendRequestViewModel.feedback.collectAsState(initial = "")
    val currentUserId by viewModel.currentUserId
    val friends by viewModel.friendsList.collectAsState()



    LaunchedEffect(feedback) {
        if (feedback.isNotEmpty()) {
            Toast.makeText(context, feedback, Toast.LENGTH_SHORT).show()
            friendRequestViewModel.clearFeedback()  // Reset the feedback after showing toast
        }
    }

    Column {
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
                    onValueChange = { newText -> if (newText.text.length <= 30) searchText.value = newText },
                    singleLine = true,
                    placeholder = { Text("Enter User ID to add") },
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth().padding(start = 50.dp)
                )
            },
            colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        )

        Row {
            Button(onClick = {
                if (searchText.value.text.isNotEmpty()) {
                    // Call the updated function with validation checks
                    friendRequestViewModel.onSendFriendRequestButtonClicked(searchText.value.text)
                } else {
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
            items(friends.size) { index ->
                Text(friends[index], modifier = Modifier.padding(8.dp))
            }
        }
    }
}

