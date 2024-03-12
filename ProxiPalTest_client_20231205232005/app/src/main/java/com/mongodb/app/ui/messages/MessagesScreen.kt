package com.mongodb.app.ui.messages

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.mongodb.app.data.messages.MOCK_MESSAGE_LIST
import com.mongodb.app.data.messages.MessagesRealm
import com.mongodb.app.data.messages.MockMessagesRealm
import com.mongodb.app.presentation.messages.MessagesViewModel
import com.mongodb.app.presentation.tasks.ToolbarViewModel
import com.mongodb.app.ui.theme.MyApplicationTheme
import com.mongodb.app.ui.theme.Purple200
import kotlinx.coroutines.launch

class MessagesScreen : ComponentActivity(){
    // region Variables
    private val repository = RealmSyncRepository { _, _ ->
        lifecycleScope.launch {
        }
    }

    private val messagesRealm = MessagesRealm { _, _ ->
        lifecycleScope.launch {
        }
    }

    private val messagesViewModel: MessagesViewModel by viewModels {
        MessagesViewModel.factory(messagesRealm, this)
    }

    private val toolbarViewModel: ToolbarViewModel by viewModels {
        ToolbarViewModel.factory(repository, this)
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
                messagesViewModel = messagesViewModel,
                toolbarViewModel = toolbarViewModel,
//                navController =
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
    toolbarViewModel: ToolbarViewModel,
//    navController: NavHostController,
    modifier: Modifier = Modifier
){
    Scaffold(
        topBar = {
//            TaskAppToolbar(
//                viewModel = toolbarViewModel,
////                navController = navController
//            )
            MessagesTopBar()
        },
        modifier = modifier
        // Pad the body of content so it does not get cut off by the scaffold top bar
    ) { innerPadding ->
        MessagesBodyContent(
            messagesViewModel = messagesViewModel,
            modifier = Modifier
                .padding(innerPadding)
//                .absolutePadding(top = innerPadding.calculateTopPadding())
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
                Text(
                    text = stringResource(
                        id = R.string.app_name
                    ),
                    color = Color.White,
                    style = MaterialTheme.typography.displayMedium
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
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier
            .fillMaxSize()
    ){
        LazyColumn(
            // Start at the bottom, then scroll up to show older messages
            reverseLayout = true,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ){
            // TODO Replace String with a custom Realm message class
            val messageList = MOCK_MESSAGE_LIST
            // Start with the more recent messages at the bottom
            items(messageList.reversed()){ message ->
                SingleMessage(
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
fun SingleMessage(
    isSenderMe: Boolean,
    message: String,
    modifier: Modifier = Modifier
){
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
            .padding(top = 4.dp, bottom = 4.dp)
    ){
        if (isSenderMe){
            Spacer(
                modifier = Modifier
                    .weight(MESSAGE_WIDTH_WEIGHT)
            )
        }
        Row(
            horizontalArrangement = rowArrangement,
            modifier = Modifier
                .weight(1 - MESSAGE_WIDTH_WEIGHT)
        ){
            Column(
                horizontalAlignment = columnAlignment,
                modifier = Modifier
            ){
                Card(
                    modifier = Modifier
                ){
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        softWrap = true,
                        overflow = TextOverflow.Clip,
                        modifier = Modifier
                            .padding(8.dp)
                    )
                }
                Text(
                    text =
                    if (isSenderMe) stringResource(id = R.string.messages_screen_sent_by_me)
                    else stringResource(id = R.string.messages_screen_sent_by_other, "someone"),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        if (!isSenderMe){
            Spacer(
                modifier = Modifier
                    .weight(MESSAGE_WIDTH_WEIGHT)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesInputRow(
    messagesViewModel: MessagesViewModel,
    modifier: Modifier = Modifier
){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
    ){
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
                .weight(0.80f)
        )
        Icon(
            painter = painterResource(id = R.drawable.tempcompass),
            contentDescription = "Send message",
            modifier = Modifier
                .weight(0.20f)
                .clickable {
                    messagesViewModel.sendMessage()
                }
        )
    }
}
// endregion Functions


// region PreviewFunctions
@Preview(showBackground = true)
@Composable
fun MessagesScreenLayoutPreview(){
    MyApplicationTheme {
        val repository = MockRepository()
        val mockRealm = MockMessagesRealm()
        MessagesScreenLayout(
            messagesViewModel = MessagesViewModel(mockRealm),
            toolbarViewModel = ToolbarViewModel(repository),
//            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MessagesTopBarPreview(){
    MyApplicationTheme {
        MessagesTopBar()
    }
}

@Preview(showBackground = true)
@Composable
fun MessagesBodyContentPreview(){
    MyApplicationTheme {
        val mockRealm = MockMessagesRealm()
        MessagesBodyContent(
            messagesViewModel = MessagesViewModel(mockRealm)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SingleMessagePreview(){
    MyApplicationTheme {
        SingleMessage(
            isSenderMe = true,
            message = stringResource(id = R.string.user_profile_test_string)
        )
    }
}
// endregion PreviewFunctions