package com.mongodb.app.ui.compassscreen

import com.mongodb.app.data.compassscreen.UserLocation

@Deprecated(
    message = "Breaks Composable function previews when used in View Model classes; " +
            "Embed mutable State variables in View Model class instead of a separate UI data class"
)
data class CompassUiState (
    val currentUserLocation: UserLocation = UserLocation(0.0, 0.0),
    val matchedUserLocation: UserLocation = UserLocation(0.0, 0.0)
)