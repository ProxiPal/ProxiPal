package com.mongodb.app.data.compassscreen

import android.Manifest

// Using https://gps-coordinates.org/distance-between-coordinates.php
// Using https://latlongdata.com/distance-calculator/

/**
 * How many kilometers (km) equal to exactly 1 latitude difference
 */
const val KM_PER_ONE_LATITUDE_DIFF: Double = 111.195

/**
 * How many kilometers (km) equal to exactly 1 longitude difference
 */
const val KM_PER_ONE_LONGITUDE_DIFF: Double = 111.195

/**
 * How many milliseconds (ms) between each update of matching users' location
 */
const val MS_BETWEEN_LOCATION_UPDATES: Long = 2000

/**
 * Necessary permissions for using Nearby API and connecting with another device
 */
val PERMISSIONS_FOR_DEVICE_CONNECTIONS = listOf(
    Manifest.permission.BLUETOOTH,
    Manifest.permission.BLUETOOTH_ADMIN,
    Manifest.permission.ACCESS_WIFI_STATE,
    Manifest.permission.CHANGE_WIFI_STATE,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.NEARBY_WIFI_DEVICES,
    Manifest.permission.BLUETOOTH_SCAN,
    Manifest.permission.ACCESS_COARSE_LOCATION
)