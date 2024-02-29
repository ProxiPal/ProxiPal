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
 * Necessary "dangerous" permissions for using Nearby API and connecting with another device
 */
val DANGEROUS_NEARBY_API_PERMISSIONS = listOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.BLUETOOTH_ADVERTISE,
    Manifest.permission.BLUETOOTH_CONNECT,
    Manifest.permission.BLUETOOTH_SCAN,
)

/**
 * Necessary plain permissions for using Nearby API and connecting with another device
 */
val SAFE_NEARBY_API_PERMISSIONS = listOf(
    Manifest.permission.BLUETOOTH,
    Manifest.permission.BLUETOOTH_ADMIN,
    Manifest.permission.ACCESS_WIFI_STATE,
    Manifest.permission.CHANGE_WIFI_STATE,
    Manifest.permission.NEARBY_WIFI_DEVICES,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)

/**
 * All necessary permissions for using Nearby API and connecting with another device
 */
val ALL_NEARBY_API_PERMISSIONS = SAFE_NEARBY_API_PERMISSIONS + DANGEROUS_NEARBY_API_PERMISSIONS

/**
 * Same as [ALL_NEARBY_API_PERMISSIONS] but as an array instead of a list
 */
val ALL_NEARBY_API_PERMISSIONS_ARRAY = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.BLUETOOTH_ADVERTISE,
    Manifest.permission.BLUETOOTH_CONNECT,
    Manifest.permission.BLUETOOTH_SCAN,
    Manifest.permission.BLUETOOTH,
    Manifest.permission.BLUETOOTH_ADMIN,
    Manifest.permission.ACCESS_WIFI_STATE,
    Manifest.permission.CHANGE_WIFI_STATE,
    Manifest.permission.NEARBY_WIFI_DEVICES,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)