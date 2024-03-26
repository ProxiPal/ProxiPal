package com.mongodb.app.ui.messages

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.mongodb.app.R
import com.mongodb.app.TAG
import com.mongodb.app.data.MockRepository
import com.mongodb.app.data.RealmSyncRepository
import com.mongodb.app.data.messages.LONG_MESSAGE_CHARACTER_THRESHOLD
import com.mongodb.app.data.messages.MESSAGE_WIDTH_WEIGHT
import com.mongodb.app.data.messages.MessagesUserAction
import com.mongodb.app.data.messages.MockConversationRepository
import com.mongodb.app.data.messages.MockMessagesRepository
import com.mongodb.app.domain.FriendMessage
import com.mongodb.app.presentation.messages.MessagesViewModel
import com.mongodb.app.ui.theme.MessageColorMine
import com.mongodb.app.ui.theme.MessageColorOther
import com.mongodb.app.ui.theme.MessageInputBackgroundColor
import com.mongodb.app.ui.theme.MyApplicationTheme
import com.mongodb.app.ui.theme.Purple200
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date


/*
TODO List of tasks to do for messages screen
- Make message history update continuously
- Make changes to both friend profile picture and IDs of users involved
... when navigating from friends screen to messages screen
- Add ability to reply to messages
| Add a small label showing what the message is in response to
| If the original message is updated or deleted, update the small label under the message reply appropriately
| ... Need to update database schema and add "messageIdRepliedTo": String that is either
| ... equal to the original message's ID if the message still exists or the empty string if the message doesn't exist
*/


class MessagesScreen : ComponentActivity() {
    // region Variables
    private val repository = RealmSyncRepository { _, _ ->
        lifecycleScope.launch {
        }
    }

    private val messagesViewModel: MessagesViewModel by viewModels {
        MessagesViewModel.factory(
            repository = repository,
            messagesRealm = repository,
            conversationsRealm = repository,
            this
        )
    }
    // endregion Variables


    // region Functions
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i(
            TAG(),
            "MessagesScreen: Start of OnCreate()"
        )

        // TODO These values are hardcoded for now
        val usersInvolved = sortedSetOf(
            // Gmail account
            "65e96193c6e205c32b0915cc",
            // Student account
            "6570119696faac878ad696a5"
        )
        messagesViewModel.updateUsersInvolved(usersInvolved)

        setContent {
            MessagesScreenLayout(
                messagesViewModel = messagesViewModel
            )
        }
    }
    // endregion Functions
}


// region Functions
/**
 * Displays the entire messages screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreenLayout(
    messagesViewModel: MessagesViewModel,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            MessagesTopBar()
        },
        modifier = modifier
        // Pad the body of content so it does not get cut off by the scaffold top bar
    ) { innerPadding ->
        MessagesBodyContent(
            messagesViewModel = messagesViewModel,
            modifier = Modifier
                .padding(innerPadding)
        )
    }
}

/**
 * Displays the top bar for the compass screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesTopBar(
    modifier: Modifier = Modifier
) {
    // Back button is automatically handled by the navigation code (?)
    // ... so it's not programmed here
    CenterAlignedTopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        // TODO Change this to a person's profile picture
                        painter = painterResource(id = R.drawable.linkedin),
                        contentDescription = null,
                        // Crops the image to fit (the circular shape space)
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .size(dimensionResource(id = R.dimen.messages_screen_profile_picture_size))
                    )
                    Text(
                        text = stringResource(
                            id = R.string.app_name
                        ),
                        color = Color.Black,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        },
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = Purple200
        ),
        modifier = modifier
    )
}

/**
 * The main middle section that displays all the messages between 2 users
 */
@Composable
fun MessagesBodyContent(
    messagesViewModel: MessagesViewModel,
    modifier: Modifier = Modifier
) {
    if (messagesViewModel.isDeletingMessage()) {
        MessagesDeleteAlert(
            onDismissRequest = {
                messagesViewModel.deleteMessageEnd()
            },
            onDismissButtonClick = {
                messagesViewModel.deleteMessageEnd()
            },
            onConfirmButtonClick = {
                messagesViewModel.deleteMessage(
                    friendMessageToDelete = messagesViewModel.friendMessageUnderActionFocus!!
                )
                messagesViewModel.deleteMessageEnd()
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        LazyColumn(
            // Starts with the more recent messages at the bottom
            // Start at the bottom, then scroll up to show older messages
            reverseLayout = true,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Log.i(
                TAG(),
                "MessagesScreen: Retrieved message amount = " +
                        "\"${messagesViewModel.messagesListState.size}\""
            )

            // Start with showing the most recent messages first
            val friendMessages = messagesViewModel.messagesListState.toList().reversed()
            items(friendMessages) { friendMessage ->
                SingleMessageContainer(
                    friendMessage = friendMessage,
                    isSenderMe = messagesViewModel.isMessageMine(friendMessage),
                    messagesViewModel = messagesViewModel
                )
                // If the sent message is the last in the list
                // ... or in other words the first ever message sent
                // Because the messages are shown starting from the bottom of the screen
                // ... to make this appear at the very top, this should go after
                // ... message composable
                if (friendMessages.indexOf(friendMessage) == friendMessages.size - 1) {
                    MessagesEndOfHistory()
                }
            }
        }
        if (messagesViewModel.isReplyingToMessage()) {
            MessagesReplyUpdateRow(
                messagesViewModel = messagesViewModel,
                tabStringId = R.string.messages_screen_reply_message_notifier
            )
        } else if (messagesViewModel.isUpdatingMessage()) {
            MessagesReplyUpdateRow(
                messagesViewModel = messagesViewModel,
                tabStringId = R.string.messages_screen_update_message_notifier
            )
        }
        MessagesInputRow(
            messagesViewModel = messagesViewModel
        )
    }
}

/**
 * Shows a single line of text to designate the end of the user's message history
 */
@Composable
fun MessagesEndOfHistory(
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.messages_screen_end_of_messages),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

/**
 * The container for a message's text as well as other features for a single message
 */
@Composable
fun SingleMessageContainer(
    friendMessage: FriendMessage,
    isSenderMe: Boolean,
    messagesViewModel: MessagesViewModel,
    modifier: Modifier = Modifier
) {
    val rowArrangement =
        if (isSenderMe) Arrangement.End else Arrangement.Start
    val columnAlignment =
        if (isSenderMe) Alignment.End else Alignment.Start

    // To ensure the message card can vary in size depending on the message size,
    // ... the card will expand horizontally at first for shorter messages
    // ... then expand vertically for longer messages
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                top = dimensionResource(id = R.dimen.messages_screen_message_container_padding),
                bottom = dimensionResource(id = R.dimen.messages_screen_message_container_padding),
                start = if (isSenderMe) 0.dp
                else dimensionResource(id = R.dimen.messages_screen_message_container_padding),
                end = if (isSenderMe) dimensionResource(id = R.dimen.messages_screen_message_container_padding)
                else 0.dp
            )
    ) {
        if (isSenderMe) {
            Spacer(
                modifier = Modifier
                    .weight(1 - MESSAGE_WIDTH_WEIGHT)
            )
        }
        Row(
            horizontalArrangement = rowArrangement,
            modifier = Modifier
                .weight(MESSAGE_WIDTH_WEIGHT)
        ) {
            Column(
                horizontalAlignment = columnAlignment
            ) {
                SingleMessage(
                    message = friendMessage.message,
                    isSenderMe = isSenderMe,
                    modifier = Modifier
                )
                MessagesContextualMenu(
                    friendMessage = friendMessage,
                    isSenderMe = isSenderMe,
                    messagesViewModel = messagesViewModel
                )
            }
        }
        if (!isSenderMe) {
            Spacer(
                modifier = Modifier
                    .weight(1 - MESSAGE_WIDTH_WEIGHT)
            )
        }
    }
}

/**
 * Shows a container with only the message text inside it
 */
@Composable
fun SingleMessage(
    message: String,
    isSenderMe: Boolean,
    modifier: Modifier = Modifier
) {
    val messageShape =
        // Every corner except the lower right is rounded
        if (isSenderMe) RoundedCornerShape(
            topStart = dimensionResource(id = R.dimen.messages_screen_message_container_rounding),
            topEnd = dimensionResource(id = R.dimen.messages_screen_message_container_rounding),
            bottomStart = dimensionResource(id = R.dimen.messages_screen_message_container_rounding)
        )
        // Every corner except the lower left is rounded
        else RoundedCornerShape(
            topStart = dimensionResource(id = R.dimen.messages_screen_message_container_rounding),
            topEnd = dimensionResource(id = R.dimen.messages_screen_message_container_rounding),
            bottomEnd = dimensionResource(id = R.dimen.messages_screen_message_container_rounding)
        )

    // For adding long-press functionality:
    // .clickable's onLongClick creates IDE error
    // .combinedClickable's onLongClick creates IDE error
    // .pointerInput's detectTapGestures's onLongPress creates IDE error only with Cards
    // ... Box and Surface show no errors, but do not do anything when long-pressed
    // Also, does not seem like GestureDetector.OnGestureListener works for long presses
    Card(
        colors = CardDefaults.cardColors(
            containerColor =
            if (isSenderMe) MessageColorMine
            else MessageColorOther
        ),
        shape = messageShape,
        modifier = modifier
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            softWrap = true,
            overflow = TextOverflow.Clip,
            modifier = Modifier
                .padding(
                    top = dimensionResource(id = R.dimen.messages_screen_message_vertical_padding),
                    bottom = dimensionResource(id = R.dimen.messages_screen_message_vertical_padding),
                    start = dimensionResource(id = R.dimen.messages_screen_message_horizontal_padding),
                    end = dimensionResource(id = R.dimen.messages_screen_message_horizontal_padding)
                )
        )
    }
}

/**
 * Provides an additional menu with extra actions the user can take
 */
@Composable
fun MessagesContextualMenu(
    friendMessage: FriendMessage,
    isSenderMe: Boolean,
    messagesViewModel: MessagesViewModel,
    modifier: Modifier = Modifier
) {
    var isContextualMenuOpen by rememberSaveable { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        IconButton(
            onClick = {
                isContextualMenuOpen = true
            }
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Open Contextual Menu"
            )
        }

        DropdownMenu(
            expanded = isContextualMenuOpen,
            onDismissRequest = {
                isContextualMenuOpen = false
            }
        ) {
            // Show options to either update or delete your messages
            if (isSenderMe) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(id = R.string.messages_screen_update_message_contextual_menu)
                        )
                    },
                    onClick = {
                        isContextualMenuOpen = false
                        if (messagesViewModel.isNotPerformingAnyContextualMenuAction()) {
                            messagesViewModel.updateMessageStart(
                                friendMessageToEdit = friendMessage
                            )
                        }
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(id = R.string.messages_screen_delete_message_contextual_menu)
                        )
                    },
                    onClick = {
                        isContextualMenuOpen = false
                        if (messagesViewModel.isNotPerformingAnyContextualMenuAction()) {
                            messagesViewModel.deleteMessageStart(
                                friendMessageToDelete = friendMessage
                            )
                        }
                    }
                )
            }
            // Show only an option to reply to another user's messages
            else {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(id = R.string.messages_screen_reply_message_contextual_menu)
                        )
                    },
                    onClick = {
                        isContextualMenuOpen = false
                        if (messagesViewModel.isNotPerformingAnyContextualMenuAction()) {
                            messagesViewModel.replyMessageStart(
                                friendMessageBeingRepliedTo = friendMessage
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun MessagesReplyUpdateRow(
    messagesViewModel: MessagesViewModel,
    @StringRes
    tabStringId: Int,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .padding(
                    all = 4.dp
                )
        ) {
            val messageUnderActionFocus: String? =
                messagesViewModel.friendMessageUnderActionFocus?.message
            val messageToDisplay = if (messageUnderActionFocus == null) {
                stringResource(id = R.string.messages_screen_message_reference_invalid)
            } else if (messageUnderActionFocus.length <= LONG_MESSAGE_CHARACTER_THRESHOLD) {
                stringResource(id = R.string.messages_screen_message_reference_short,
                    messageUnderActionFocus)
            } else {
                stringResource(id = R.string.messages_screen_message_reference_long,
                    messageUnderActionFocus.substring(0, LONG_MESSAGE_CHARACTER_THRESHOLD))
            }
            Text(
                // Message variable should not be null at this point
                text = stringResource(id = tabStringId, messageToDisplay),
                modifier = Modifier
                    .padding(
                        top = 4.dp,
                        start = 8.dp,
                        end = 8.dp,
                        bottom = 4.dp
                    )
            )
        }
    }
}

@Composable
fun MessagesDeleteAlert(
    onDismissRequest: (() -> Unit),
    onDismissButtonClick: (() -> Unit),
    onConfirmButtonClick: (() -> Unit),
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = { onDismissRequest() },
        dismissButton = {
            TextButton(
                onClick = { onDismissButtonClick() }
            ) {
                Text(
                    text = stringResource(id = R.string.messages_screen_delete_message_dismiss)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirmButtonClick() }
            ) {
                Text(
                    text = stringResource(id = R.string.messages_screen_delete_message_confirm)
                )
            }
        },
        text = {
            Text(
                text = stringResource(id = R.string.messages_screen_delete_message_warning_text)
            )
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesInputRow(
    messagesViewModel: MessagesViewModel,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MessageInputBackgroundColor
            )
    ) {
        val onIconButtonClick: (() -> Unit) = when (messagesViewModel.currentAction.value) {
            // Refresh current message history
            MessagesUserAction.IDLE -> {
                messagesViewModel::refreshMessages
            }
            // Cancel process to update messages
            MessagesUserAction.UPDATE -> {
                messagesViewModel::updateMessageEnd
            }
            // Cancel process to delete message
            MessagesUserAction.DELETE -> {
                messagesViewModel::deleteMessageEnd
            }
            // Cancel process to reply to an message
            MessagesUserAction.REPLY -> {
                messagesViewModel::replyMessageEnd
            }
        }
        val iconImageVector = when (messagesViewModel.currentAction.value) {
            MessagesUserAction.IDLE -> Icons.Default.Refresh
            MessagesUserAction.UPDATE -> Icons.Default.Cancel
            MessagesUserAction.DELETE -> Icons.Default.Cancel
            MessagesUserAction.REPLY -> Icons.Default.Cancel
        }
        // Button to refresh message history
        IconButton(
            onClick = { onIconButtonClick() }
        ) {
            Icon(
                imageVector = iconImageVector,
                contentDescription = null
            )
        }
        TextField(
            value = messagesViewModel.message.value,
            placeholder = {
                Text(
                    text = stringResource(id = R.string.messages_screen_empty_message_field)
                )
            },
            onValueChange = { messagesViewModel.updateMessage(it) },
            singleLine = true,
            modifier = Modifier
                .weight(1f)
            // Padding causes the messages' UI to go too large for some reason
//                .padding(
//                    top = dimensionResource(id = R.dimen.messages_screen_message_input_padding)
//                )
        )
        // Button to send/update message
        IconButton(
            onClick = { messagesViewModel.sendMessage() }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send message"
            )
        }
    }
}
// endregion Functions


// region PreviewFunctions
@Preview(showBackground = true)
@Composable
fun TimePreview() {
    MyApplicationTheme {
        // There are many established ways online to get the system time as a number
        // ... but using Calendar.getInstance() might be the most common answer
        Column {
            // Note, numerical values are in milliseconds, not seconds
            // Dates are in PDT, but millisecond times are in GMT (?)

//            // This works
//            Text(
//                text = "Timestamp = \n\"${Timestamp(System.currentTimeMillis())}\""
//            )
//            Text(
//                text = "System time = \n\"${System.currentTimeMillis()}\""
//            )

//            // This works too
//            Text(
//                text = "Current date time = \n\"${java.util.Date()}\""
//            )
//            Text(
//                text = "Current timestamp = \n\"${java.util.Date().time}\""
//            )

//            // This works too
//            Text(
//                text = "Local date time = \n\"${LocalDateTime.now()}\""
//            )

//            // This works too
//            Text(
//                text = "Instant time = \n\"${Instant.now().epochSecond}\""
//            )
//            Text(
//                text = "Instant ms time = \n\"${Instant.now().toEpochMilli()}\""
//            )

            // This works too
            Text(
                text = "Calendar date time = \n\"${Calendar.getInstance().time}\""
            )
            Text(
                text = "Calendar time = \n\"${Calendar.getInstance().timeInMillis}\""
            )
            Text(
                text = "Calendar date from time = \n\"${Date(Calendar.getInstance().timeInMillis)}\""
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MessagesScreenLayoutPreview() {
    MyApplicationTheme {
        val mockSyncRepository = MockRepository()
        val mockMessagesRepository = MockMessagesRepository()
        val mockConversationRepository = MockConversationRepository()
        MessagesScreenLayout(
            messagesViewModel = MessagesViewModel(
                repository = mockSyncRepository,
                messagesRepository = mockMessagesRepository,
                conversationsRepository = mockConversationRepository
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SingleMessageContainerPreview() {
    MyApplicationTheme {
        Column {
            val messagesViewModel = MessagesViewModel(
                MockRepository(), MockMessagesRepository(), MockConversationRepository()
            )
            SingleMessageContainer(
                friendMessage = FriendMessage().apply {
                    message =
                        stringResource(id = R.string.user_profile_test_string)
                },
                isSenderMe = false,
                messagesViewModel = messagesViewModel
            )
            SingleMessageContainer(
                friendMessage = FriendMessage().apply {
                    message =
                        stringResource(id = R.string.user_profile_test_string)
                },
                isSenderMe = true,
                messagesViewModel = messagesViewModel
            )
        }
    }
}
// endregion PreviewFunctions