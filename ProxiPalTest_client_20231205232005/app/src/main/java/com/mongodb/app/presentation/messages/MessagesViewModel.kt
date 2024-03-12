package com.mongodb.app.presentation.messages

import android.os.Bundle
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.mongodb.app.data.SyncRepository
import com.mongodb.app.presentation.userprofiles.UserProfileViewModel

class MessagesViewModel constructor(
    private var repository: SyncRepository
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

    fun resetMessage(){
        _message.value = ""
    }
    // endregion Functions


    // Allows instantiating an instance of itself
    companion object {
        fun factory(
            repository: SyncRepository,
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
                    return MessagesViewModel (repository) as T
                }
            }
        }
    }
}