package com.mongodb.app.data.messages

import com.mongodb.app.domain.FriendConversation
import com.mongodb.app.domain.FriendMessage
import io.realm.kotlin.Realm
import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.query.RealmQuery
import kotlinx.coroutines.flow.Flow
import org.mongodb.kbson.ObjectId

/**
 * A mock class to be used in preview Composable functions
 */
class MockMessagesRepository : IMessagesRealm{
    override suspend fun updateRealmSubscriptionsMessages() {
        TODO("Not yet implemented")
    }

    override fun getRealmQueryMyMessages(realm: Realm): RealmQuery<FriendMessage> {
        TODO("Not yet implemented")
    }

    override fun getRealmQueryOthersMessages(realm: Realm): RealmQuery<FriendMessage> {
        TODO("Not yet implemented")
    }

    override suspend fun createMessage(newMessage: FriendMessage) {
        TODO("Not yet implemented")
    }

    override fun readMessage(messageId: ObjectId): Flow<ResultsChange<FriendMessage>> {
        TODO("Not yet implemented")
    }

    override fun readConversationMessages(friendConversation: FriendConversation): Flow<ResultsChange<FriendMessage>> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteMessage(messageId: ObjectId) {
        TODO("Not yet implemented")
    }
}