package com.mongodb.app.data.messages

import com.mongodb.app.domain.FriendConversation
import com.mongodb.app.domain.FriendMessage
import io.realm.kotlin.Realm
import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.query.RealmQuery
import kotlinx.coroutines.flow.Flow
import org.mongodb.kbson.ObjectId

/**
 * Contains necessary functions when working with [FriendMessage] objects and Atlas Realm
 */
interface IMessagesRealm{
    /**
     * Updates the realm instance subscriptions for messages
     */
    suspend fun updateSubscriptionsMessages()

    /**
     * Returns a query to be added as a subscription to the realm instance
     */
    fun getQueryMessages(realm: Realm): RealmQuery<FriendMessage>

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