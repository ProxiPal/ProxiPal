package com.mongodb.app.presentation.blocking_censoring

import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.mongodb.app.TAG
import com.mongodb.app.data.SyncRepository
import com.mongodb.app.data.blocking_censoring.IBlockingCensoringRealm
import com.mongodb.app.data.toObjectId
import com.mongodb.app.domain.UserProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


/*
Contributions:
- Kevin Kubota (everything in this file)
 */


enum class BlockingAction{
    IDLE, /* Not blocking or unblocking a user */
    BLOCKING,
    UNBLOCKING
}


class BlockingViewModel (
    private var repository: SyncRepository,
    private var blockingCensoringRealm: IBlockingCensoringRealm
) : ViewModel(){
    // region Variables
    private val _currentUserId = mutableStateOf("")
    private var _currentUserProfile: MutableState<UserProfile> = mutableStateOf(UserProfile())
    private val _userIdInFocus = mutableStateOf("")
    private val _focusedUserName = mutableStateOf("")
    private val _blockingAction = mutableStateOf(BlockingAction.IDLE)
    // endregion Variables


    // region Properties
    val currentUserId
        get() = _currentUserId
    val currentUserProfile
        get() = _currentUserProfile
    val userIdInFocus
        get() = _userIdInFocus
    val focusedUserName
        get() = _focusedUserName
    val blockingAction
        get() = _blockingAction
    val isUserInFocusBlocked
        get() = currentUserProfile.value.isUserBlocked(userIdInFocus.value)
    // endregion Properties


    // region Functions
    /**
     * Updates the necessary variables during a recomposition (eg: screen orientation change)
     */
    fun updateRepositories(newRepository: SyncRepository){
        repository = newRepository
        currentUserId.value = repository.getCurrentUserId()
        viewModelScope.launch {
            resetCurrentUserProfileReference()
        }
    }

    /**
     * Updates a local variable with the user ID of another user in the process of being (un)blocked
     */
    fun updateUserInFocus(userIdInFocus: String){
        this.userIdInFocus.value = userIdInFocus
    }

    /**
     * Starts the process for blocking another user
     */
    fun blockUserStart(
        userIdToBlock: String
    ){
        blockingAction.value = BlockingAction.BLOCKING
        userIdInFocus.value = userIdToBlock
        viewModelScope.launch {
            resetFocusedUserProfileReference()
        }
    }

    /**
     * Starts the process for unblocking another user
     */
    fun unblockUserStart(
        userIdToUnblock: String
    ){
        blockingAction.value = BlockingAction.UNBLOCKING
        userIdInFocus.value = userIdToUnblock
        viewModelScope.launch {
            resetFocusedUserProfileReference()
        }
    }

    /**
     * Blocks a user
     */
    fun blockUser(){
        viewModelScope.launch {
            tryBlockUnblockUser(true)
            blockUnblockUserEnd()
            resetCurrentUserProfileReference()
        }
    }

    /**
     * Unblocks a user
     */
    fun unblockUser(){
        viewModelScope.launch {
            tryBlockUnblockUser(false)
            blockUnblockUserEnd()
            resetCurrentUserProfileReference()
        }
    }

    /**
     * Attempts to block or unblock the current user in focus
     */
    private suspend fun tryBlockUnblockUser(shouldBlock: Boolean){
        // This is inside a try-catch block in case the user ID being (un)blocked is not a valid ID
        try{
            val objectId = userIdInFocus.value.toObjectId()
            blockingCensoringRealm.updateUsersBlocked(objectId, shouldBlock)
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

    /**
     * Resets the reference to the current user's profile (to retrieve the latest list of blocked users)
     */
    private suspend fun resetCurrentUserProfileReference(){
        repository.readUserProfile(currentUserId.value)
            .first{
//                Log.i(
//                    TAG(),
//                    "BlockingViewModel: Current blocked = \"${currentUserProfile.value.usersBlocked}\""
//                )
                if (it.list.size > 0){
                    currentUserProfile.value = it.list[0]
                }
//                Log.i(
//                    TAG(),
//                    "BlockingViewModel: Now Current blocked = \"${currentUserProfile.value.usersBlocked}\""
//                )
                true
            }
    }

    /**
     * Resets the reference to the user-in-focus's profile
     */
    private suspend fun resetFocusedUserProfileReference(){
        repository.readUserProfile(userIdInFocus.value)
            .first{
                if (it.list.size > 0){
                    focusedUserName.value = it.list[0].firstName
                }
                true
            }
    }

    /**
     * Ends the process to (un)block another user
     */
    fun blockUnblockUserEnd(){
        blockingAction.value = BlockingAction.IDLE
        currentUserProfile.value = UserProfile()
//        // Reset the variables denoting which other user is currently being blocked/unblocked
//        userIdInFocus.value = String.empty
//        focusedUserName.value = String.empty
    }

    /**
     * Checks whether another user is blocked by the current user
     */
    fun isUserBlocked(userIdToCheck: String): Boolean {
        return currentUserProfile.value.isUserBlocked(userIdToCheck)
    }
    // endregion Functions


    // Allows instantiating an instance of itself
    companion object {
        fun factory(
            repository: SyncRepository,
            blockingCensoringRealm: IBlockingCensoringRealm,
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
                    return BlockingViewModel (repository, blockingCensoringRealm) as T
                }
            }
        }
    }
}