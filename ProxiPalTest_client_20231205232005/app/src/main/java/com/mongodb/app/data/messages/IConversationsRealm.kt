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
     * Creates a [FriendConversation] Realm object
     */
    suspend fun createConversation(usersInvolved: SortedSet<String>)

    /**
     * Returns a specific conversation object if it exists
     */
    fun readConversation(usersInvolved: SortedSet<String>): Flow<ResultsChange<FriendConversation>>

    /**
     * Updates a [FriendConversation] object
     */
    suspend fun updateConversation(usersInvolved: SortedSet<String>, messageId: String, shouldAddMessage: Boolean)
}