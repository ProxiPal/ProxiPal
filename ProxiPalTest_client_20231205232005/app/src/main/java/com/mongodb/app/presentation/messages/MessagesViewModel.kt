package com.mongodb.app.presentation.messages

import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.mongodb.app.TAG
import com.mongodb.app.data.SyncRepository
import com.mongodb.app.data.messages.IConversationsRealm
import com.mongodb.app.data.messages.IMessagesRealm
import com.mongodb.app.domain.FriendConversation
import com.mongodb.app.domain.FriendMessage
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.notifications.InitialResults
import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.notifications.UpdatedResults
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
import java.util.Calendar
import java.util.Date
import java.util.SortedSet


// region Extensions
fun String.toObjectId(): ObjectId {
    return ObjectId(this)
}
// endregion Extensions


class MessagesViewModel(
    private var repository: SyncRepository,
    var messagesRepository: IMessagesRealm,
    private var conversationsRepository: IConversationsRealm
) : ViewModel(){
    // region Variables
    private val _message = mutableStateOf("")
    private var _usersInvolved: SortedSet<String> = sortedSetOf("")
    private val _messagesListState: SnapshotStateList<FriendMessage> = mutableStateListOf()
    private val _conversationsListState: SnapshotStateList<FriendConversation> = mutableStateListOf()
    private var _currentConversation: FriendConversation? = null
    private val _currentMessages: MutableList<FriendMessage> = mutableListOf()
    // endregion Variables


    // region Properties
    val message
        get() = _message
    val messagesListState
        get() = _messagesListState
    val conversationsListState
        get() = _conversationsListState
    val currentConversation
        get() = _currentConversation
    val currentMessages
        get() = _currentMessages
    // endregion Properties


    // region Functions
    /**
     * Gets the messages from the latest conversation object
     */
    fun refreshMessages(){
        viewModelScope.launch {
            // This method only works if running the below method with it
//            readMessagesMultipleQueries()
            // This method works by itself and alongside the above method
            readMessagesSingleQuery()
            // Code beyond this point does not get called
            Log.i(
                TAG(),
                "MessagesViewModel: Finished reading messages using any combination of methods"
            )
        }
    }

    fun updateMessage(newMessage: String){
        _message.value = newMessage
    }

    private fun resetMessage(){
        _message.value = ""
    }

    /**
     * Returns the amount of ms since the epoch time
     */
    private fun getCurrentTime(): Long{
        return Calendar.getInstance().timeInMillis
    }

    /**
     * Returns a [Date] object given how many milliseconds since the epoch time
     */
    private fun getDateFromTime(time: Long): Date{
        return Date(time)
    }

    fun sendMessage(){
        viewModelScope.launch {
            createMessage()
            resetMessage()
            // Refresh the conversation object instance to the one saved in the database
            // This allows keeping updated with the latest messages (namely the one just sent)
            readConversation()
            // Code beyond this point does not get called
        }
    }


    // region Messages
    /**
     * Adds a [FriendMessage] object to the database
     */
    private suspend fun createMessage(){
        val newMessage = FriendMessage()
            .also {
                it.message = message.value
                it.timeSent = getCurrentTime()
                it.ownerId = repository.getCurrentUserId()
            }
        Log.i(
            TAG(),
            "MessagesViewModel: New message ID = \"${newMessage._id.toHexString()}\"; " +
                    "Message = \"${newMessage.message}\""
        )

        messagesRepository.createMessage(
            newMessage = newMessage
        )
        // Add a reference to the new message in the corresponding conversation object
        conversationsRepository.updateConversation(
            usersInvolved = _usersInvolved,
            // Use .toHexString() instead of .toString()
            messageId = newMessage._id.toHexString(),
            shouldAddMessage = true
        )
    }

    /**
     * Gets the list of [FriendMessage] objects for the current [FriendConversation]
     * using multiple RQL queries.
     * (Iterates through all referenced message IDs and queries messages one by one that
     * correspond to each ID. Uses [IMessagesRealm.readMessage])
     */
    private suspend fun readMessagesMultipleQueries() {
        currentMessages.clear()
        if (currentConversation != null){
            CoroutineScope(Dispatchers.IO).async {
                for (messageId in currentConversation!!.messagesSent){
                    val messageFlow: Flow<ResultsChange<FriendMessage>> =
                        messagesRepository.readMessage(messageId.toObjectId())
                    // Use .first instead of .collect
                    // Otherwise only the 1st message will be retrieved
                    // ... since .collect does not terminate automatically (?)
                    messageFlow.first{
                        currentMessages.addAll(it.list)
                    }
                }
            }
                // Wait until all the messages have been retrieved
                .await()
        }
        Log.i(
            TAG(),
            "MessagesViewModel: Finished reading messages using for loop over message references"
        )
    }

    /**
     * Gets the list of [FriendMessage] objects for the current [FriendConversation]
     * using a single RQL query.
     * (Iterates through all messages in the database and queries if its ID is in the list
     * of referenced message IDs. Uses [IMessagesRealm.readConversationMessages])
     */
    private suspend fun readMessagesSingleQuery(){
        messagesRepository.readConversationMessages(currentConversation!!)
            .collect{
                messagesListState.clear()
                messagesListState.addAll(it.list)
                Log.i(
                    TAG(),
                    "MessagesViewModel: TEMP; conversation messages = " +
                            "\"${currentConversation!!.messagesSent}\"; " +
                            "state list messages = \"${it.list}\""
                )

                Log.i(
                    TAG(),
                    "MessagesViewModel: (0) Finished reading messages using \"a IN b\" RQL query"
                )
                return@collect
            }
        Log.i(
            TAG(),
            "MessagesViewModel: (1) Finished reading messages using \"a IN b\" RQL query"
        )
        withContext(Dispatchers.Main){
            Log.i(
                TAG(),
                "MessagesViewModel: (2) Finished reading messages using \"a IN b\" RQL query"
            )
            return@withContext
        }
        Log.i(
            TAG(),
            "MessagesViewModel: (3) Finished reading messages using \"a IN b\" RQL query"
        )
        return
    }

    /**
     * Starts the process for updating a [FriendMessage] in the database
     */
    fun updateMessageStart(){
        // TODO
    }

    /**
     * Removes a [FriendMessage] object from the database
     */
    fun deleteMessage(friendMessage: FriendMessage){
        viewModelScope.launch {
            // Do not manually remove message ID reference from conversation object
            // Results in an error saying you cannot modify list outside of write transaction
            messagesRepository.deleteMessage(
                messageId = friendMessage._id
            )
            conversationsRepository.updateConversation(
                usersInvolved = _usersInvolved,
                // Use .toHexString() instead of .toString()
                messageId = friendMessage._id.toHexString(),
                shouldAddMessage = false
            )
        }
    }

    /**
     * Checks if a specific message was sent by the current user
     */
    fun isMessageMine(message: FriendMessage): Boolean{
        return message.ownerId == repository.getCurrentUserId()
    }

    /**
     * Starts the process for replying to a message
     */
    fun replyMessageStart(){
        // TODO
    }
    // endregion Messages


    // region Conversations
    /**
     * Adds a [FriendConversation] object to the database
     */
    private suspend fun createConversation(){
        Log.i(
            TAG(),
            "MessagesViewModel: Conversation users involved = \"${_usersInvolved}\""
        )
        conversationsRepository.createConversation(
            usersInvolved = _usersInvolved
        )
    }

    /**
     * Gets the current [FriendConversation] object, if it exists
     */
    private suspend fun readConversation(){
        // This logic is copied from the UserProfileViewModel class
        conversationsRepository.readConversation(_usersInvolved)
            .collect {
                    event: ResultsChange<FriendConversation> ->
                when (event){
                    is InitialResults -> {
                        conversationsListState.clear()
                        conversationsListState.addAll(event.list)
                        Log.i(
                            TAG(),
                            "MessagesViewModel: Current conversation amount = \"" +
                                    "${event.list.size}\""
                        )
                        when (event.list.size) {
                            1 -> {
                                _currentConversation = event.list[0]
                            }
                            0 -> {
                                // Before creating and saving a message in the database
                                // ... check if there is the corresponding conversation object
                                // ... to store a reference to the message
                                // If not, create the conversation object first
                                // If it exists already, get the corresponding conversation object
                                createConversation()
                            }
                            else -> {
                                Log.i(
                                    TAG(),
                                    "MessagesViewModel: Too many found current conversations; Skipping..."
                                )
                            }
                        }
                        if (currentConversation != null){
                            Log.i(
                                TAG(),
                                "MessagesViewModel: Current conversation = \"${currentConversation!!._id}\" has " +
                                        "\"${currentConversation!!.messagesSent.size}\" messages sent and" +
                                        " users involved = \"${currentConversation!!.usersInvolved}\""
                            )
                        }
                    }
                    is UpdatedResults -> {
                        if (event.deletions.isNotEmpty() && conversationsListState.isNotEmpty()) {
                            event.deletions.reversed().forEach {
                                conversationsListState.removeAt(it)
                            }
                        }
                        if (event.insertions.isNotEmpty()) {
                            event.insertions.forEach {
                                conversationsListState.add(it, event.list[it])
                            }
                        }
                        if (event.changes.isNotEmpty()) {
                            event.changes.forEach {
                                conversationsListState.removeAt(it)
                                conversationsListState.add(it, event.list[it])
                            }
                        }
                    }
                    else -> Unit // No-op
                }
            }
    }

    /**
     * Updates a [FriendConversation]'s users involved field
     */
    fun updateUsersInvolved(usersInvolved: SortedSet<String>){
        _usersInvolved = usersInvolved
        // Get the corresponding conversation object with the given users involved
        viewModelScope.launch {
            readConversation()
        }
    }
    // endregion Functions


    // Allows instantiating an instance of itself
    companion object {
        fun factory(
            repository: SyncRepository,
            messagesRealm: IMessagesRealm,
            conversationsRealm: IConversationsRealm,
            owner: SavedStateRegistryOwner,
            defaultArgs: Bundle? = null
        ): AbstractSavedStateViewModelFactory {
            return object : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    // Remember to change the cast to the class name this code is in
                    return MessagesViewModel (repository, messagesRealm, conversationsRealm) as T
                }
            }
        }
    }
}