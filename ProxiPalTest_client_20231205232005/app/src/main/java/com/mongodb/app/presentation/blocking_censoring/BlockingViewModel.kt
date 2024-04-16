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
import com.mongodb.app.domain.UserProfile
import com.mongodb.app.ui.messages.empty
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


enum class BlockingAction{
    IDLE, /* Not blocking or unblocking a user */
    BLOCKING,
    UNBLOCKING
}


class BlockingViewModel (
    private var repository: SyncRepository
) : ViewModel(){
    // region Variables
    private var _currentUserId = mutableStateOf("")
    private var _currentUserProfile: UserProfile? = null
    private val _blockingAction = mutableStateOf(BlockingAction.IDLE)
    private val _userIdInFocus = mutableStateOf("")
    // endregion Variables


    // region Properties
    val currentUserId
        get() = _currentUserId
    val currentUserProfile
        get() = _currentUserProfile
    val blockingAction
        get() = _blockingAction
    val userIdInFocus
        get() = _userIdInFocus
    // endregion Properties


    // region Functions
    fun updateRepositories(newRepository: SyncRepository){
        repository = newRepository
        currentUserId.value = repository.getCurrentUserId()
        viewModelScope.launch {
            resetCurrentUserProfileReference()
        }
    }

    fun blockUserStart(
        userIdToBlock: String
    ){
        blockingAction.value = BlockingAction.BLOCKING
        userIdInFocus.value = userIdToBlock
    }

    fun unblockUserStart(
        userIdToUnblock: String
    ){
        blockingAction.value = BlockingAction.UNBLOCKING
        userIdInFocus.value = userIdToUnblock
    }

    fun blockUser(){
        viewModelScope.launch {
            tryBlockUnblockUser(true)
            resetCurrentUserProfileReference()
            blockUnblockUserEnd()
        }
    }

    fun unblockUser(){
        viewModelScope.launch {
            tryBlockUnblockUser(false)
            resetCurrentUserProfileReference()
            blockUnblockUserEnd()
        }
    }

    /**
     * Attempts to block or unblock the current user in focus
     */
    private suspend fun tryBlockUnblockUser(shouldBlock: Boolean){
        // This is inside a try-catch block in case the user ID being (un)blocked is not a valid ID
        try{
            val objectId = userIdInFocus.value.toObjectId()
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
                            "with ID \"${userIdInFocus.value}\""
                )
            }
            else{
                Log.e(
                    TAG(),
                    "BlockingViewModel: Caught exception \"$e\" while trying to unblock user " +
                            "with ID \"${userIdInFocus.value}\""
                )
            }
        }
    }

    private suspend fun resetCurrentUserProfileReference(){
        repository.readUserProfile(currentUserId.value)
            .first{
                if (it.list.size > 0){
                    _currentUserProfile = it.list[0]
                }
                true
            }
    }

    fun blockUnblockUserEnd(){
        blockingAction.value = BlockingAction.IDLE
        userIdInFocus.value = String.empty
    }

    fun isUserBlocked(userId: String): Boolean {
        if (currentUserProfile == null){
            return false
        }
        return currentUserProfile!!.usersBlocked.contains(userId)
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