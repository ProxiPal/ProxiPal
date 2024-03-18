package com.mongodb.app.data.messages

import com.mongodb.app.domain.FriendMessage
import io.realm.kotlin.Realm
import io.realm.kotlin.query.RealmQuery

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

    override suspend fun addMessage(message: String, timeSent: Long) {
        TODO("Not yet implemented")
    }
}