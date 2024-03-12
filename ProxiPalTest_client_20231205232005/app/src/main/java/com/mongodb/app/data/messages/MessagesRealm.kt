package com.mongodb.app.data.messages

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
import io.realm.kotlin.query.RealmQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface IMessagesRealm{
    fun getQueryMessages(): RealmQuery<FriendMessage>

    suspend fun addMessage(message: String, timeSent: Long)
}

class MessagesRealm(
    onSyncError: (session: SyncSession, error: SyncException) -> Unit
): IMessagesRealm {
    // region Variables
    private val realm: Realm
    private val config: SyncConfiguration
    private val currentUser: User
        get() = app.currentUser!!
    // endregion Variables


    init{
        val schemaSet = setOf(FriendMessage::class)
        config = SyncConfiguration.Builder(currentUser, schemaSet)
            .initialSubscriptions { realm ->
                add(
                    getQueryMessages()
                )
            }
            .errorHandler { session: SyncSession, error: SyncException ->
                onSyncError.invoke(session, error)
            }
            .waitForInitialRemoteData()
            .build()

        realm = Realm.open(config)

//        // After configuration changes, realm stays the same but config changes every time
//        // This leads to the app crashing when trying to interact with Realm after a configuration change
//        Log.i(
//            TAG(),
//            "RealmSyncRepository: Realm = \"${realm}\""
//        )
//        Log.i(
//            TAG(),
//            "RealmSyncRepository: Config = \"${config}\""
//        )

        // Mutable states must be updated on the UI thread
        CoroutineScope(Dispatchers.Main).launch {
            realm.subscriptions.waitForSynchronization()
        }
    }


    // region Functions
    override fun getQueryMessages(): RealmQuery<FriendMessage>{
        return realm.query("ownerId == $0", currentUser.id)
    }

    override suspend fun addMessage(message: String, timeSent: Long){
        val friendMessage = FriendMessage().apply {
            ownerId = currentUser.id
            this.message = message
            this.timeSent = timeSent
        }
        realm.write {
            copyToRealm(friendMessage, updatePolicy = UpdatePolicy.ALL)
        }
    }
    // endregion Functions
}

class MockMessagesRealm : IMessagesRealm{
    override fun getQueryMessages(): RealmQuery<FriendMessage> {
        TODO("Not yet implemented")
    }

    override suspend fun addMessage(message: String, timeSent: Long) {
        TODO("Not yet implemented")
    }
}