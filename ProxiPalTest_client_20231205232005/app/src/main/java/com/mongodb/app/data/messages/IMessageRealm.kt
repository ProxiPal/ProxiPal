package com.mongodb.app.data.messages

import com.mongodb.app.domain.FriendConversation
import com.mongodb.app.domain.FriendMessage
import io.realm.kotlin.Realm
import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.query.RealmQuery
import kotlinx.coroutines.flow.Flow
import org.mongodb.kbson.ObjectId


const val SubscriptionNameMyMessages = "MyMessages"

const val SubscriptionNameCurrentConversationMessages = "CurrentConversationMessages"

/**
 * Contains necessary functions when working with [FriendMessage] objects and Atlas Realm
 */
interface IMessagesRealm{
    /**
     * Updates the realm instance subscriptions for a specific [FriendConversation]'s [FriendMessage]s
     */
    suspend fun updateSubscriptionsMessages(friendConversation: FriendConversation)

    /**
     * Returns a query for getting all [FriendMessage]s in a [FriendConversation]
     */
    fun getQueryConversationMessages(realm: Realm, friendConversation: FriendConversation): RealmQuery<FriendMessage>

    /**
     * Creates a [FriendMessage] object
     */
    suspend fun createMessage(newMessage: FriendMessage)

    /**
     * Gets a specific [FriendMessage] object
     */
    fun readMessage(messageId: ObjectId): Flow<ResultsChange<FriendMessage>>

    /**
     * Gets a list of [FriendMessage] objects from a [FriendConversation]
     */
    fun readConversationMessages(friendConversation: FriendConversation): Flow<ResultsChange<FriendMessage>>

    /**
     * Deletes a [FriendMessage]
     */
    suspend fun deleteMessage(messageId: ObjectId)
}