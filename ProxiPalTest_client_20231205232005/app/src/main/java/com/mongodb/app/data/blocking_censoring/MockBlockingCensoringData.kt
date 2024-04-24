package com.mongodb.app.data.blocking_censoring

import com.mongodb.app.data.MockRepository
import com.mongodb.app.data.messages.MockConversationRepository
import com.mongodb.app.data.messages.MockMessagesRepository
import com.mongodb.app.presentation.blocking_censoring.BlockingViewModel
import com.mongodb.app.presentation.blocking_censoring.CensoringViewModel
import com.mongodb.app.presentation.messages.MessagesViewModel


/**
 * For unifying the use of "mock" variables for specific preview composable functions
 */
class MockBlockingCensoringData {
    companion object{
        val mockRepository = MockRepository()

        val mockMessagesRepository = MockMessagesRepository()
        val mockConversationRepository = MockConversationRepository()
        val mockMessagesViewModel = MessagesViewModel(
            repository = mockRepository,
            messagesRepository = mockMessagesRepository,
            conversationsRepository = mockConversationRepository
        )

        val mockBlockingCensoringRealm = MockBlockingCensoringRealm()
        val mockBlockingViewModel = BlockingViewModel(
            repository = mockRepository,
            blockingCensoringRealm = mockBlockingCensoringRealm
        )
        val mockCensoringViewModel = CensoringViewModel(
            repository = mockRepository,
            blockingCensoringRealm = mockBlockingCensoringRealm,
            shouldReadCensoredTextOnInit = false
        )
    }
}