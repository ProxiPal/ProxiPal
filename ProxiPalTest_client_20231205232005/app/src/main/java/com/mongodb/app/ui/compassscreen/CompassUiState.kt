package com.mongodb.app.ui.compassscreen

import com.mongodb.app.data.UserLocation

data class CompassUiState (
    val currentUserLocation: UserLocation = UserLocation(0.0, 0.0),
    val matchedUserLocation: UserLocation = UserLocation(0.0, 0.0)
)