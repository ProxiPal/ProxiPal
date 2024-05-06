package com.mongodb.app

import com.mongodb.app.data.MockRepository
import com.mongodb.app.presentation.compassscreen.CompassViewModel
import junit.framework.TestCase.assertEquals
import org.junit.Test
import kotlin.math.sqrt

class CompassMeasurementTests {
    // km per 1 latitude, also km per 1 longitude
    private val metricPerUnit = 111.195

    // mi per 1 latitude, also mi per 1 longitude
    private val imperialPerUnit = 69.093

    @Test
    fun calculateBearing_0_0_10_10(){
        val repository = MockRepository()
        val compassViewModel = CompassViewModel(repository = repository)
        val currentLatitude = 0.0
        val currentLongitude = 0.0
        val destinationLatitude = 10.0
        val destinationLongitude = 10.0
        // Angle of line, from (0, 0) to (10, 10), from the +x axis
        val expectedBearing = 45.0
        val actualBearing = compassViewModel.calculateBearingBetweenPoints(
            currentLatitude,
            currentLongitude,
            destinationLatitude,
            destinationLongitude
        )
        assertEquals(expectedBearing, actualBearing)
    }

    @Test
    fun calculateDistance_0_0_10_10_usingMetric(){
        val repository = MockRepository()
        val compassViewModel = CompassViewModel(repository = repository)
        val currentLatitude = 0.0
        val currentLongitude = 0.0
        val destinationLatitude = 10.0
        val destinationLongitude = 10.0
        // Distance from (0, 0) to (10, 10), then multiplied by km per 1 latitude
        val expectedDistance = sqrt(200.0) * metricPerUnit
        val expectedDistanceString = String.format("%.6f", expectedDistance)
        val actualDistance = compassViewModel.calculateDistanceBetweenPoints(
            currentLatitude,
            currentLongitude,
            destinationLatitude,
            destinationLongitude,
            true
        )
        val actualDistanceString = String.format("%.6f", actualDistance)
        assertEquals(expectedDistanceString, actualDistanceString)
    }

    @Test
    fun calculateDistance_0_0_10_10_usingImperial(){
        val repository = MockRepository()
        val compassViewModel = CompassViewModel(repository = repository)
        val currentLatitude = 0.0
        val currentLongitude = 0.0
        val destinationLatitude = 10.0
        val destinationLongitude = 10.0
        // Distance from (0, 0) to (10, 10), then multiplied by mi per 1 latitude
        val expectedDistance = sqrt(200.0) * imperialPerUnit
        val expectedDistanceString = String.format("%.6f", expectedDistance)
        val actualDistance = compassViewModel.calculateDistanceBetweenPoints(
            currentLatitude,
            currentLongitude,
            destinationLatitude,
            destinationLongitude,
            false
        )
        val actualDistanceString = String.format("%.6f", actualDistance)
        assertEquals(expectedDistanceString, actualDistanceString)
    }

    @Test
    fun calculateBearing_0_0_0_10(){
        val repository = MockRepository()
        val compassViewModel = CompassViewModel(repository = repository)
        val currentLatitude = 0.0
        val currentLongitude = 0.0
        val destinationLatitude = 0.0
        val destinationLongitude = 10.0
        // Angle of line, from (0, 0) to (0, 10), from the +x axis
        val expectedBearing = 90.0
        val actualBearing = compassViewModel.calculateBearingBetweenPoints(
            currentLatitude,
            currentLongitude,
            destinationLatitude,
            destinationLongitude
        )
        assertEquals(expectedBearing, actualBearing)
    }

    @Test
    fun calculateDistance_0_0_0_10_usingMetric(){
        val repository = MockRepository()
        val compassViewModel = CompassViewModel(repository = repository)
        val currentLatitude = 0.0
        val currentLongitude = 0.0
        val destinationLatitude = 0.0
        val destinationLongitude = 10.0
        // Distance from (0, 0) to (0, 10), then multiplied by km per 1 latitude
        val expectedDistance = sqrt(100.0) * metricPerUnit
        val expectedDistanceString = String.format("%.6f", expectedDistance)
        val actualDistance = compassViewModel.calculateDistanceBetweenPoints(
            currentLatitude,
            currentLongitude,
            destinationLatitude,
            destinationLongitude,
            true
        )
        val actualDistanceString = String.format("%.6f", actualDistance)
        assertEquals(expectedDistanceString, actualDistanceString)
    }

    @Test
    fun calculateDistance_0_0_0_10_usingImperial(){
        val repository = MockRepository()
        val compassViewModel = CompassViewModel(repository = repository)
        val currentLatitude = 0.0
        val currentLongitude = 0.0
        val destinationLatitude = 0.0
        val destinationLongitude = 10.0
        // Distance from (0, 0) to (0, 10), then multiplied by mi per 1 latitude
        val expectedDistance = sqrt(100.0) * imperialPerUnit
        val expectedDistanceString = String.format("%.6f", expectedDistance)
        val actualDistance = compassViewModel.calculateDistanceBetweenPoints(
            currentLatitude,
            currentLongitude,
            destinationLatitude,
            destinationLongitude,
            false
        )
        val actualDistanceString = String.format("%.6f", actualDistance)
        assertEquals(expectedDistanceString, actualDistanceString)
    }

    @Test
    fun calculateBearing_0_0_0_0(){
        val repository = MockRepository()
        val compassViewModel = CompassViewModel(repository = repository)
        val currentLatitude = 0.0
        val currentLongitude = 0.0
        val destinationLatitude = 0.0
        val destinationLongitude = 0.0
        // Angle of line, from (0, 0) to (0, 0), from the +x axis
        val expectedBearing = 0.0
        val actualBearing = compassViewModel.calculateBearingBetweenPoints(
            currentLatitude,
            currentLongitude,
            destinationLatitude,
            destinationLongitude
        )
        assertEquals(expectedBearing, actualBearing)
    }

    @Test
    fun calculateDistance_0_0_0_0_usingMetric(){
        val repository = MockRepository()
        val compassViewModel = CompassViewModel(repository = repository)
        val currentLatitude = 0.0
        val currentLongitude = 0.0
        val destinationLatitude = 0.0
        val destinationLongitude = 0.0
        val expectedDistance = 0.0
        val expectedDistanceString = String.format("%.6f", expectedDistance)
        val actualDistance = compassViewModel.calculateDistanceBetweenPoints(
            currentLatitude,
            currentLongitude,
            destinationLatitude,
            destinationLongitude,
            true
        )
        val actualDistanceString = String.format("%.6f", actualDistance)
        assertEquals(expectedDistanceString, actualDistanceString)
    }

    @Test
    fun calculateDistance_0_0_0_0_usingImperial(){
        val repository = MockRepository()
        val compassViewModel = CompassViewModel(repository = repository)
        val currentLatitude = 0.0
        val currentLongitude = 0.0
        val destinationLatitude = 0.0
        val destinationLongitude = 0.0
        val expectedDistance = 0.0
        val expectedDistanceString = String.format("%.6f", expectedDistance)
        val actualDistance = compassViewModel.calculateDistanceBetweenPoints(
            currentLatitude,
            currentLongitude,
            destinationLatitude,
            destinationLongitude,
            false
        )
        val actualDistanceString = String.format("%.6f", actualDistance)
        assertEquals(expectedDistanceString, actualDistanceString)
    }
}