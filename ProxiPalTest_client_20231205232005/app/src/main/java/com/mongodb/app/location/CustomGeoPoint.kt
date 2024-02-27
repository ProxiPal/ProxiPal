package com.mongodb.app.location
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.EmbeddedRealmObject
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.annotations.Ignore

// Contribution: Marco Pacini
/**
 * Custom data class that conforms to the GeoJSON spec
 * Used to store latitude and longitude for a user
 * Based on https://www.mongodb.com/docs/realm/sdk/kotlin/realm-database/schemas/geospatials/#std-label-kotlin-geospatial
 */
class CustomGeoPoint : EmbeddedRealmObject {
    constructor(latitude: Double, longitude: Double) {
        coordinates.apply {
            add(longitude)
            add(latitude)
        }
    }
    // Empty constructor required by Realm
    constructor() : this(0.0, 0.0)

    // Name and type required by Realm
    var coordinates: RealmList<Double> = realmListOf()

    // Name, type, and value required by Realm
    private var type: String = "Point"

    // Latitude is added as the second element of the coordinates list and longitude is the first,
    // even though it is passed to the constructor in a different order
    // in order to conform to the GeoJSON spec
    @Ignore
    var latitude: Double
        get() = coordinates[1]
        set(value) {
            coordinates[1] = value
        }

    @Ignore
    var longitude: Double
        get() = coordinates[0]
        set(value) {
            coordinates[0] = value
        }
}


