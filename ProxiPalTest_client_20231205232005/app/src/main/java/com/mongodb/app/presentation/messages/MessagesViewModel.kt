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
        // Before creating and saving a message in the database
        // ... check if there is the corresponding conversation object
        // ... to store a reference to the message
        // If not, create the conversation object first
        // If it exists already, get the corresponding conversation object

//        getConversationList()
//        val currentConversation = getSpecificConversation(_usersInvolved)
        getCurrentConversation()
        if (_currentConversation != null){
            Log.i(
                TAG(),
                "MessagesViewModel: Conversation object exists = \"${_currentConversation}\""
            )
        }
        else{
            Log.i(
                TAG(),
                "MessagesViewModel: Conversation object doesn't exist; Adding it now"
            )
            addConversationToDatabase()
        }


        addMessageToDatabase()
        resetMessage()
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
            messagesRepository.addMessage(
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
        viewModelScope.launch {
            Log.i(
                TAG(),
                "MessageViewModel: Conversation users involved = \"${_usersInvolved}\""
            )
            conversationsRepository.addConversation(
                usersInvolved = _usersInvolved
            )
        }
    }

    private fun getSpecificConversation(usersInvolved: SortedSet<String>): FriendConversation?{
        for (conversation in allConversationsInvolvedIn){
            if (conversation.usersInvolved == usersInvolved.toRealmList()){
                return conversation
            }
        }
        Log.i(
            TAG(),
            "MessagesViewModel: Could not find conversation = \"${usersInvolved.toRealmList()}\""
        )
        return null
    }

    private fun getCurrentConversation(){
        // This logic is copied from the UserProfileViewModel class
        viewModelScope.launch {
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
                            if (event.list.size == 1){
                                _currentConversation = event.list[0]
                            }
                            else{
                                Log.i(
                                    TAG(),
                                    "MessagesViewModel: SKipping current conversation retrieval"
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
    }

    private fun getConversationList(){
        // This logic is copied from the UserProfileViewModel class
        viewModelScope.launch {
            conversationsRepository.getAllConversations()
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
                            // Add each element in the retrieved list of all conversations to a variable list
                            event.list.forEach {
                                allConversations.add(it)
                                if (it.usersInvolved.contains(repository.getCurrentUserId())){
                                    allConversationsInvolvedIn.add(it)
                                }
                            }
                            Log.i(
                                TAG(),
                                "MessagesViewModel: Lists' conversation amounts = \"" +
                                        "${allConversations.size}\" ;; \"${allConversationsInvolvedIn.size}\""
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