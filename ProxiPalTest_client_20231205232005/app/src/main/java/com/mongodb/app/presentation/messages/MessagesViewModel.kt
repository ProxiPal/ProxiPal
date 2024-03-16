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
import com.mongodb.app.data.messages.IConversationsRealm
import com.mongodb.app.data.messages.IMessagesRealm
import com.mongodb.app.domain.FriendConversation
import io.realm.kotlin.notifications.InitialResults
import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.notifications.UpdatedResults
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.SortedSet

class MessagesViewModel constructor(
    private var messagesRealm: IMessagesRealm,
    private var conversationsRealm: IConversationsRealm
) : ViewModel(){
    // region Variables
    private val _message = mutableStateOf("")
    private val _usersInvolved = sortedSetOf("")
    private val _conversationsListState: SnapshotStateList<FriendConversation> = mutableStateListOf()
    // endregion Variables


    // region Properties
    val message
        get() = _message
    val usersInvolved
        get() = _usersInvolved
    val conversationsListState
        get() = _conversationsListState
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
        addMessageToDatabase()
        resetMessage()

        addConversationToDatabase()
        getConversationList()
    }

    fun deleteMessage(){
        removeMessageFromDatabase()
    }

    /**
     * Adds a friend message to the database and consequently to both users' message history
     */
    private fun addMessageToDatabase(){
        viewModelScope.launch {
            val timeSent: Long = getCurrentTime()
            Log.i(
                TAG(),
                "MessageViewModel: Message = \"${message.value}\" ;; Time = \"${timeSent}\""
            )
            messagesRealm.addMessage(
                message = message.value,
                timeSent = timeSent
            )
        }
        // By itself this does not work, but with the viewModelScope this does work
//        CoroutineScope(Dispatchers.IO).launch {
//            runCatching {
//                messagesRealm.addMessage(
//                    message = message.value,
//                    timeSent = getTimeSent()
//                )
//            }
//        }
    }

    /**
     * Removes a sent message from the database and consequently from both users' message history
     */
    private fun removeMessageFromDatabase(){
        // TODO
    }

    private fun addConversationToDatabase(){
        // TODO These values are hardcoded for now
        val usersInvolved = sortedSetOf(
            // Gmail account
            "65e96193c6e205c32b0915cc",
            // Student account
            "6570119696faac878ad696a5"
        )
        viewModelScope.launch {
            Log.i(
                TAG(),
                "MessageViewModel: Conversation users involved = \"${usersInvolved}\""
            )
            conversationsRealm.addConversation(
                usersInvolved = usersInvolved
            )
        }
    }

    private fun getConversationList(){
        // This logic is copied from the UserProfileViewModel class
        viewModelScope.launch {
            conversationsRealm.getConversationList()
                .collect {
                    event: ResultsChange<FriendConversation> ->
                    when (event){
                        is InitialResults -> {
                            conversationsListState.clear()
                            conversationsListState.addAll(event.list)
                            Log.i(
                                TAG(),
                                "MessagesViewModel: Conversation amount = \"" +
                                        "${event.list.size}\""
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
    }

    fun updateConversationUsersInvolved(usersInvolved: SortedSet<String>){

    }
    // endregion Functions


    // Allows instantiating an instance of itself
    companion object {
        fun factory(
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
                    return MessagesViewModel (messagesRealm, conversationsRealm) as T
                }
            }
        }
    }
}