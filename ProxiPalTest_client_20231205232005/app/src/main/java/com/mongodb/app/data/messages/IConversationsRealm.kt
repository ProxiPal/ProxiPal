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
 * Contains necessary functions when working with [FriendConversation] objects and the Atlas Realm
 */
interface IConversationsRealm {
    /**
     * Updates the realm instance subscriptions for conversations
     */
    suspend fun updateRealmSubscriptionsConversations()

    /**
     * Returns a query to be added as a subscription to the realm instance
     */
    fun getRealmQueryMyConversations(realm: Realm): RealmQuery<FriendConversation>

    /**
     * Creates a [FriendConversation] object for the specified users involved
     */
    suspend fun createConversation(usersInvolved: SortedSet<String>)

    /**
     * Returns a [FriendConversation] object, if it exists for the specified users involved
     */
    fun readConversation(usersInvolved: SortedSet<String>): Flow<ResultsChange<FriendConversation>>

    /**
     * Updates a [FriendConversation] object by adding a reference to a [FriendMessage]
     */
    suspend fun updateConversationAdd(friendConversation: FriendConversation, messageId: ObjectId)

    /**
     * Updates a [FriendConversation] object by removing a reference to a [FriendMessage]
     */
    suspend fun updateConversationRemove(friendConversation: FriendConversation, messageId: ObjectId)
}