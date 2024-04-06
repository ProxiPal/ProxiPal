package com.mongodb.app.presentation.userprofiles

import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableDoubleStateOf
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
import com.mongodb.app.data.userprofiles.USER_PROFILE_BIOGRAPHY_MAXIMUM_CHARACTER_AMOUNT
import com.mongodb.app.data.userprofiles.USER_PROFILE_NAME_MAXIMUM_CHARACTER_AMOUNT
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/*
Contributions:
- Kevin Kubota (all user profile UI, except for navigation between screens)
- Marco Pacini (location related tasks only)
 */


object UserProfileViewEvent

sealed class AddUserProfileEvent {
    class Info(val message: String) : AddUserProfileEvent()
    class Error(val message: String, val throwable: Throwable) : AddUserProfileEvent()
}

class UserProfileViewModel(
    private var repository: SyncRepository,
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

    // for current user's location, added by Marco Pacini
    private val _userProfileLatitude: MutableState<Double> = mutableDoubleStateOf(0.0)
    private val _userProfileLongitude: MutableState<Double> = mutableDoubleStateOf(0.0)

    private val _nearbyUserProfiles: MutableList<UserProfile> = mutableListOf()

    private val _proximityRadius: MutableState<Double> = mutableDoubleStateOf(0.1)


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

    // For current user's location, added by Marco Pacini
    val userProfileLatitude: State<Double>
        get() = _userProfileLatitude

    val userProfileLongitude: State<Double>
        get() = _userProfileLongitude

    val nearbyUserProfiles: List<UserProfile>
        get() = _nearbyUserProfiles

    val proxmityRadius: State<Double>
        get() = _proximityRadius


    init {
        Log.i(
            TAG(),
            "UPViewModel: Start of Init{}"
        )
        getUserProfile()
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


    /*
    ===== Functions =====
     */
    /**
     * When a configuration change occurs, this allows updating the current SyncRepository instance
     * and prevents the app from crashing when trying to communicate with Realm after it has closed.
     */
    fun updateRepository(
        newRepository: SyncRepository
    ){
        repository = newRepository
    }

    /**
     * Retrieves the current user's user profile, if it exists
     */
    private fun getUserProfile(){
        viewModelScope.launch {
            repository.readUserProfile(repository.getCurrentUserId())
                .first{
                    userProfileListState.clear()
                    userProfileListState.addAll(it.list)
                    // When trying to update a user profile that is not saved in the database
                    // ... the SyncRepository will handle creating a new user profile before
                    // ... making the updated changes
                    if (it.list.size > 0){
                        Log.i(
                            TAG(),
                            "UserProfileViewModel: Current user's profile = \"${it.list[0]._id}\""
                        )
                        setUserProfileVariables(it.list[0])
                    }
                    true
                }
        }
    }

    private fun setUserProfileVariables(userProfile: UserProfile){
        _userProfileFirstName.value = userProfile.firstName
        _userProfileLastName.value = userProfile.lastName
        _userProfileBiography.value = userProfile.biography
        _userProfileLatitude.value = userProfile.location?.latitude!!
        _userProfileLongitude.value = userProfile.location?.longitude!!
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
     * Updates the user profile's location
     */
    fun setUserProfileLocation(latitude: Double, longitude: Double){
        _userProfileLatitude.value = latitude
        _userProfileLongitude.value = longitude

        viewModelScope.launch {
            repository.updateUserProfileLocation(
                latitude = userProfileLatitude.value,
                longitude = userProfileLongitude.value
            )
        }
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
     * Updates the user profile first name
     */
    fun setUserProfileFirstName(newFirstName: String){
        if (newFirstName.length <= USER_PROFILE_NAME_MAXIMUM_CHARACTER_AMOUNT) {
            _userProfileFirstName.value = newFirstName
        }
    }

    /**
     * Updates the user profile last name
     */
    fun setUserProfileLastName(newLastName: String){
        if (newLastName.length <= USER_PROFILE_NAME_MAXIMUM_CHARACTER_AMOUNT) {
            _userProfileLastName.value = newLastName
        }
    }

    /**
     * Updates the user profile biography
     */
    fun setUserProfileBiography(newBiography: String){
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
     * Discards any unsaved changes made to the user profile
     */
    fun discardUserProfileChanges(){
        _isEditingUserProfile.value = false
        getUserProfile()
    }

    @Deprecated(
        message = "Users should only be able to see and edit their own user profile, so this is not necessary"
    )
    fun showPermissionsMessage() {
        viewModelScope.launch {
            _event.emit(UserProfileViewEvent)
        }
    }

    @Deprecated(
        message = "Users should only be able to see and edit their own user profile"
    )
    fun isUserProfileMine(userProfile: UserProfile): Boolean = repository.isUserProfileMine(userProfile)

    @Deprecated(
        message = "Adding user profiles will only be done once, around when a user registers and creates their account." +
                "Account, and in turn user profile deletion, may not be implemented as of now"
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

    /**
     * Queries nearby user profiles and updates the nearby user profile list
     */
    fun fetchAndStoreNearbyUserProfiles() {
        CoroutineScope(Dispatchers.Main).launch {
            repository.getNearbyUserProfileList(userProfileLatitude.value, userProfileLongitude.value, 0.1)
                .collect { resultsChange: ResultsChange<UserProfile> ->
                    _nearbyUserProfiles.clear()
                    _nearbyUserProfiles.addAll(resultsChange.list)
                }
        }
    }

    fun updateProximityRadius(radiusInKilometers: Double){
        _proximityRadius.value = radiusInKilometers
    }
}
