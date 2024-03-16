package com.mongodb.app.data.messages

import com.mongodb.app.domain.FriendConversation
import io.realm.kotlin.Realm
import io.realm.kotlin.query.RealmQuery
import java.util.SortedSet


/**
 * A mock class to be used in preview Composable functions
 */
class MockConversationRepository: IConversationsRealm{
    override suspend fun updateSubscriptionsConversations() {
        TODO("Not yet implemented")
    }

    override fun getQueryConversations(realm: Realm): RealmQuery<FriendConversation> {
        TODO("Not yet implemented")
    }

    override suspend fun addConversation(usersInvolved: SortedSet<String>) {
        TODO("Not yet implemented")
    }
}