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
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.notifications.InitialResults
import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.notifications.UpdatedResults
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.SortedSet

class MessagesViewModel constructor(
    private var repository: SyncRepository,
    private var messagesRepository: IMessagesRealm,
    private var conversationsRepository: IConversationsRealm
) : ViewModel(){
    // region Variables
    private val _message = mutableStateOf("")
    private var _usersInvolved: SortedSet<String> = sortedSetOf("")
    private val _conversationsListState: SnapshotStateList<FriendConversation> = mutableStateListOf()
    private val _allConversations: MutableList<FriendConversation> = mutableListOf()
    private val _allConversationsInvolvedIn: MutableList<FriendConversation> = mutableListOf()
    private var _currentConversation: FriendConversation? = null
    // endregion Variables


    // region Properties
    val message
        get() = _message
    val conversationsListState
        get() = _conversationsListState
    val allConversations
        get() = _allConversations
    val allConversationsInvolvedIn
        get() = _allConversationsInvolvedIn
    // endregion Properties


    // region Functions
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
            getCurrentConversation()

            addMessageToDatabase()
            resetMessage()
        }
    }

    fun deleteMessage(){
        removeMessageFromDatabase()
    }

    /**
     * Adds a friend message to the database and consequently to both users' message history
     */
    private suspend fun addMessageToDatabase(){
        val timeSent: Long = getCurrentTime()
        Log.i(
            TAG(),
            "MessagesViewModel: Message = \"${message.value}\" ;; Time = \"${timeSent}\""
        )
        messagesRepository.addMessage(
            message = message.value,
            timeSent = timeSent
        )
    }

    /**
     * Removes a sent message from the database and consequently from both users' message history
     */
    private fun removeMessageFromDatabase(){
        // TODO
    }

    private suspend fun addConversationToDatabase(){
        Log.i(
            TAG(),
            "MessagesViewModel: Conversation users involved = \"${_usersInvolved}\""
        )
        conversationsRepository.addConversation(
            usersInvolved = _usersInvolved
        )
    }

    private suspend fun getCurrentConversation(){
        // This logic is copied from the UserProfileViewModel class
        conversationsRepository.getSpecificConversation(_usersInvolved)
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
                                addConversationToDatabase()
                            }
                            else -> {
                                Log.i(
                                    TAG(),
                                    "MessagesViewModel: Too many found current conversations; Skipping..."
                                )
                            }
                        }
                        Log.i(
                            TAG(),
                            "MessagesViewModel: Current conversation = \"${_currentConversation}\""
                        )
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

    fun updateConversationUsersInvolved(usersInvolved: SortedSet<String>){
        _usersInvolved = usersInvolved
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