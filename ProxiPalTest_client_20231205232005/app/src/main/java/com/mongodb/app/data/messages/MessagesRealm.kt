package com.mongodb.app.data.messages

import android.util.Log
import com.mongodb.app.TAG
import com.mongodb.app.app
import com.mongodb.app.domain.FriendMessage
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.realm.kotlin.mongodb.User
import io.realm.kotlin.mongodb.exceptions.SyncException
import io.realm.kotlin.mongodb.subscriptions
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import io.realm.kotlin.mongodb.sync.SyncSession
import io.realm.kotlin.mongodb.syncSession
import io.realm.kotlin.query.RealmQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

    suspend fun updateSubscriptions()

    fun getQueryMessages(realm: Realm): RealmQuery<FriendMessage>

    suspend fun addMessage(message: String, timeSent: Long)
}

class MessagesRealm(
    onSyncError: (session: SyncSession, error: SyncException) -> Unit
): IMessagesRealm {
    // region Variables
    private val _subscriptionNameOwnedMessages = "MySentMessages"
    private val _realm: Realm
    private val _config: SyncConfiguration
    // endregion Variables


    // region Properties
    private val currentUser: User
        get() = app.currentUser!!
    // endregion Properties

    init{
        Log.i(
            TAG(),
            "MessagesRealm: Start of init{}"
        )
//        Log.i(
//            TAG(),
//            "MessagesRealm: ${}"
//        )
        val schemaSet = setOf(FriendMessage::class)
        _config = SyncConfiguration.Builder(currentUser, schemaSet)
            .initialSubscriptions { realm ->
                add(
                    getQueryMessages(realm),
                    _subscriptionNameOwnedMessages
                )
            }
            .errorHandler { session: SyncSession, error: SyncException ->
                onSyncError.invoke(session, error)
            }
            .waitForInitialRemoteData()
            .build()

        _realm = Realm.open(_config)

        if (SHOULD_PRINT_REALM_CONFIG_INFO){
            // After configuration changes, realm stays the same but config changes every time
            // This leads to the app crashing when trying to interact with Realm after a configuration change
            Log.i(
                TAG(),
                "MessagesRealm: Realm = \"${_realm}\""
            )
            Log.i(
                TAG(),
                "MessagesRealm: Config = \"${_config}\""
            )
        }

        // Mutable states must be updated on the UI thread
        CoroutineScope(Dispatchers.Main).launch {
            _realm.subscriptions.waitForSynchronization()
            // Need to call this to make sure related subscriptions are added properly
            // ... otherwise this will result in an "RLM_ERR_NO_SUBSCRIPTION_FOR_WRITE" error
            updateSubscriptions()
        }
    }


    // region Functions
    override fun pauseSync() {
        _realm.syncSession.pause()
    }

    override fun resumeSync() {
        _realm.syncSession.resume()
    }

    override fun close() = _realm.close()

    override suspend fun updateSubscriptions() {
        _realm.subscriptions.update {
            add(getQueryMessages(_realm), _subscriptionNameOwnedMessages)
        }
        if (SHOULD_PRINT_REALM_CONFIG_INFO){
            for (subscription in _realm.subscriptions){
                Log.i(
                    TAG(),
                    "MessagesRealm: Subscription = \"${subscription}\""
                )
            }
        }
    }

    override fun getQueryMessages(realm: Realm): RealmQuery<FriendMessage>{
        return realm.query("ownerId == $0", currentUser.id)
    }

    override suspend fun addMessage(message: String, timeSent: Long){
        val friendMessage = FriendMessage().apply {
            ownerId = currentUser.id
            this.message = message
            this.timeSent = timeSent
        }
        _realm.write {
            copyToRealm(friendMessage, updatePolicy = UpdatePolicy.ALL)
        }
    }
    // endregion Functions
}

class MockMessagesRealm : IMessagesRealm{
    override fun pauseSync() {
        TODO("Not yet implemented")
    }

    override fun resumeSync() {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }

    override suspend fun updateSubscriptions() {
        TODO("Not yet implemented")
    }

    override fun getQueryMessages(realm: Realm): RealmQuery<FriendMessage> {
        TODO("Not yet implemented")
    }

    override suspend fun addMessage(message: String, timeSent: Long) {
        TODO("Not yet implemented")
    }
}