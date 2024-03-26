package com.mongodb.app.data.messages

import com.mongodb.app.domain.FriendConversation
import com.mongodb.app.domain.FriendMessage
import io.realm.kotlin.Realm
import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.query.RealmQuery
import kotlinx.coroutines.flow.Flow
import org.mongodb.kbson.ObjectId
import java.util.SortedSet


const val SubscriptionNameMyFriendConversations = "MyConversations"

/**
 * Contains necessary functions when working with [FriendConversation] objects and the Atlas Realm
 */
interface IConversationsRealm {
    /**
     * Returns a query for finding all [FriendConversation]s the user is involved in
     */
    fun getQueryMyConversations(realm: Realm): RealmQuery<FriendConversation>

    /**
     * Returns a query for finding a specific [FriendConversation] with the specified users involved
     */
    fun getQuerySpecificConversation(realm: Realm, usersInvolved: SortedSet<String>): RealmQuery<FriendConversation>

    /**
     * Returns a query for finding a specific [FriendConversation] by its [ObjectId]
     */
    fun getQuerySpecificConversation(realm: Realm, conversationId: ObjectId): RealmQuery<FriendConversation>

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