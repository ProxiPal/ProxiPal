package com.mongodb.app.data.messages

import com.mongodb.app.domain.FriendMessage
import io.realm.kotlin.Realm
import io.realm.kotlin.query.RealmQuery

/**
 * Contains necessary functions when working with message objects and the Atlas Realm
 */
interface IMessagesRealm{
    /**
     * Pauses synchronization with MongoDB. This is used to emulate a scenario of no connectivity.
     */
    fun pauseSync()

    /**
     * Resumes synchronization with MongoDB.
     */
    fun resumeSync()

    /**
     * Closes the realm instance held by this repository.
     */
    fun close()

    /**
     * Updates the realm instance subscriptions
     */
    suspend fun updateSubscriptions()

    /**
     * Returns a query to be added as a subscription to the realm instance
     */
    fun getQueryMessages(realm: Realm): RealmQuery<FriendMessage>

    /**
     * Adds a message object to the Atlas database
     */
    suspend fun addMessage(message: String, timeSent: Long)
}