package com.mongodb.app.data.messages

import com.mongodb.app.domain.FriendConversation
import com.mongodb.app.domain.FriendMessage
import io.realm.kotlin.Realm
import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.query.RealmQuery
import kotlinx.coroutines.flow.Flow
import org.mongodb.kbson.ObjectId
import java.util.SortedSet


/**
 * A mock class to be used in preview Composable functions
 */
class MockConversationRepository: IConversationsRealm{
    override fun getQueryMyConversations(realm: Realm): RealmQuery<FriendConversation> {
        TODO("Not yet implemented")
    }

    override fun getQuerySpecificConversation(
        realm: Realm,
        usersInvolved: SortedSet<String>
    ): RealmQuery<FriendConversation> {
        TODO("Not yet implemented")
    }

    override fun getQuerySpecificConversation(
        realm: Realm,
        conversationId: ObjectId
    ): RealmQuery<FriendConversation> {
        TODO("Not yet implemented")
    }

    override suspend fun createConversation(usersInvolved: SortedSet<String>) {
        TODO("Not yet implemented")
    }

    override fun readConversation(usersInvolved: SortedSet<String>): Flow<ResultsChange<FriendConversation>> {
        TODO("Not yet implemented")
    }

    override suspend fun updateConversationAdd(
        friendConversation: FriendConversation,
        messageId: ObjectId
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun updateConversationRemove(
        friendConversation: FriendConversation,
        messageId: ObjectId
    ) {
        TODO("Not yet implemented")
    }
}