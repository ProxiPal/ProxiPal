package com.mongodb.app.presentation.blocking_censoring

import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.mongodb.app.TAG
import com.mongodb.app.data.SyncRepository
import com.mongodb.app.ui.messages.empty


class BlockingViewModel (
    private var repository: SyncRepository
) : ViewModel(){
    // region Variables
    private val _isBlockingUser = mutableStateOf(false)
    private val _userIdBeingBlocked = mutableStateOf("")
    // endregion Variables


    // region Properties
    val isBlockingUser
        get() = _isBlockingUser
    val userIdBeingBlocked
        get() = _userIdBeingBlocked
    // endregion Properties


    // region Functions
    fun updateRepositories(newRepository: SyncRepository){
        repository = newRepository
    }

    fun blockUserStart(
        userIdToBlock: String
    ){
        _isBlockingUser.value = true
        _userIdBeingBlocked.value = userIdToBlock
    }

    fun blockUser(){
        Log.i(
            TAG(),
            "BlockingViewModel: Blocked user = \"\""
        )
        blockUserEnd()
    }

    fun blockUserEnd(){
        _isBlockingUser.value = false
        _userIdBeingBlocked.value = String.empty
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
                    return BlockingViewModel (repository) as T
                }
            }
        }
    }
}