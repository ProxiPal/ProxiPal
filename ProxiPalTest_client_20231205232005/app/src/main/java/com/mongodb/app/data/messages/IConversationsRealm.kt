package com.mongodb.app.data.messages

import com.mongodb.app.domain.FriendConversation
import io.realm.kotlin.Realm
import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.query.RealmQuery
import kotlinx.coroutines.flow.Flow
import java.util.SortedSet

/**
 * Contains necessary functions when working with [FriendConversation] fields
 */
interface IConversationsRealm {
    /**
     * Updates the realm instance subscriptions for conversations
     */
    suspend fun updateSubscriptionsConversations()

    /**
     * Returns a query to be added as a subscription to the realm instance
     */
    fun getQueryConversations(realm: Realm): RealmQuery<FriendConversation>

    /**
     * Adds a conversation object to the Atlas database
     */
    suspend fun addConversation(usersInvolved: SortedSet<String>)

    /**
     * Returns a list of all friend conversations across all users
     */
    fun getAllConversations(): Flow<ResultsChange<FriendConversation>>
}