package com.mongodb.app.ui.messages

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
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
import com.mongodb.app.data.messages.MESSAGE_WIDTH_WEIGHT
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
- Show the most recent message for each corresponding conversation in the friends screen
| Add functionality where new messages in the friends screen are bolded if their time sent is more recent than the time
... you last read that conversation (timeRead will be a new field and will get updated when the user either opens or exits
... out of viewing the conversation.)
- Make changes to both friend profile picture and IDs of users involved
... when navigating from friends screen to messages screen
- Add ability to delete a message
- (Maybe) Add ability to reply to a message
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
            this)
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
                        "\"${messagesViewModel.currentMessages.toList().size}\"; " +
                        "Alt message amount = " +
                        "\"${messagesViewModel.messagesListState.size}\""
            )

            // Uses "multiple query" method in MessagesViewModel
//            val friendMessages = messagesViewModel.currentMessages.toList()
            // Uses "single query" method in MessagesViewModel
            // As of now, keep using this method (see comments in MessagesViewModel for more)
            val friendMessages = messagesViewModel.messagesListState.toList()
            items(friendMessages.reversed()) {
                friendMessage ->
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
                if (friendMessages.indexOf(friendMessage) == 0){
                    MessagesEndOfHistory()
                }
            }
        }
        MessagesInputRow(
            messagesViewModel = messagesViewModel,
            modifier = Modifier
        )
    }
}

/**
 * Shows a single line of text to designate the end of the user's message history
 */
@Composable
fun MessagesEndOfHistory(
    modifier: Modifier = Modifier
){
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp)
    ){
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
//                SingleMessageExtras(
//                    isSenderMe = isSenderMe,
//                    messagesViewModel = messagesViewModel,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                )
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
 * Shows tiny details below the actual message, such as the contextual menu
 */
@Composable
fun SingleMessageExtras(
    friendMessage: FriendMessage,
    isSenderMe: Boolean,
    messagesViewModel: MessagesViewModel,
    modifier: Modifier = Modifier,
){
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
    ){
        // Show contextual menu entry point on the left
        if (!isSenderMe){
            MessagesContextualMenu(
                friendMessage = friendMessage,
                isSenderMe = isSenderMe,
                messagesViewModel = messagesViewModel
            )
        }
        // The "sent by" message label
        Text(
            text =
            if (isSenderMe) stringResource(id = R.string.messages_screen_sent_by_me)
            else stringResource(id = R.string.messages_screen_sent_by_other, "someone"),
            style = MaterialTheme.typography.labelSmall
        )
        // Show contextual menu entry point on the right
        if (isSenderMe){
            MessagesContextualMenu(
                friendMessage = friendMessage,
                isSenderMe = isSenderMe,
                messagesViewModel = messagesViewModel
            )
        }
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
){
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
            if (isSenderMe){
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(id = R.string.messages_screen_update_message)
                        )
                    },
                    onClick = {
                        messagesViewModel.updateMessageStart()
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(id = R.string.messages_screen_delete_message)
                        )
                    },
                    onClick = { 
                        messagesViewModel.deleteMessage(
                            friendMessage = friendMessage
                        )
                    }
                )
            }
            // Show only an option to reply to another user's messages
            else{
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(id = R.string.messages_screen_reply_message)
                        )
                    },
                    onClick = {
                        messagesViewModel.replyMessageStart()
                    }
                )
            }
        }
    }
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
        // Button to refresh message history
        IconButton(
            onClick = { messagesViewModel.refreshMessages() }
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
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
        // Button to send message
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
fun TimePreview(){
    MyApplicationTheme {
        // There are many established ways online to get the system time as a number
        // ... but using Calendar.getInstance() might be the most common answer
        Column{
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
        Column(){
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