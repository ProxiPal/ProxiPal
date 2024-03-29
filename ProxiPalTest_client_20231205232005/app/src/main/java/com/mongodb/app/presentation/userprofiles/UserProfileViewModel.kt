package com.mongodb.app.presentation.userprofiles

import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
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
import com.mongodb.app.data.USER_PROFILE_BIOGRAPHY_MAXIMUM_CHARACTER_AMOUNT
import com.mongodb.app.data.USER_PROFILE_NAME_MAXIMUM_CHARACTER_AMOUNT
import com.mongodb.app.domain.UserProfile
import io.realm.kotlin.notifications.InitialResults
import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.notifications.UpdatedResults
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/*
Contributions:
- Kevin Kubota (all user profile UI, except for navigation between screens)
- Marco Pacini (location related tasks only)
- Vichet Chim (user's interest/industry)
 */


object UserProfileViewEvent

sealed class AddUserProfileEvent {
    class Info(val message: String) : AddUserProfileEvent()
    class Error(val message: String, val throwable: Throwable) : AddUserProfileEvent()
}

class UserProfileViewModel constructor(
    private var repository: SyncRepository,
    val userProfileListState: SnapshotStateList<UserProfile> = mutableStateListOf()
) : ViewModel() {
    /*
    ===== Variables =====
     */
    private val _event: MutableSharedFlow<UserProfileViewEvent> = MutableSharedFlow()

    private val _addUserProfileEvent: MutableSharedFlow<AddUserProfileEvent> = MutableSharedFlow()

    private val _userProfileFirstName: MutableState<String> = mutableStateOf("")
    private val _userProfileLastName: MutableState<String> = mutableStateOf("")
    private val _userProfileBiography: MutableState<String> = mutableStateOf("")

    private val _isEditingUserProfile: MutableState<Boolean> = mutableStateOf(false)

    // for current user's location, added by Marco Pacini
    private val _userProfileLatitude: MutableState<Double> = mutableStateOf(0.0)
    private val _userProfileLongitude: MutableState<Double> = mutableStateOf(0.0)

    private val _nearbyUserProfiles: MutableList<UserProfile> = mutableListOf()

    private val _proximityRadius: MutableState<Double> = mutableStateOf(0.1)


    //added by George Fu for Social media handling
    private val _userProfileInstagramHandle: MutableState<String> = mutableStateOf("")
    private val _userProfileTwitterHandle: MutableState<String> = mutableStateOf("")
    private val _userProfileLinktreeHandle: MutableState<String> = mutableStateOf("")
    private val _userProfilelinkedinHandle: MutableState<String> = mutableStateOf("")

    // for current user's interests/industries, added by Vichet Chim
    private var _userProfileInterests: MutableList<String> = mutableListOf()
    private var _userProfileIndustries: MutableList<String> = mutableListOf()



    /*
    ===== Properties =====
     */
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


    //George Fu For Social Media
    val userProfileInstagramHandle: State<String>
        get() = _userProfileInstagramHandle

    val userProfileTwitterHandle: State<String>
        get() = _userProfileTwitterHandle

    val userProfileLinktreeHandle: State<String>
        get() = _userProfileLinktreeHandle

    val userProfilelinkedinHandle: State<String>
        get() = _userProfilelinkedinHandle

    // for current user's interests/industries, added by Vichet Chim
    val userProfileInterests: List<String>
        get() = _userProfileInterests
    val userProfileIndustries: List<String>
        get() = _userProfileIndustries





    init {
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
            repository.getUserProfileList()
                .collect { event: ResultsChange<UserProfile> ->
                    when (event) {
                        is InitialResults -> {
                            userProfileListState.clear()
                            userProfileListState.addAll(event.list)
                            // The user should not have more than 1 user profile,
                            // ... but will allow the app to run and not throw an exception for now
                            when (event.list.size){
                                0 -> {
                                    // When trying to update a user profile that is not saved in the database
                                    // ... the SyncRepository will handle creating a new user profile before
                                    // ... making the updated changes
                                }
                                1 -> {
                                    // Load the saved profile details
                                    _userProfileFirstName.value = event.list[0].firstName
                                    _userProfileLastName.value = event.list[0].lastName
                                    _userProfileBiography.value = event.list[0].biography
                                    _userProfileLatitude.value = event.list[0].location?.latitude!!
                                    _userProfileLongitude.value = event.list[0].location?.longitude!!

                                    _userProfileInstagramHandle.value = event.list[0].instagramHandle
                                    _userProfileTwitterHandle.value = event.list[0].twitterHandle
                                    _userProfileLinktreeHandle.value = event.list[0].linktreeHandle
                                    _userProfilelinkedinHandle.value = event.list[0].linkedinHandle

                                    _userProfileInterests = event.list[0].interests.toList().toMutableList()
                                    _userProfileIndustries = event.list[0].industries.toList().toMutableList()

                                }
                                else -> {
                                    // Load the saved profile details
                                    _userProfileFirstName.value = event.list[0].firstName
                                    _userProfileLastName.value = event.list[0].lastName
                                    _userProfileBiography.value = event.list[0].biography
                                    _userProfileLatitude.value = event.list[0].location?.latitude!!
                                    _userProfileLongitude.value = event.list[0].location?.longitude!!

                                    _userProfileInstagramHandle.value = event.list[0].instagramHandle
                                    _userProfileTwitterHandle.value = event.list[0].twitterHandle
                                    _userProfileLinktreeHandle.value = event.list[0].linktreeHandle
                                    _userProfilelinkedinHandle.value = event.list[0].linkedinHandle

                                    _userProfileInterests = event.list[0].interests.toList().toMutableList()
                                    _userProfileIndustries = event.list[0].industries.toList().toMutableList()

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
    }


    /**
     * Updates the current user's user profile, if it exists
     */
    private fun setUserProfile(){
        viewModelScope.launch {
            repository.updateUserProfile(
                firstName = userProfileFirstName.value,
                lastName = userProfileLastName.value,
                biography = userProfileBiography.value,
                //George Fu For Social Media
                instagramHandle = userProfileInstagramHandle.value,
                twitterHandle = userProfileTwitterHandle.value,
                linktreeHandle = userProfileLinktreeHandle.value,
                linkedinHandle = userProfilelinkedinHandle.value
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

    //Function for users to set up their LinkTree Handle - George Fu
    fun setUserProfileLinktreeHandle(newLinktreeHandle: String) {
        // Update the Linktree handle state
        _userProfileLinktreeHandle.value = newLinktreeHandle

        // Call the repository function to update the user profile in the database
        // Make sure to pass all necessary information to update the profile
        viewModelScope.launch {
            repository.updateUserProfile(
                firstName = _userProfileFirstName.value,
                lastName = _userProfileLastName.value,
                biography = _userProfileBiography.value,
                instagramHandle = _userProfileInstagramHandle.value, // Keep existing Instagram handle
                twitterHandle = _userProfileTwitterHandle.value,
                linktreeHandle = newLinktreeHandle,
                linkedinHandle = _userProfilelinkedinHandle.value
            )
        }
    }
    //Function For Users to Set up Twitter Handle - George Fu
    fun setUserProfileTwitterHandle(newTwitterHandle: String) {
        // Update the Twitter handle state
        _userProfileTwitterHandle.value = newTwitterHandle

        // Call the repository function to update the user profile in the database
        // Make sure to pass all necessary information to update the profile
        viewModelScope.launch {
            repository.updateUserProfile(
                firstName = _userProfileFirstName.value,
                lastName = _userProfileLastName.value,
                biography = _userProfileBiography.value,
                instagramHandle = _userProfileInstagramHandle.value, // Keep existing Instagram handle
                twitterHandle = newTwitterHandle, // Add the new Twitter handle
                linktreeHandle = _userProfileLinktreeHandle.value,
                linkedinHandle = _userProfilelinkedinHandle.value
            )
        }
    }
    //Function For Users to Set up Instagram Handle - George Fu
    fun setUserProfileInstagramHandle(newInstagramHandle: String) {
        // Update the Instagram handle state
        _userProfileInstagramHandle.value = newInstagramHandle

        // Call the repository function to update the user profile in the database
        // Make sure to pass all necessary information to update the profile
        viewModelScope.launch {
            repository.updateUserProfile(
                firstName = _userProfileFirstName.value,
                lastName = _userProfileLastName.value,
                biography = _userProfileBiography.value,
                instagramHandle = newInstagramHandle, // Pass the new Instagram handle
                twitterHandle = _userProfileTwitterHandle.value,
                linktreeHandle = _userProfileLinktreeHandle.value,
                linkedinHandle = _userProfilelinkedinHandle.value

            )
        }
    }

    //Function For Users to Set up LinkedIn Handle - George Fu
    fun setUserProfilelinkedinHandle(newlinkedinHandle: String) {
        // Update the linkedin handle state
        _userProfilelinkedinHandle.value = newlinkedinHandle

        // Call the repository function to update the user profile in the database
        // Make sure to pass all necessary information to update the profile
        viewModelScope.launch {
            repository.updateUserProfile(
                firstName = _userProfileFirstName.value,
                lastName = _userProfileLastName.value,
                biography = _userProfileBiography.value,
                instagramHandle = _userProfileInstagramHandle.value, // Pass the new Instagram handle
                twitterHandle = _userProfileTwitterHandle.value,
                linktreeHandle = _userProfileLinktreeHandle.value,
                linkedinHandle = newlinkedinHandle

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
                    biography = userProfileBiography.value,
                    //George Fu For Social Media Handling
                    instagramHandle = userProfileInstagramHandle.value,
                    twitterHandle = userProfileTwitterHandle.value,
                    linktreeHandle = userProfileLinktreeHandle.value,
                    linkedinHandle = userProfilelinkedinHandle.value
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


    //toggle user's interest
    fun toggleInterest(interest: String){
        if (_userProfileInterests.contains(interest)) {
            _userProfileInterests.remove(interest)
        }  else{
            _userProfileInterests.add(interest)
        }
        updateUserInterests(interest)
    }


    // update user interests' list
    private fun updateUserInterests(interest:String) {
    viewModelScope.launch {
        repository.updateUserProfileInterests(
            interest = interest
        )
    }
    }

    //toggle user's industry
    fun toggleIndustry(industry: String){
        if (_userProfileIndustries.contains(industry)) {
            _userProfileIndustries.remove(industry)
        }  else{
            _userProfileIndustries.add(industry)
        }
        updateUserIndustries(industry)
    }

    // update user industries' list
    private fun updateUserIndustries(industry:String) {
        viewModelScope.launch {
            repository.updateUserProfileIndustries(
                industry = industry
            )
        }
    }

}
