package com.mongodb.app.data.messages

import com.mongodb.app.domain.FriendConversation
import com.mongodb.app.domain.FriendMessage
import io.realm.kotlin.Realm
import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.query.RealmQuery
import kotlinx.coroutines.flow.Flow

/**
 * A mock class to be used in preview Composable functions
 */
class MockMessagesRepository : IMessagesRealm{
    override suspend fun updateSubscriptionsMessages() {
        TODO("Not yet implemented")
    }

    override fun getQueryMessages(realm: Realm): RealmQuery<FriendMessage> {
        TODO("Not yet implemented")
    }

    override suspend fun createMessage(newMessage: FriendMessage) {
        TODO("Not yet implemented")
    }

    override fun readMessage(id: String): Flow<ResultsChange<FriendMessage>> {
        TODO("Not yet implemented")
    }

    override fun readConversationMessages(friendConversation: FriendConversation): Flow<ResultsChange<FriendMessage>> {
        TODO("Not yet implemented")
    }
}