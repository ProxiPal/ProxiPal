package com.mongodb.app.data.compassscreen

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