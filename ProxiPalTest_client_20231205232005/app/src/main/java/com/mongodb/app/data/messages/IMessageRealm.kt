package com.mongodb.app.data.messages

import com.mongodb.app.domain.FriendMessage
import io.realm.kotlin.Realm
import io.realm.kotlin.query.RealmQuery

/**
 * Contains necessary functions when working with [FriendMessage] objects
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
     * Adds a message object to the Atlas database
     */
    suspend fun addMessage(message: String, timeSent: Long)
}