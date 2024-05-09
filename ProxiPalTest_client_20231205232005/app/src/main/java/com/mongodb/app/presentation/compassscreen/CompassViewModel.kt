package com.mongodb.app.presentation.compassscreen

import android.os.Bundle
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.mongodb.app.data.SyncRepository
import com.mongodb.app.data.compassscreen.CompassConnectionType
import com.mongodb.app.data.compassscreen.KM_PER_ONE_LATITUDE_DIFF
import com.mongodb.app.data.compassscreen.KM_PER_ONE_LONGITUDE_DIFF
import com.mongodb.app.data.compassscreen.MILES_PER_ONE_LATITUDE_DIFF
import com.mongodb.app.data.compassscreen.MILES_PER_ONE_LONGITUDE_DIFF
import com.mongodb.app.data.compassscreen.MS_BETWEEN_LOCATION_UPDATES
import com.mongodb.app.data.compassscreen.SHOULD_USE_METRIC_SYSTEM
import com.mongodb.app.data.messages.MessagesData
import com.mongodb.app.domain.UserProfile
import com.mongodb.app.presentation.userprofiles.UserProfileViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt


/*
Contributions:
- Kevin Kubota (entire file)
 */


class CompassViewModel constructor(
    private var repository: SyncRepository
) : ViewModel() {
    /*
    ===== Variables =====
     */
    private var _currentUserProfile: UserProfile? = null
    private var _focusedUserProfile: UserProfile? = null

    private var _currentUserLocation: Pair<Double, Double> = Pair(0.0, 0.0)
    private var _focusedUserLocation: Pair<Double, Double> = Pair(0.0, 0.0)

    private val _bearing: MutableState<Double> = mutableDoubleStateOf(0.0)

    private val _distance: MutableState<Double> = mutableDoubleStateOf(0.0)

    private val _connectionType: MutableState<CompassConnectionType> =
        mutableStateOf(CompassConnectionType.OFFLINE)

    private lateinit var _userProfileViewModel: UserProfileViewModel


    /*
    ===== Properties =====
     */
    val currentUserLocation
        get() = _currentUserLocation
    val focusedUserLocation
        get() = _focusedUserLocation

    val bearing: State<Double>
        get() = _bearing

    val distance: State<Double>
        get() = _distance

    val connectionType: State<CompassConnectionType>
        get() = _connectionType


    init {
        updateConnectionType(CompassConnectionType.OFFLINE)
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
                    return CompassViewModel(repository) as T
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
    ) {
        repository = newRepository
    }

    /**
     * Function to set view model instances. Should be called immediately after initialization
     */
    fun setViewModels(userProfileViewModel: UserProfileViewModel) {
        _userProfileViewModel = userProfileViewModel
    }

    /**
     * Refreshes instances of user profiles needed
     */
    fun refreshUserProfileInstances(){
        viewModelScope.launch {
            repository.readUserProfile(repository.getCurrentUserId()).first{
                if (it.list.size > 0){
                    _currentUserProfile = it.list[0]
                }
                true
            }
        }
        viewModelScope.launch {
            repository.readUserProfile(MessagesData.userIdInFocus.value).first{
                if (it.list.size > 0){
                    _focusedUserProfile = it.list[0]
                }
                true
            }
        }
    }

    private fun readUserLocations(){
        if (_currentUserProfile != null && _currentUserProfile!!.location != null){
            _currentUserLocation = Pair(
                _currentUserProfile!!.location!!.latitude,
                _currentUserProfile!!.location!!.longitude
            )
        }
        if (_focusedUserProfile != null && _focusedUserProfile!!.location != null){
            _focusedUserLocation = Pair(
                _focusedUserProfile!!.location!!.latitude,
                _focusedUserProfile!!.location!!.longitude
            )
        }
    }

    fun updateFocusedUserLocation(latitude: Double, longitude: Double){
        _focusedUserLocation = Pair(latitude, longitude)
    }

    /**
     * Updates both the bearing and distance measurements
     */
    private fun updateMeasurements() {
        _bearing.value = calculateBearingBetweenPoints(
            startLatitude = _currentUserLocation.first,
            startLongitude = _currentUserLocation.second,
            endLatitude = _focusedUserLocation.first,
            endLongitude = _focusedUserLocation.second
        )
        _distance.value = calculateDistanceBetweenPoints(
            startLatitude = _currentUserLocation.first,
            startLongitude = _currentUserLocation.second,
            endLatitude = _focusedUserLocation.first,
            endLongitude = _focusedUserLocation.second
        )
    }

    /**
     * Calculates the bearing angle, in degrees, between two points
     * (Bearing angle should be 0 degrees in the +y direction and increase clockwise)
     */
    private fun calculateBearingBetweenPoints(
        startLatitude: Double,
        startLongitude: Double,
        endLatitude: Double,
        endLongitude: Double
    ): Double {
        val deltaLatitude = endLatitude - startLatitude
        val deltaLongitude = endLongitude - startLongitude
        // The user is at the same location as their match
        if (deltaLatitude == 0.0 && deltaLongitude == 0.0) {
            return 0.0
        }
        var theta = atan2(deltaLatitude, deltaLongitude)
        // Convert the angle to degrees
        theta = Math.toDegrees(theta)
        // Subtract 90 to make 0 degrees point north instead of east
        theta -= 90
        // Make the theta non-negative
        if (theta < 0) {
            theta += 360
        }
        // Reverse the direction the bearing increases, from counter-clockwise to clockwise
        theta = 360 - theta
        // Convert 360 degrees to 0
        // Theta should never be above 360, but >= check is used anyway
        if (theta >= 360) {
            theta -= 360
        }
        return theta
    }

    /**
     * Calculates the distance, in km, between two points
     */
    private fun calculateDistanceBetweenPoints(
        startLatitude: Double,
        startLongitude: Double,
        endLatitude: Double,
        endLongitude: Double
    ): Double {
        // Using the distance formula
        // Make sure to take into account the actual distance between points
        var deltaLatitude = (endLatitude - startLatitude)
        deltaLatitude *=
            if (SHOULD_USE_METRIC_SYSTEM) KM_PER_ONE_LATITUDE_DIFF
            else MILES_PER_ONE_LATITUDE_DIFF
        var deltaLongitude = (endLongitude - startLongitude)
        deltaLongitude *=
            if (SHOULD_USE_METRIC_SYSTEM) KM_PER_ONE_LONGITUDE_DIFF
            else MILES_PER_ONE_LONGITUDE_DIFF
        return sqrt(deltaLatitude.pow(2) + deltaLongitude.pow(2))
    }

    /**
     * Called by [CompassNearbyAPI]'s [CompassNearbyAPI.updateConnectionType] function
     */
    fun updateConnectionType(newCompassConnectionType: CompassConnectionType) {
        _connectionType.value = newCompassConnectionType
        if (connectionType.value == CompassConnectionType.MEETING) {
            viewModelScope.launch {
                while (connectionType.value != CompassConnectionType.OFFLINE) {
                    readUserLocations()
                    updateMeasurements()
                    delay(MS_BETWEEN_LOCATION_UPDATES)
                }
            }
        }
    }
}