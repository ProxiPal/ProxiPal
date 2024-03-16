package com.mongodb.app.ui.messages

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.mongodb.app.data.RealmSyncRepository
import com.mongodb.app.data.messages.MESSAGE_WIDTH_WEIGHT
import com.mongodb.app.data.messages.MOCK_MESSAGE_LIST
import com.mongodb.app.data.messages.MockConversationRepository
import com.mongodb.app.data.messages.MockMessagesRepository
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
- Update message UI
| Add message when at top of message history (something like "No more messages to load")
- Update database/collection workings
| Add collection for FriendConversations (current FriendMessage one will become FriendMessages)
    | Make a 1-to-many relationship from FriendConversations and FriendMessages (Each document is a single message,
    ... which prevents both parties of a conversation from editing the same document(s) at the same time.
    ... Also this is more ideal than directly embedding messages into a conversation as (1) Messages can get large if
    ... adding photos to messages and (2) There may be a lot of messages in a conversation)
- Add ability to add an image (see HomeScreen and HomeViewModel)
- Show the most recent message for each corresponding conversation in the friends screen
| Add functionality where new messages in the friends screen are bolded if their time sent is more recent than the time
... you last read that conversation (timeRead will be a new field and will get updated when the user either opens or exits
... out of viewing the conversation.)
- Add ability to tap and hold a message
| Add ability to delete a message
| Add ability to reply to a message
*/


class MessagesScreen : ComponentActivity() {
    // region Variables
    private val repository = RealmSyncRepository { _, _ ->
        lifecycleScope.launch {
        }
    }

    private val messagesViewModel: MessagesViewModel by viewModels {
        MessagesViewModel.factory(repository, repository, this)
    }
    // endregion Variables


    // region Functions
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i(
            TAG(),
            "MessagesScreen: Start of OnCreate()"
        )

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
            // Start at the bottom, then scroll up to show older messages
            reverseLayout = true,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // TODO Replace String with a custom Realm message class
            val messageList = MOCK_MESSAGE_LIST
            // Start with the more recent messages at the bottom
            items(messageList.reversed()) { message ->
                SingleMessageContainer(
                    // TODO Use custom Realm class to store who sent a message
                    isSenderMe = messageList.reversed().indexOf(message) % 2 == 1,
                    message = message
                )
            }
        }
        MessagesInputRow(
            messagesViewModel = messagesViewModel,
            modifier = Modifier
        )
    }
}

@Composable
fun SingleMessageContainer(
    message: String,
    isSenderMe: Boolean,
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
                    message = message,
                    isSenderMe = isSenderMe,
                    modifier = Modifier
                )
                // The "sent by" message label
                Text(
                    text =
                    if (isSenderMe) stringResource(id = R.string.messages_screen_sent_by_me)
                    else stringResource(id = R.string.messages_screen_sent_by_other, "someone"),
                    style = MaterialTheme.typography.labelSmall
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
        IconButton(
            onClick = { messagesViewModel.sendMessage() },
            modifier = Modifier
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
        val mockMessagesRepository = MockMessagesRepository()
        val mockConversationRepository = MockConversationRepository()
        MessagesScreenLayout(
            messagesViewModel = MessagesViewModel(
                messagesRealm = mockMessagesRepository,
                conversationsRealm = mockConversationRepository
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SingleMessageContainerPreview() {
    MyApplicationTheme {
        SingleMessageContainer(
            message = stringResource(id = R.string.user_profile_test_string),
            isSenderMe = true
        )
    }
}
// endregion PreviewFunctions