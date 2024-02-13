package com.mongodb.app.presentation.userprofiles

import android.os.Bundle
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.mongodb.app.data.SyncRepository
import com.mongodb.app.data.USER_PROFILE_BIOGRAPHY_MAXIMUM_CHARACTER_AMOUNT
import com.mongodb.app.data.USER_PROFILE_NAME_MAXIMUM_CHARACTER_AMOUNT
import com.mongodb.app.domain.UserProfile
import com.mongodb.app.ui.userprofiles.UserProfileUiState
import io.realm.kotlin.notifications.InitialResults
import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.notifications.UpdatedResults
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object UserProfileViewEvent

// From AddItemViewModel
sealed class AddUserProfileEvent {
    class Info(val message: String) : AddUserProfileEvent()
    class Error(val message: String, val throwable: Throwable) : AddUserProfileEvent()
}

class UserProfileViewModel constructor(
    private val repository: SyncRepository,
    val userProfileListState: SnapshotStateList<UserProfile> = mutableStateListOf()
) : ViewModel() {
    /*
    ===== Variables =====
     */
    // Accessible and editable only in this class
    // A "data holder observable flow" for current and new states
    private val _userProfileUiState = MutableStateFlow(UserProfileUiState())

    private val _event: MutableSharedFlow<UserProfileViewEvent> = MutableSharedFlow()

    // From AddItemViewModel
    private val _addUserProfileEvent: MutableSharedFlow<AddUserProfileEvent> = MutableSharedFlow()


    /*
    ===== Properties =====
     */
    // Read-only state flow for access outside this class
    val userProfileUiState: StateFlow<UserProfileUiState> = _userProfileUiState.asStateFlow()

    var userProfileFirstName: MutableState<String> = mutableStateOf("")
        private set

    var userProfileLastName: MutableState<String> = mutableStateOf("")
        private set

    var userProfileBiography: MutableState<String> = mutableStateOf("")
        private set

    var isEditingUserProfile by mutableStateOf(false)
        private set

    var temporaryOwnerId by mutableStateOf("")
        private set

    val event: Flow<UserProfileViewEvent>
        get() = _event

    // From AddItemViewModel
    val addUserProfileEvent: Flow<AddUserProfileEvent>
        get() = _addUserProfileEvent


    init {
        viewModelScope.launch {
            repository.getUserProfileList()
                .collect { event: ResultsChange<UserProfile> ->
                    when (event) {
                        is InitialResults -> {
                            userProfileListState.clear()
                            userProfileListState.addAll(event.list)
                        }
                        is UpdatedResults -> {
                            if (event.deletions.isNotEmpty() && userProfileListState.isNotEmpty()) {
                                event.deletions.reversed().forEach {
                                    userProfileListState.removeAt(it)
                                }
                            }
                            if (event.insertions.isNotEmpty()) {
                                event.insertions.forEach {
                                    userProfileListState.add(it, event.list[it])
                                }
                            }
                            if (event.changes.isNotEmpty()) {
                                event.changes.forEach {
                                    userProfileListState.removeAt(it)
                                    userProfileListState.add(it, event.list[it])
                                }
                            }
                        }
                        else -> Unit // No-op
                    }
                }
        }
    }


    /*
    ===== Functions =====
     */
    /**
    Update the user profile first name
     */
    fun updateUserProfileFirstName(newFirstName: String){
        if (newFirstName.length <= USER_PROFILE_NAME_MAXIMUM_CHARACTER_AMOUNT) {
            userProfileFirstName.value = newFirstName
        }
    }

    /**
    Update the user profile last name
     */
    fun updateUserProfileLastName(newLastName: String){
        if (newLastName.length <= USER_PROFILE_NAME_MAXIMUM_CHARACTER_AMOUNT) {
            userProfileLastName.value = newLastName
        }
    }

    /**
    Update the user profile biography
     */
    fun updateUserProfileBiography(newBiography: String){
        if (newBiography.length <= USER_PROFILE_BIOGRAPHY_MAXIMUM_CHARACTER_AMOUNT) {
            userProfileBiography.value = newBiography
        }
    }

    /**
    Toggles whether the user is currently updating their user profile
     */
    fun toggleUserProfileEditMode(){
        isEditingUserProfile = !isEditingUserProfile
    }

    /**
     * Returns how many more characters are allowed before the corresponding character limit is reached
     */
    fun getRemainingCharacterAmountFirstName(): Int{
        return USER_PROFILE_NAME_MAXIMUM_CHARACTER_AMOUNT - userProfileFirstName.value.length
    }

    /**
     * Returns how many more characters are allowed before the corresponding character limit is reached
     */
    fun getRemainingCharacterAmountLastName(): Int{
        return USER_PROFILE_NAME_MAXIMUM_CHARACTER_AMOUNT - userProfileLastName.value.length
    }

    /**
     * Returns how many more characters are allowed before the corresponding character limit is reached
     */
    fun getRemainingCharacterAmountBiography(): Int{
        return USER_PROFILE_BIOGRAPHY_MAXIMUM_CHARACTER_AMOUNT - userProfileBiography.value.length
    }

    /**
     * (Temporary fast way to change current user's "owner ID" to view corresponding user profile)
     */
    fun updateTemporaryOwnerId(newOwnerId: String){
        temporaryOwnerId = newOwnerId
    }

    // Not in use since toggleIsComplete function is not added/copied for UserProfiles
//    fun toggleIsComplete(task: Item) {
//        CoroutineScope(Dispatchers.IO).launch {
//            repository.toggleIsComplete(task)
//        }
//    }

    fun showPermissionsMessage() {
        viewModelScope.launch {
            _event.emit(UserProfileViewEvent)
        }
    }

    fun isUserProfileMine(userProfile: UserProfile): Boolean = repository.isUserProfileMine(userProfile)

    /**
     * Adds a user profile instance to the Realm/Mongo Atlas database
     */
    fun addUserProfile() {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                repository.addUserProfile(
                    firstName = userProfileFirstName.value,
                    lastName = userProfileLastName.value,
                    biography = userProfileBiography.value
                )
            }.onSuccess {
                withContext(Dispatchers.Main) {
                    _addUserProfileEvent.emit(AddUserProfileEvent.Info("User profile \"$userProfileFirstName\"" +
                            " ; \"$userProfileLastName\" ; \"$userProfileBiography\" added successfully."))
                }
            }.onFailure {
                withContext(Dispatchers.Main) {
                    _addUserProfileEvent.emit(
                        AddUserProfileEvent.Error(
                            "There was an error while adding the user profile " +
                                    "\"$userProfileFirstName\" ; \"$userProfileLastName\" ; \"$userProfileBiography\"",
                            it
                        )
                    )
                }
            }
//            cleanUpAndClose()
        }
    }


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
                    return UserProfileViewModel (repository) as T
                }
            }
        }
    }
}
