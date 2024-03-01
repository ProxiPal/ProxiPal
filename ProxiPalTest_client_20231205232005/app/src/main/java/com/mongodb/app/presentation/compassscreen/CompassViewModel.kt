package com.mongodb.app.presentation.compassscreen

import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
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
import com.mongodb.app.data.compassscreen.UserLocation
import com.mongodb.app.presentation.userprofiles.UserProfileViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

class CompassViewModel constructor(
    private var repository: SyncRepository
) : ViewModel() {
    /*
    ===== Variables =====
     */
    // TODO Replace this with the actual location data
    private val _matchedUserLocation: MutableState<UserLocation> = mutableStateOf(
        UserLocation(0.0, 0.0)
    )

    private val _bearing: MutableState<Double> = mutableStateOf(0.0)

    private val _distance: MutableState<Double> = mutableStateOf(0.0)

    private val _connectionType: MutableState<CompassConnectionType> =
        mutableStateOf(CompassConnectionType.OFFLINE)

    private lateinit var _userProfileViewModel: UserProfileViewModel


    /*
    ===== Properties =====
     */
    // TODO Replace this with the actual location data
    val matchedUserLocation: State<UserLocation>
        get() = _matchedUserLocation

    val bearing: State<Double>
        get() = _bearing

    val distance: State<Double>
        get() = _distance

    val connectionType: State<CompassConnectionType>
        get() = _connectionType


    init {
        // TODO Temporary setting, replace this with actual values later
        _matchedUserLocation.value.latitude = 0.0
        _matchedUserLocation.value.longitude = 0.0

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

    fun setViewModels(userProfileViewModel: UserProfileViewModel) {
        _userProfileViewModel = userProfileViewModel
    }

    /**
     * Temporary function for updating matching users' locations
     */
    private fun updateUserLocations() {
        // Update user location values by a random value in range [-10, 10]
        val minimum = -10.0
        val maximum = 10.0
        updateMatchedUserLatitude(Random.nextDouble(minimum, maximum))
        updateMatchedUserLongitude(Random.nextDouble(minimum, maximum) * 2)
        updateMeasurements()
    }

    fun getCurrentUserLocation(): String {
        return "(${_userProfileViewModel.userProfileLatitude.value}, " +
                "${_userProfileViewModel.userProfileLongitude.value})"
    }

    /**
     * Checks if a value is a valid latitude value
     */
    private fun isValidLatitude(latitude: Double): Boolean {
        return latitude in -90.0..90.0
    }

    /**
     * Checks if a value is a valid longitude value
     */
    private fun isValidLongitude(longitude: Double): Boolean {
        return longitude in -180.0..180.0
    }

    /**
     * Updates the matched user's latitude
     */
    fun updateMatchedUserLatitude(newLatitude: Double) {
        if (isValidLatitude(newLatitude)) {
            _matchedUserLocation.value.latitude = newLatitude
            updateMeasurements()
        }
    }

    /**
     * Updates the matched user's longitude
     */
    fun updateMatchedUserLongitude(newLongitude: Double) {
        if (isValidLongitude(newLongitude)) {
            _matchedUserLocation.value.longitude = newLongitude
            updateMeasurements()
        }
    }

    /**
     * Updates both the bearing and distance measurements
     */
    private fun updateMeasurements() {
        _bearing.value = calculateBearingBetweenPoints(
            startLatitude = _userProfileViewModel.userProfileLatitude.value,
            startLongitude = _userProfileViewModel.userProfileLongitude.value,
            endLatitude = matchedUserLocation.value.latitude,
            endLongitude = matchedUserLocation.value.longitude
        )
        _distance.value = calculateDistanceBetweenPoints(
            startLatitude = _userProfileViewModel.userProfileLatitude.value,
            startLongitude = _userProfileViewModel.userProfileLongitude.value,
            endLatitude = matchedUserLocation.value.latitude,
            endLongitude = matchedUserLocation.value.longitude
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

    fun updateConnectionType(newCompassConnectionType: CompassConnectionType) {
        _connectionType.value = newCompassConnectionType
        if (connectionType.value == CompassConnectionType.MEETING) {
            // TODO Temporary updating of user locations, replace this with actual values later
            viewModelScope.launch {
                while (connectionType.value != CompassConnectionType.OFFLINE) {
                    updateUserLocations()
                    delay(MS_BETWEEN_LOCATION_UPDATES)
                }
            }
        }
    }
}