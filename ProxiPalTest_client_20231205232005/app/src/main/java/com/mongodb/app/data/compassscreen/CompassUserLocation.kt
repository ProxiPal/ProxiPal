package com.mongodb.app.data.compassscreen

@Deprecated(
    message = "Will replace this with actual geopoint data"
)
data class UserLocation(
    var longitude: Double,
    var latitude: Double
)