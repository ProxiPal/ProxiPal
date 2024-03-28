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
import com.mongodb.app.data.messages.MessagesUserAction
import com.mongodb.app.data.toObjectId
import com.mongodb.app.domain.FriendConversation
import com.mongodb.app.domain.FriendMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
import java.util.Calendar
import java.util.Date
import java.util.SortedSet


class MessagesViewModel(
    private var repository: SyncRepository,
    var messagesRepository: IMessagesRealm,
    private var conversationsRepository: IConversationsRealm
) : ViewModel(){
    // region Variables
    /**
     * For storing the current text the user is entering
     */
    private val _message = mutableStateOf("")
    private var _usersInvolved: SortedSet<String>? = null
    private val _messagesListState: SnapshotStateList<FriendMessage> = mutableStateListOf()
    private val _conversationsListState: SnapshotStateList<FriendConversation> = mutableStateListOf()
    private var _friendMessageUnderActionFocus: FriendMessage? = null
    private val _currentAction = mutableStateOf(MessagesUserAction.IDLE)

    /**
     * Maps the [ObjectId] of a [FriendMessage] reply to the message of the [FriendMessage] replying to
     */
    private val _messageIdRepliesToOriginalMessages = mutableMapOf<ObjectId, String?>()
    // endregion Variables


    // region Properties
    val message
        get() = _message
    val messagesListState
        get() = _messagesListState
    private val conversationsListState
        get() = _conversationsListState
    private val currentConversation: FriendConversation?
        get() {
            return if (_conversationsListState.size > 0) _conversationsListState[0]
            else null
        }
    val friendMessageUnderActionFocus
        get() = _friendMessageUnderActionFocus
    val currentAction
        get() = _currentAction
    val messageIdRepliesToOriginalMessages
        get() = _messageIdRepliesToOriginalMessages
    // endregion Properties


    // region Functions
    /**
     * Gets the messages from the latest conversation object
     */
    fun refreshMessages(){
        viewModelScope.launch {
            readConversation()
            Log.i(
                TAG(),
                "MessagesViewModel: Finished reading conversation = \"${currentConversation?._id}\""
            )
            readMessages()
        }
    }

    fun updateMessage(newMessage: String){
        _message.value = newMessage
    }

    private fun resetMessage(){
        _message.value = ""
    }


    // region DateTime
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
    // endregion DateTime


    fun sendMessage(){
        // Do not create a message if it does not contain anything
        if (message.value.isEmpty()){
            return
        }

        viewModelScope.launch {
            // Updates a message
            if (isUpdatingMessage()){
                updateMessage()
            }
            // Creates or replies to a message
            else{
                createMessage()
            }
            resetMessage()
            // Refresh the conversation object instance to the one saved in the database
            // This allows keeping updated with the latest messages (namely the one just sent)
            readConversation()
        }
    }


    // region Messages
    /**
     * Adds a [FriendMessage] object to the database
     */
    private suspend fun createMessage(){
        val messageIdRepliedTo: String = if (friendMessageUnderActionFocus == null || !isReplyingToMessage()){
            ""
        }
        // Is replying to a message, get its ID to reference it in the replying message
        else{
            friendMessageUnderActionFocus!!._id.toHexString()
        }
        val newMessage = FriendMessage()
            .also {
                it.message = message.value
                it.timeSent = getCurrentTime()
                it.ownerId = repository.getCurrentUserId()
                it.hasBeenUpdated = false
                it.messageIdRepliedTo = messageIdRepliedTo
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
        conversationsRepository.updateConversationAdd(
            friendConversation = currentConversation!!,
            messageId = newMessage._id
        )
    }


    // region Reading
    /**
     * Gets the list of [FriendMessage] objects for the current [FriendConversation]
     */
    private suspend fun readMessages(){
        messagesRepository.readConversationMessages(currentConversation!!)
            .collect{
                // Read each retrieved message and read their original messages, if any
                messageIdRepliesToOriginalMessages.clear()
                it.list.forEach {
                    friendMessage ->
                    // If the message is a reply to another message
                    if (friendMessage.messageIdRepliedTo.isNotEmpty()){
                        messageIdRepliesToOriginalMessages[friendMessage._id] =
                            readMessageReply(friendMessage)
                    }
                }

                messagesListState.clear()
                messagesListState.addAll(it.list)
                Log.i(
                    TAG(),
                    "MessagesViewModel: Finished reading messages using \"a IN b\" RQL query"
                )
                return@collect
            }
        // Code beyond this point does not get called, regardless of return statements (?)
        withContext(Dispatchers.Main){
            return@withContext
        }
        return
    }

    /**
     * Gets the [FriendMessage] the specified [FriendMessage] is a reply to, if the original
     * message still exists
     */
    private suspend fun readMessageReply(friendMessageReply: FriendMessage): String? {
        var originalMessage: String? = null
        // If the supplied message is not actually a reply to another message
        // ... return an empty string
        return if (friendMessageReply.messageIdRepliedTo.isEmpty()){
            originalMessage
        } else{
            messagesRepository.readMessage(friendMessageReply.messageIdRepliedTo.toObjectId())
                .first{
                    // If the original message no longer exists (such as from the original
                    // ... sender deleting it), show the original message as null
                    if (it.list.size == 0){
                        return@first true
                    }
                    else{
                        originalMessage = it.list[0].message
                    }
                    true
                }
            originalMessage
        }
    }
    // endregion Reading


    // region Updating
    /**
     * Updates a [FriendMessage] object in the database
     */
    private suspend fun updateMessage(){
        if (friendMessageUnderActionFocus != null){
            Log.i(
                TAG(),
                "MessagesViewModel: Updated message ID = \"${friendMessageUnderActionFocus!!._id.toHexString()}\"; " +
                        "Updated message = \"${message.value}\""
            )

            messagesRepository.updateMessage(
                messageId = friendMessageUnderActionFocus!!._id,
                newMessage = message.value
            )
            updateMessageEnd()
        }
    }

    /**
     * Starts the process for updating a [FriendMessage] in the database
     */
    fun updateMessageStart(
        friendMessageToEdit: FriendMessage
    ){
        currentAction.value = MessagesUserAction.UPDATE
        _friendMessageUnderActionFocus = friendMessageToEdit
        message.value = friendMessageToEdit.message
    }

    /**
     * Ends/Cancels the process for updating a [FriendMessage]
     */
    fun updateMessageEnd(){
        currentAction.value = MessagesUserAction.IDLE
        _friendMessageUnderActionFocus = null
        message.value = ""
    }

    fun isUpdatingMessage(): Boolean{
        return currentAction.value == MessagesUserAction.UPDATE
    }
    // endregion Updating


    // region Deleting
    /**
     * Removes a [FriendMessage] object from the database
     */
    fun deleteMessage(friendMessageToDelete: FriendMessage){
        viewModelScope.launch {
            // Do not manually remove message ID reference from conversation object
            // Results in an error saying you cannot modify list outside of write transaction
            messagesRepository.deleteMessage(
                messageId = friendMessageToDelete._id
            )
            conversationsRepository.updateConversationRemove(
                friendConversation = currentConversation!!,
                messageId = friendMessageToDelete._id
            )
        }
    }

    fun deleteMessageStart(
        friendMessageToDelete: FriendMessage
    ){
        currentAction.value = MessagesUserAction.DELETE
        _friendMessageUnderActionFocus = friendMessageToDelete
    }

    fun deleteMessageEnd(){
        currentAction.value = MessagesUserAction.IDLE
        _friendMessageUnderActionFocus = null
    }

    fun isDeletingMessage(): Boolean{
        return currentAction.value == MessagesUserAction.DELETE
    }
    // endregion Deleting


    // region Replying
    fun replyMessageStart(
        friendMessageBeingRepliedTo: FriendMessage
    ){
        currentAction.value = MessagesUserAction.REPLY
        _friendMessageUnderActionFocus = friendMessageBeingRepliedTo
    }

    fun replyMessageEnd(){
        currentAction.value = MessagesUserAction.IDLE
        _friendMessageUnderActionFocus = null
    }

    fun isReplyingToMessage(): Boolean{
        return currentAction.value == MessagesUserAction.REPLY
    }
    // endregion Replying


    /**
     * Checks if a specific [FriendMessage] was sent by the current user
     */
    fun isMessageMine(message: FriendMessage): Boolean{
        return message.ownerId == repository.getCurrentUserId()
    }

    /**
     * Checks if the user is not either updating, deleting, or replying to a message
     */
    fun isNotPerformingAnyContextualMenuAction(): Boolean{
        return !(isUpdatingMessage() || isDeletingMessage() || isReplyingToMessage())
    }
    // endregion Messages


    // region Conversations
    /**
     * Gets the current [FriendConversation] object, if it exists
     */
    private suspend fun readConversation(){
        if (_usersInvolved == null){
            Log.i(
                TAG(),
                "MessagesViewModel: Skipped reading; Users involved is null"
            )
            return
        }

        Log.i(
            TAG(),
            "MessagesViewModel: Started reading; Conversation is = \"$currentConversation\""
        )
        // There should only be 1 conversation with only the specified users involved
        conversationsRepository.readConversation(_usersInvolved!!)
            .first{
                conversationsListState.clear()
                // If a conversation object does not already exist, create it first
                if (it.list.size == 0){
                    conversationsRepository.createConversation(
                        usersInvolved = _usersInvolved!!
                    )
                }
                it.list.forEach {
                    i ->
                    Log.i(
                        TAG(),
                        "\"MessagesViewModel: Single conversation object = \"${i}\""
                    )
                }
                conversationsListState.addAll(it.list)
            }
        Log.i(
            TAG(),
            "MessagesViewModel: Done reading; Conversation is now = \"${currentConversation}\""
        )
    }

    /**
     * Updates a [FriendConversation]'s users involved field
     */
    fun updateUsersInvolved(usersInvolved: SortedSet<String>){
        _usersInvolved = usersInvolved
        // Get the corresponding conversation object with the given users involved
        viewModelScope.launch {
            readConversation()
            readMessages()
        }
    }
    // endregion Conversations


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