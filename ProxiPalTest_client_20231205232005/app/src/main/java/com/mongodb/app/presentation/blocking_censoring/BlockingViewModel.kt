package com.mongodb.app.presentation.blocking_censoring

import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.mongodb.app.TAG
import com.mongodb.app.data.SyncRepository
import com.mongodb.app.data.toObjectId
import com.mongodb.app.ui.messages.empty
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


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
        viewModelScope.launch {
            tryBlockUnblockUser(true)
            blockUserEnd()
        }
    }

    fun unblockUser(){
        viewModelScope.launch {
            tryBlockUnblockUser(false)
            blockUserEnd()
        }
    }

    /**
     * Attempts to block or unblock the current user in focus
     */
    private suspend fun tryBlockUnblockUser(shouldBlock: Boolean){
        // This is inside a try-catch block in case the user ID being (un)blocked is not a valid ID
        try{
            val objectId = userIdBeingBlocked.value.toObjectId()
            repository.updateUsersBlocked(objectId, shouldBlock)
            if (shouldBlock){
                Log.i(
                    TAG(),
                    "BlockingViewModel: Blocked user = \"$objectId\""
                )
            }
            else{
                Log.i(
                    TAG(),
                    "BlockingViewModel: Unblocked user = \"$objectId\""
                )
            }
        }
        catch (e: Exception){
            if (shouldBlock){
                Log.e(
                    TAG(),
                    "BlockingViewModel: Caught exception \"$e\" while trying to block user " +
                            "with ID \"${userIdBeingBlocked.value}\""
                )
            }
            else{
                Log.e(
                    TAG(),
                    "BlockingViewModel: Caught exception \"$e\" while trying to unblock user " +
                            "with ID \"${userIdBeingBlocked.value}\""
                )
            }
        }
    }

    fun blockUserEnd(){
        _isBlockingUser.value = false
        _userIdBeingBlocked.value = String.empty
    }

    // TODO
    fun isUserBlocked(): Boolean{
        return true
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