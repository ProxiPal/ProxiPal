package com.mongodb.app.presentation.userprofiles

import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
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
import com.mongodb.app.TAG
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

    private val _addUserProfileEvent: MutableSharedFlow<AddUserProfileEvent> = MutableSharedFlow()

    private val _userProfileFirstName: MutableState<String> = mutableStateOf("")
    private val _userProfileLastName: MutableState<String> = mutableStateOf("")
    private val _userProfileBiography: MutableState<String> = mutableStateOf("")

    private val _isEditingUserProfile: MutableState<Boolean> = mutableStateOf(false)


    /*
    ===== Properties =====
     */
    // Read-only state flow for access outside this class
    val userProfileUiState: StateFlow<UserProfileUiState>
        get() = _userProfileUiState.asStateFlow()

    val userProfileFirstName: State<String>
        get() = _userProfileFirstName

    val userProfileLastName: State<String>
        get() = _userProfileLastName

    val userProfileBiography: State<String>
        get() = _userProfileBiography

    val isEditingUserProfile: State<Boolean>
        get() = _isEditingUserProfile

    val event: Flow<UserProfileViewEvent>
        get() = _event

    val addUserProfileEvent: Flow<AddUserProfileEvent>
        get() = _addUserProfileEvent


    init {
        Log.i(
            TAG(),
            "UPViewModel: Start of Init{}"
        )
        getUserProfile()
    }


    /*
    ===== Functions =====
     */
    /**
     * Update the user profile first name
     */
    fun updateUserProfileFirstName(newFirstName: String){
        if (newFirstName.length <= USER_PROFILE_NAME_MAXIMUM_CHARACTER_AMOUNT) {
            _userProfileFirstName.value = newFirstName
        }
    }

    /**
     * Update the user profile last name
     */
    fun updateUserProfileLastName(newLastName: String){
        if (newLastName.length <= USER_PROFILE_NAME_MAXIMUM_CHARACTER_AMOUNT) {
            _userProfileLastName.value = newLastName
        }
    }

    /**
     * Update the user profile biography
     */
    fun updateUserProfileBiography(newBiography: String){
        if (newBiography.length <= USER_PROFILE_BIOGRAPHY_MAXIMUM_CHARACTER_AMOUNT) {
            _userProfileBiography.value = newBiography
        }
    }

    /**
     * Toggles whether the user is currently updating their user profile
     */
    fun toggleUserProfileEditMode(){
        _isEditingUserProfile.value = !isEditingUserProfile.value
        // If no longer editing the user profile, save the changes to the database
        if (!isEditingUserProfile.value){
            setUserProfile()
        }
    }

    /**
     * Updates the current user's user profile, if it exists
     */
    private fun setUserProfile(){
        viewModelScope.launch {
            repository.updateUserProfile(
                firstName = userProfileFirstName.value,
                lastName = userProfileLastName.value,
                biography = userProfileBiography.value
            )
        }
    }

    /**
     * Retrieves the current user's user profile, if it exists
     */
    private fun getUserProfile(){
        viewModelScope.launch {
            repository.getUserProfileList()
                .collect { event: ResultsChange<UserProfile> ->
                    Log.i(
                        TAG(),
                        "UPViewModel: Current user's user profile amount = \"${event.list.size}\""
                    )
                    when (event) {
                        is InitialResults -> {
                            userProfileListState.clear()
                            userProfileListState.addAll(event.list)
                            // The user should not have more than 1 user profile,
                            // ... but will allow the app to run and not throw an exception for now
                            when (event.list.size){
                                0 -> {
                                    Log.i(
                                        TAG(),
                                        "UPViewModel: InitialResults; Current user has no user profile created"
                                    )
                                    // When trying to update a user profile that is not saved in the database
                                    // ... the SyncRepository will handle creating a new user profile before
                                    // ... making the updated changes
                                }
                                1 -> {
                                    Log.i(
                                        TAG(),
                                        "UPViewModel: InitialResults; Getting current user's user profile..."
                                    )
                                    // Load the saved profile details
                                    _userProfileFirstName.value = event.list[0].firstName
                                    _userProfileLastName.value = event.list[0].lastName
                                    _userProfileBiography.value = event.list[0].biography
                                }
                                else -> {
                                    Log.i(
                                        TAG(),
                                        "UPViewModel: InitialResults; Current user has more than 1 user profile; " +
                                                "Retrieving only the first user profile instance..."
                                    )
                                    // Load the saved profile details
                                    _userProfileFirstName.value = event.list[0].firstName
                                    _userProfileLastName.value = event.list[0].lastName
                                    _userProfileBiography.value = event.list[0].biography
                                }
                            }
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
//        viewModelScope.launch {
//            repository.getUserProfileList()
//                .collect { event: ResultsChange<UserProfile> ->
//                    Log.i(
//                        TAG(),
//                        "UPViewModel: Current user's user profile amount = \"${event.list.size}\""
//                    )
//                    userProfileListState.clear()
//                    userProfileListState.addAll(event.list)
//                    // The user should not have more than 1 user profile,
//                    // ... but will allow the app to run and not throw an exception for now
//                    when (event.list.size){
//                        0 -> {
//                            Log.i(
//                                TAG(),
//                                "UPViewModel: InitialResults; Current user has no user profile created"
//                            )
//                            // When trying to update a user profile that is not saved in the database
//                            // ... the SyncRepository will handle creating a new user profile before
//                            // ... making the updated changes
//                        }
//                        1 -> {
//                            Log.i(
//                                TAG(),
//                                "UPViewModel: InitialResults; Getting current user's user profile..."
//                            )
//                            // Load the saved profile details
//                            _userProfileFirstName.value = event.list[0].firstName
//                            _userProfileLastName.value = event.list[0].lastName
//                            _userProfileBiography.value = event.list[0].biography
//                        }
//                        else -> {
//                            Log.i(
//                                TAG(),
//                                "UPViewModel: InitialResults; Current user has more than 1 user profile; " +
//                                        "Retrieving only the first user profile instance..."
//                            )
//                            // Load the saved profile details
//                            _userProfileFirstName.value = event.list[0].firstName
//                            _userProfileLastName.value = event.list[0].lastName
//                            _userProfileBiography.value = event.list[0].biography
//                        }
//                    }
//                }
//        }
    }

    /**
     * Discards any unsaved changes made to the user profile
     */
    fun discardUserProfileChanges(){
        _isEditingUserProfile.value = false
        getUserProfile()
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

    fun showPermissionsMessage() {
        viewModelScope.launch {
            _event.emit(UserProfileViewEvent)
        }
    }

    @Deprecated(
        message = "Users should only be able to see and edit their own user profile"
    )
    fun isUserProfileMine(userProfile: UserProfile): Boolean = repository.isUserProfileMine(userProfile)

    /**
     * Adds a user profile instance to the database
     */
    @Deprecated(
        message = "Adding user profiles will only be done once, around when a user registers and creates their account"
    )
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
                    _addUserProfileEvent.emit(AddUserProfileEvent.Info("UPViewModel: Successfully added user profile " +
                            "\"${userProfileFirstName.value}\" ; " +
                            "\"${userProfileLastName.value}\" ; " +
                            "\"${userProfileBiography.value}\""))
                }
            }.onFailure {
                withContext(Dispatchers.Main) {
                    _addUserProfileEvent.emit(
                        AddUserProfileEvent.Error(
                            "UPViewModel: There was an error while adding the user profile " +
                                    "\"${userProfileFirstName.value}\" ; " +
                                    "\"${userProfileLastName.value}\" ; " +
                                    "\"${userProfileBiography.value}\"",
                            it
                        )
                    )
                }
            }
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
