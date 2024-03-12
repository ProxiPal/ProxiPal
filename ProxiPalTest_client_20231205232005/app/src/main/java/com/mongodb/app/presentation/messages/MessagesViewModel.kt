package com.mongodb.app.presentation.messages

import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.mongodb.app.TAG
import com.mongodb.app.data.messages.IMessagesRealm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MessagesViewModel constructor(
    private var messagesRealm: IMessagesRealm
) : ViewModel(){
    // region Variables
    private val _message = mutableStateOf("")
    // endregion Variables


    // region Properties
    val message
        get() = _message
    // endregion Properties


    // region Functions
    fun updateMessage(newMessage: String){
        _message.value = newMessage
    }

    private fun resetMessage(){
        _message.value = ""
    }

    private fun getTimeSent(): Long{
        // TODO Update this to actually get the system time sent
        return Long.MAX_VALUE
    }

    fun sendMessage(){
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
            messagesRealm.addMessage(
                message = message.value,
                timeSent = getTimeSent()
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
    // endregion Functions


    // Allows instantiating an instance of itself
    companion object {
        fun factory(
            messagesRealm: IMessagesRealm,
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
                    return MessagesViewModel (messagesRealm) as T
                }
            }
        }
    }
}