package com.mongodb.app.presentation.messages

import android.os.Bundle
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.mongodb.app.data.messages.IMessagesRealm
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
            messagesRealm
        }
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