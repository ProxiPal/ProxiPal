package com.mongodb.app.presentation.compassscreen

import android.os.Bundle
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.mongodb.app.data.SyncRepository
import com.mongodb.app.data.compassscreen.UserLocation
import com.mongodb.app.presentation.userprofiles.UserProfileViewModel
import com.mongodb.app.ui.compassscreen.CompassUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CompassViewModel : ViewModel() {
    /*
    ===== Variables =====
     */
    private val _userLocations = MutableStateFlow(CompassUiState())

    private val _currentUserLatitude: MutableState<String> = mutableStateOf(
        "0.0"
    )

    private val _currentUserLongitude: MutableState<String> = mutableStateOf(
        "0.0"
    )

    private val _matchedUserLatitude: MutableState<String> = mutableStateOf(
        "0.0"
    )

    private val _matchedUserLongitude: MutableState<String> = mutableStateOf(
        "0.0"
    )

    private val _isMeetingWithMatch: MutableState<Boolean> = mutableStateOf(true)


    /*
    ===== Properties =====
     */
    val userLocations: StateFlow<CompassUiState> = _userLocations.asStateFlow()

    val currentUserLatitude: State<String>
        get() = _currentUserLatitude

    val currentUserLongitude: State<String>
        get() = _currentUserLongitude

    val matchedUserLatitude: State<String>
        get() = _matchedUserLatitude

    val matchedUserLongitude: State<String>
        get() = _matchedUserLongitude

    val isMeetingWithMatch: State<Boolean>
        get() = _isMeetingWithMatch


    init{
        // Start the compass screen with the user currently meeting up with their match
        _isMeetingWithMatch.value = true
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
    private fun isValidLatitude(latitude: Double): Boolean{
        return latitude in -90.0..90.0
    }

    private fun isValidLongitude(longitude: Double): Boolean{
        return longitude in -180.0..180.0
    }

    /**
     * Updates the current user's latitude
     */
    fun updateCurrentUserLatitude(newLatitude: String){
        _currentUserLatitude.value = newLatitude
        if (newLatitude.toDoubleOrNull() != null && isValidLatitude(newLatitude.toDouble())){
            _userLocations.update { currentState ->
                currentState.copy(
                    currentUserLocation = UserLocation(
                        latitude = newLatitude.toDouble(),
                        longitude = userLocations.value.currentUserLocation.longitude
                    )
                )
            }
        }
    }

    /**
     * Updates the current user's longitude
     */
    fun updateCurrentUserLongitude(newLongitude: String){
        _currentUserLongitude.value = newLongitude
        if (newLongitude.toDoubleOrNull() != null && isValidLongitude(newLongitude.toDouble())){
            _userLocations.update { currentState ->
                currentState.copy(
                    currentUserLocation = UserLocation(
                        latitude = userLocations.value.currentUserLocation.latitude,
                        longitude = newLongitude.toDouble()
                    )
                )
            }
        }
    }

    /**
     * Updates the matched user's latitude
     */
    fun updateMatchedUserLatitude(newLatitude: String) {
        _matchedUserLatitude.value = newLatitude
        if (newLatitude.toDoubleOrNull() != null && isValidLatitude(newLatitude.toDouble())){
            _userLocations.update { currentState ->
                currentState.copy(
                    matchedUserLocation = UserLocation(
                        latitude = newLatitude.toDouble(),
                        longitude = userLocations.value.matchedUserLocation.longitude
                    )
                )
            }
        }
    }

    /**
     * Updates the matched user's longitude
     */
    fun updateMatchedUserLongitude(newLongitude: String) {
        _matchedUserLongitude.value = newLongitude
        if (newLongitude.toDoubleOrNull() != null && isValidLongitude(newLongitude.toDouble())){
            _userLocations.update { currentState ->
                currentState.copy(
                    matchedUserLocation = UserLocation(
                        latitude = userLocations.value.matchedUserLocation.latitude,
                        longitude = newLongitude.toDouble()
                    )
                )
            }
        }
    }

    /**
     * Toggles whether the current user is meeting up with their matched user
     */
    fun toggleMeetingWithMatch(){
        _isMeetingWithMatch.value = !_isMeetingWithMatch.value
    }
}