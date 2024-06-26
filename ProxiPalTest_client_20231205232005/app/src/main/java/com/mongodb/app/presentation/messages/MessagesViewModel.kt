package com.mongodb.app.presentation.messages

import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.MutableState
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
import com.mongodb.app.data.messages.MAX_USERS_PER_CONVERSATION
import com.mongodb.app.data.messages.MessagesUserAction
import com.mongodb.app.data.toObjectId
import com.mongodb.app.domain.FriendConversation
import com.mongodb.app.domain.FriendMessage
import com.mongodb.app.domain.UserProfile
import com.mongodb.app.ui.messages.empty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.SortedSet


/*
Contributions:
- Kevin Kubota (everything in this file)
 */


private const val ZONE_ID_UTC = "UTC"


class MessagesViewModel(
    private var repository: SyncRepository,
    private var messagesRepository: IMessagesRealm,
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
    private val _otherUserProfileId = mutableStateOf("")
    private val _otherUserProfileName = mutableStateOf("")
    private var _currentUserProfile: MutableState<UserProfile> = mutableStateOf(UserProfile())

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
    val otherUserProfileId
        get() = _otherUserProfileId
    val otherUserProfileName
        get() = _otherUserProfileName
    val currentUserProfile
        get() = _currentUserProfile
    // endregion Properties


    // region Functions
    /**
     * When a configuration change occurs, this allows updating the current SyncRepository instance
     * and prevents the app from crashing when trying to communicate with Realm after it has closed.
     */
    fun updateRepository(
        newRepository: SyncRepository
    ){
        repository = newRepository
    }

    /**
     * Retrieves the latest [FriendConversation] object from the database and its referenced [FriendMessage]s
     */
    fun refreshMessages(){
        viewModelScope.launch {
            // Load the conversation object first
            readConversation()
            // Then load the conversation's corresponding messages
            // This should also be called last because code beyond this point does not get called
            readMessages()
        }
    }

    /**
     * Called when a user sends a new, updates an existing, or creates a reply message.
     * Deleting a message is handled using [deleteMessage]
     */
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
            refreshMessages()
        }
    }

    /**
     * Updates the text that gets displayed in the input field row at the bottom of the messages screen
     */
    fun updateMessage(newMessage: String){
        _message.value = newMessage
    }

    /**
     * Resets the text that gets displayed in the input field row at the bottom of the messages screen
     */
    private fun resetMessage(){
        _message.value = String.empty
        currentAction.value = MessagesUserAction.IDLE
    }


    // region OtherUser
    private fun readMyUserProfile(){
        viewModelScope.launch {
            repository.readUserProfile(repository.getCurrentUserId())
                .first{
                    if (it.list.size > 0){
                        currentUserProfile.value = it.list[0]
                    }
                    true
                }
        }
    }

    /**
     * Gets the [UserProfile] of the other user involved in a [FriendConversation]
     */
    private fun readOtherUserProfile(){
        val otherUserId = getOtherUserInvolvedId()
        viewModelScope.launch {
            if (otherUserId.isEmpty()){
                return@launch
            }
            else {
                repository.readUserProfile(otherUserId)
                    .first{
                        if (it.list.size > 0){
                            otherUserProfileId.value = it.list[0].ownerId
                            otherUserProfileName.value = it.list[0].firstName
                        }
                        true
                    }
            }
        }
    }

    /**
     * Gets the ID of the other user involved in the current conversation
     * (There should only currently be 2 users involved per conversation,
     * 1 being the current user and the other being another user.
     * This function returns the other user.)
     */
    private fun getOtherUserInvolvedId(): String{
        // Users involved list is not set yet
        if (_usersInvolved == null){
            return String.empty
        }
        // Users involved list has the wrong amount of users
        if (_usersInvolved!!.size > MAX_USERS_PER_CONVERSATION){
            return String.empty
        }
        // Users involved list does not contain the current user
        if (!_usersInvolved!!.contains(repository.getCurrentUserId())){
            return String.empty
        }
        // Search through all the friend IDs in the users involved list
        for (userID in _usersInvolved!!){
            // Return the other user's ID (the one not equal to the current user's)
            if (!userID.equals(repository.getCurrentUserId())){
                return userID
            }
        }
        return String.empty
    }
    // endregion OtherUser


    // region DateTime
    /**
     * Returns the amount of milliseconds since the epoch time
     * (Note, the same instance in time across different time zones return the same epoch time)
     */
    fun getEpochTime(): Long{
//        // There are many ways to get the epoch time, but this might be the most common
//        return Calendar.getInstance().timeInMillis

        val localDateTime = LocalDateTime.now()
        val localZoned = localDateTime.atZone(ZoneId.systemDefault())
        // This contains the UTC time
        val utcZoned = localZoned.withZoneSameInstant(ZoneId.of(ZONE_ID_UTC))
        return utcZoned.toInstant().toEpochMilli()
    }

    private fun getDate(msSinceEpoch: Long, shouldUseUTC: Boolean): ZonedDateTime{
        val instant = Instant.ofEpochMilli(msSinceEpoch)
        val zoneId = if (shouldUseUTC){
            ZONE_ID_UTC
        } else {
            ZoneId.systemDefault().toString()
        }
        val zonedDateTime = instant.atZone(ZoneId.of(zoneId))
        return zonedDateTime
    }

    /**
     * Returns the current date and time in the user's local time
     */
    fun getLocalDate(msSinceEpoch: Long = getEpochTime()): ZonedDateTime{
        return getDate(msSinceEpoch, false)
    }

    /**
     * Returns the current date and time in the universal time zone (UTC)
     */
    fun getUniversalDate(msSinceEpoch: Long = getEpochTime()): ZonedDateTime {
        return getDate(msSinceEpoch, true)
    }
    // endregion DateTime


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
                it.timeSent = getEpochTime()
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
                // Manually sort the messages by their time sent (time since epoch time)
                // Without this, messages will sometimes show out of order
                // (It appears it would have been sorted by who sent the message instead)
                messagesListState.sortBy{
                    toSort -> toSort.timeSent
                }
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
     * Starts the process for updating a [FriendMessage]
     */
    fun updateMessageStart(
        friendMessageToEdit: FriendMessage
    ){
        currentAction.value = MessagesUserAction.UPDATE
        _friendMessageUnderActionFocus = friendMessageToEdit
        message.value = friendMessageToEdit.message
    }

    /**
     * Ends the process for updating a [FriendMessage]
     */
    fun updateMessageEnd(){
        currentAction.value = MessagesUserAction.IDLE
        _friendMessageUnderActionFocus = null
        message.value = ""
    }

    /**
     * Checks if the user is currently updating a message
     */
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

    /**
     * Starts the process for deleting a message
     */
    fun deleteMessageStart(
        friendMessageToDelete: FriendMessage
    ){
        currentAction.value = MessagesUserAction.DELETE
        _friendMessageUnderActionFocus = friendMessageToDelete
    }

    /**
     * Ends the process for deleting a message
     */
    fun deleteMessageEnd(){
        currentAction.value = MessagesUserAction.IDLE
        _friendMessageUnderActionFocus = null
    }

    /**
     * Checks if the user is currently deleting a message
     */
    fun isDeletingMessage(): Boolean{
        return currentAction.value == MessagesUserAction.DELETE
    }
    // endregion Deleting


    // region Replying
    /**
     * Starts the process for creating a reply message
     */
    fun replyMessageStart(
        friendMessageBeingRepliedTo: FriendMessage
    ){
        currentAction.value = MessagesUserAction.REPLY
        _friendMessageUnderActionFocus = friendMessageBeingRepliedTo
    }

    /**
     * Ends the process for creating a reply message
     */
    fun replyMessageEnd(){
        currentAction.value = MessagesUserAction.IDLE
        _friendMessageUnderActionFocus = null
    }

    /**
     * Checks if the user is currently replying to a message
     */
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
            return
        }

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
                        "MessagesViewModel: Single conversation object = \"${i}\""
                    )
                }
                conversationsListState.addAll(it.list)
            }
    }

    /**
     * Updates a [FriendConversation]'s users involved field
     */
    fun updateUsersInvolved(usersInvolved: SortedSet<String>){
        // A safety check that adds the current user if not involved in the conversation already
        if (!usersInvolved.contains(repository.getCurrentUserId())){
            usersInvolved.add(repository.getCurrentUserId())
        }

        // Skip if the number of users involved is invalid
        if (usersInvolved.size > MAX_USERS_PER_CONVERSATION || usersInvolved.size <= 1){
            Log.i(
                TAG(),
                "MessagesViewModel: Skipping conversation users involved updating"
            )
            return
        }

        _usersInvolved = usersInvolved
        readMyUserProfile()
        readOtherUserProfile()
        refreshMessages()
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