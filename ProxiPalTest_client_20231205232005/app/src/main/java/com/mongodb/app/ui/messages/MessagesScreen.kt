package com.mongodb.app.ui.messages

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.mongodb.app.R
import com.mongodb.app.TAG
import com.mongodb.app.data.blocking_censoring.MockBlockingCensoringData
import com.mongodb.app.data.messages.LONG_MESSAGE_CHARACTER_THRESHOLD
import com.mongodb.app.data.messages.MESSAGE_WIDTH_WEIGHT
import com.mongodb.app.data.messages.MessagesUserAction
import com.mongodb.app.domain.FriendMessage
import com.mongodb.app.navigation.Routes
import com.mongodb.app.presentation.blocking_censoring.BlockingViewModel
import com.mongodb.app.presentation.blocking_censoring.CensoringViewModel
import com.mongodb.app.presentation.blocking_censoring.censor
import com.mongodb.app.presentation.messages.MessagesViewModel
import com.mongodb.app.ui.blocking_censoring.BlockingContextualMenu
import com.mongodb.app.ui.theme.MessageColorMine
import com.mongodb.app.ui.theme.MessageColorOther
import com.mongodb.app.ui.theme.MessageInputBackgroundColor
import com.mongodb.app.ui.theme.MyApplicationTheme
import com.mongodb.app.ui.theme.Purple200
import java.util.Calendar
import java.util.Date
import java.util.SortedSet


/*
Contributions:
- Kevin Kubota (everything in this file, except for potential screen navigation logic)
 */


// region Extensions
val String.Companion.empty: String
    get() { return "" }
// endregion Extensions


// region Functions
/**
 * Displays the entire messages screen
 */
@Composable
fun MessagesScreenLayout(
    navController: NavHostController,
    messagesViewModel: MessagesViewModel,
    conversationUsersInvolved: SortedSet<String>,
    blockingViewModel: BlockingViewModel,
    censoringViewModel: CensoringViewModel,
    modifier: Modifier = Modifier
) {
    messagesViewModel.updateUsersInvolved(
        usersInvolved = conversationUsersInvolved
    )
    blockingViewModel.updateUserInFocus(
        userIdInFocus = messagesViewModel.otherUserProfileId.value
    )
    censoringViewModel.updateShouldCensorTextState()

    Scaffold(
        topBar = {
            MessagesTopBar(
                navController = navController,
                messagesViewModel = messagesViewModel,
                blockingViewModel = blockingViewModel,
                censoringViewModel = censoringViewModel,
                userIdInFocus = messagesViewModel.otherUserProfileId.value
            )
        },
        modifier = modifier
        // Pad the body of content so it does not get cut off by the scaffold top bar
    ) { innerPadding ->
        // If the current user has the other user blocked, show a different UI
        // However, do not show this for the blocked user for privacy reasons
        // ... (Do not want users to know someone has blocked them)
        if (blockingViewModel.isUserInFocusBlocked){
            MessagesBlockedNotifier(
                modifier = Modifier
                    .padding(innerPadding)
            )
        }
        else{
            MessagesBodyContent(
                messagesViewModel = messagesViewModel,
                censoringViewModel = censoringViewModel,
                modifier = Modifier
                    .padding(innerPadding)
            )
        }
    }
}

@Composable
fun MessagesBlockedNotifier(
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier
            .fillMaxSize()
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    all = dimensionResource(id = R.dimen.messages_screen_user_blocked_notifier_all_padding)
                )
        ){
            Text(
                text = stringResource(id = R.string.messages_screen_user_blocked_notifier)
            )
        }
    }
}

/**
 * Displays the top bar for the compass screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesTopBar(
    navController: NavHostController,
    messagesViewModel: MessagesViewModel,
    blockingViewModel: BlockingViewModel,
    censoringViewModel: CensoringViewModel,
    /* The other user's ID involved in the current conversation */
    userIdInFocus: String,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // Back button
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier
                        .clickable {
                            // TODO Need to change this to navigate to friends screen instead
                            navController.navigate(Routes.UserProfileScreen.route)
                        }
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.default_user_icon),
                        contentDescription = null,
                        // Crops the image to fit (the circular shape space)
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .size(dimensionResource(id = R.dimen.messages_screen_profile_picture_size))
                    )
                    Text(
                        text = messagesViewModel.otherUserProfileName.value,
                        color = Color.Black,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                BlockingContextualMenu(
                    userId = userIdInFocus,
                    blockingViewModel = blockingViewModel,
                    censoringViewModel = censoringViewModel
                )
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
    censoringViewModel: CensoringViewModel,
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
                // Exclude empty messages from being shown in the message history
                // Namely, the initial message when creating a conversation object will not be shown
                if (friendMessage.message.isNotBlank() && friendMessage.message.isNotEmpty()){
                    SingleMessageContainer(
                        friendMessage = friendMessage,
                        isSenderMe = messagesViewModel.isMessageMine(friendMessage),
                        messagesViewModel = messagesViewModel,
                        censoringViewModel = censoringViewModel
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
            .padding(
                top = dimensionResource(id = R.dimen.messages_screen_message_history_message_vertical_padding),
                bottom = dimensionResource(id = R.dimen.messages_screen_message_history_message_vertical_padding)
            )
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
    censoringViewModel: CensoringViewModel,
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
                    censoringViewModel = censoringViewModel,
                    modifier = Modifier
                )
                Row(
                    horizontalArrangement = rowArrangement,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ){
                    // Row under message that contains contextual menu and any necessary labels
                    if (isSenderMe){
                        MessagesExtrasLabel(
                            messagesViewModel = messagesViewModel,
                            friendMessage = friendMessage
                        )
                    }
                    MessagesContextualMenu(
                        friendMessage = friendMessage,
                        isSenderMe = isSenderMe,
                        messagesViewModel = messagesViewModel
                    )
                    if (!isSenderMe){
                        MessagesExtrasLabel(
                            messagesViewModel = messagesViewModel,
                            friendMessage = friendMessage
                        )
                    }
                }
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
    censoringViewModel: CensoringViewModel,
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
            text = if (censoringViewModel.isCensoringText.value){
                message.censor(censoringViewModel.profanityListAll)
            } else{
                message
                  },
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
 * Shows some extra information about a message via a label
 */
@Composable
fun MessagesExtrasLabel(
    friendMessage: FriendMessage,
    messagesViewModel: MessagesViewModel,
    modifier: Modifier = Modifier
){
    // Get the original message that the current specified message is replying to
    // This will be either the original message or null if it doesn't exist
    val originalMessage = messagesViewModel.messageIdRepliesToOriginalMessages[friendMessage._id]
    val originalMessageShortened: String = if (originalMessage == null){
        stringResource(id = R.string.messages_screen_message_reference_invalid)
    }
    else if (originalMessage.length <= LONG_MESSAGE_CHARACTER_THRESHOLD){
        stringResource(id = R.string.messages_screen_message_reference_short,
            originalMessage)
    }
    else{
        stringResource(id = R.string.messages_screen_message_reference_long,
            originalMessage.substring(0, LONG_MESSAGE_CHARACTER_THRESHOLD))
    }

    // Label showing that a message has been edited and is a reply
    val text = if (friendMessage.isUpdated() && friendMessage.isAReply()){
        stringResource(id = R.string.messages_screen_updated_and_replied_message_label,
            originalMessageShortened)
    }
    // Label showing that a message has been edited
    else if (friendMessage.isUpdated()){
        stringResource(id = R.string.messages_screen_updated_message_label)
    }
    // Label showing that a message is a reply to another message
    else if (friendMessage.isAReply()){
        stringResource(id = R.string.messages_screen_replied_message_label,
            originalMessageShortened)
    }
    // Label showing that a message neither has been edited or is a reply
    else{
        String.empty
    }
    Log.i(
        "TAG()",
        "MessagesScreen: Friend message = \"${friendMessage._id}\" " +
                "replied to message with ID = \"${friendMessage.messageIdRepliedTo}\" " +
                "and it has message = \"${originalMessage}\""
    )
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        modifier = modifier
            .padding(start = dimensionResource(id = R.dimen.messages_screen_extras_label_start_padding))
    )
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
//        IconButton(
//            onClick = {
//                isContextualMenuOpen = true
//            }
//        ) {
//            Icon(
//                imageVector = Icons.Default.MoreVert,
//                contentDescription = "Open Contextual Menu"
//            )
//        }
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "Open Contextual Menu",
            modifier = Modifier
                .clickable {
                    isContextualMenuOpen = true
                }
        )

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
                    all = dimensionResource(id = R.dimen.messages_screen_reply_update_card_all_padding)
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
                        top = dimensionResource(id = R.dimen.messages_screen_reply_update_row_vertical_padding),
                        start = dimensionResource(id = R.dimen.messages_screen_reply_update_row_horizontal_padding),
                        end = dimensionResource(id = R.dimen.messages_screen_reply_update_row_horizontal_padding),
                        bottom = dimensionResource(id = R.dimen.messages_screen_reply_update_row_vertical_padding)
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
            // Cancel action is via an alert dialog, not this button
            MessagesUserAction.DELETE -> Icons.Default.Refresh
            MessagesUserAction.REPLY -> Icons.Default.Cancel
        }
        // Button to either refresh message history or cancel a contextual menu action
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
                // Make the text field fill the remaining middle space between the
                // ... refresh/cancel and send buttons
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
        Column {
            Text(
                text = "Epoch = \n\"${MockBlockingCensoringData.mockMessagesViewModel.getCurrentTime()}\""
            )
            Text(
                text = "Zoned = \n\"${MockBlockingCensoringData.mockMessagesViewModel.getZonedDateTimeFromEpochTime(
                    MockBlockingCensoringData.mockMessagesViewModel.getCurrentTime()
                )}\""
            )
            Text(
                text = "Universal = \n\"${MockBlockingCensoringData.mockMessagesViewModel.getUniversalTime()}\""
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MessagesScreenLayoutPreview() {
    MyApplicationTheme {
        MessagesScreenLayout(
            navController = rememberNavController(),
            messagesViewModel = MessagesViewModel(
                repository = MockBlockingCensoringData.mockRepository,
                messagesRepository = MockBlockingCensoringData.mockMessagesRepository,
                conversationsRepository = MockBlockingCensoringData.mockConversationRepository
            ),
            conversationUsersInvolved = sortedSetOf(String.empty),
            blockingViewModel = MockBlockingCensoringData.mockBlockingViewModel,
            censoringViewModel = MockBlockingCensoringData.mockCensoringViewModel
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SingleMessageContainerPreview() {
    MyApplicationTheme {
        Column {
            val testMessages = listOf(
                stringResource(id = R.string.user_profile_test_string),
                stringResource(id = R.string.user_profile_test_string),
                "a",
                "z"
            )

            for (index in testMessages.indices){
                SingleMessageContainer(
                    friendMessage = FriendMessage().apply {
                        message = testMessages[index]
                    },
                    // Make every 2nd message mine
                    isSenderMe = index % 2 == 1,
                    messagesViewModel = MockBlockingCensoringData.mockMessagesViewModel,
                    censoringViewModel = MockBlockingCensoringData.mockCensoringViewModel
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun MessagesBlockedNotifierPreview(){
    MyApplicationTheme {
        MessagesBlockedNotifier()
    }
}
// endregion PreviewFunctions