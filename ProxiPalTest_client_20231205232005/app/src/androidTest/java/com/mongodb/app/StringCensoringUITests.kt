package com.mongodb.app

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.compose.rememberNavController
import com.mongodb.app.data.MockRepository
import com.mongodb.app.data.SyncRepository
import com.mongodb.app.data.blocking_censoring.MockBlockingCensoringRealm
import com.mongodb.app.data.messages.MockConversationRepository
import com.mongodb.app.data.messages.MockMessagesRepository
import com.mongodb.app.presentation.blocking_censoring.BlockingViewModel
import com.mongodb.app.presentation.blocking_censoring.CensoringViewModel
import com.mongodb.app.presentation.messages.MessagesViewModel
import com.mongodb.app.ui.messages.MessagesScreenLayout
import com.mongodb.app.ui.theme.MyApplicationTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test

@Deprecated(
    message = "Currently does not work due to Realm related functions having to be implemented"
)
class StringCensoringUITests {

    @get: Rule
    val composeTestRule = createComposeRule()

    private val repository: SyncRepository = MockRepository()
    private val blockingCensoringRealm = MockBlockingCensoringRealm()
    private val messagesRepository = MockMessagesRepository()
    private val conversationsRepository = MockConversationRepository()

    private val messagesViewModel = MessagesViewModel(
        repository, messagesRepository, conversationsRepository
    )
    private val usersInvolved = sortedSetOf(
        "65e96193c6e205c32b0915cc", "6570119696faac878ad696a5"
    )
    private val blockingViewModel = BlockingViewModel(
        repository, blockingCensoringRealm
    )
    private val censoringViewModel = CensoringViewModel(
        repository, blockingCensoringRealm, false
    )


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun censorStrings(){
        composeTestRule.setContent {
            MyApplicationTheme {
                MessagesScreenLayout(
                    navController = rememberNavController(),
                    messagesViewModel = messagesViewModel,
                    conversationUsersInvolved = usersInvolved,
                    blockingViewModel = blockingViewModel,
                    censoringViewModel = censoringViewModel
                )
            }
        }
        composeTestRule.onNodeWithTag("blockingContextualMenu")
            .assertExists("Blocking contextual menu does not exist")
    }
}