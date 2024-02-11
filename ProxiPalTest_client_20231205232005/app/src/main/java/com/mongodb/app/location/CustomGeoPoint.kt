package com.mongodb.app.location
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.EmbeddedRealmObject
import io.realm.kotlin.types.RealmList

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

    fun setLatitude(lat: Double) {
        coordinates[1] = lat
    }

    fun getLatitude(): Double {
        return coordinates[1]
    }

    fun setLongitude(lon: Double) {
        coordinates[0] = lon
    }

    fun getLongitude(): Double {
        return coordinates[0]
    }
}

