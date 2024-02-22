@file:OptIn(ExperimentalMaterial3Api::class)

package com.mongodb.app.ui.compassscreen

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mongodb.app.R
import com.mongodb.app.TAG
import com.mongodb.app.data.MockRepository
import com.mongodb.app.data.RealmSyncRepository
import com.mongodb.app.data.compassscreen.KM_PER_ONE_LATITUDE_DIFF
import com.mongodb.app.data.compassscreen.KM_PER_ONE_LONGITUDE_DIFF
import com.mongodb.app.presentation.compassscreen.CompassViewModel
import com.mongodb.app.ui.theme.MyApplicationTheme
import com.mongodb.app.ui.theme.Purple200
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt


class CompassScreen : ComponentActivity(){
    /*
    ===== Variables =====
     */
    private val repository = RealmSyncRepository { _, error ->
        // Sync errors come from a background thread so route the Toast through the UI thread
        lifecycleScope.launch {
            // Catch write permission errors and notify user. This is just a 2nd line of defense
            // since we prevent users from modifying someone else's tasks
            // TODO the SDK does not have an enum for this type of error yet so make sure to update this once it has been added
            if (error.message?.contains("CompensatingWrite") == true) {
//                Toast.makeText(
//                    this@CompassScreen, getString(R.string.user_profile_permissions_warning),
//                    Toast.LENGTH_SHORT
//                )
//                    .show()
            }
        }
    }

    private val compassViewModel: CompassViewModel by viewModels{
        CompassViewModel.factory(repository, this)
    }


    /*
    ===== Functions =====
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i(
            TAG(),
            "CompassScreen: Start of OnCreate()"
        )

        setContent{
            MyApplicationTheme {
                CompassScreenLayout(
                    compassViewModel = compassViewModel
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Repository must be closed to free resources
        repository.close()
    }
}


/*
===== Functions =====
 */
/**
 * Displays the entire connection with others screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompassScreenLayout(
    compassViewModel: CompassViewModel,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            CompassScreenTopBar()
        },
        modifier = modifier
        // Pad the body of content so it does not get cut off by the scaffold top bar
    ) { innerPadding ->
        CompassScreenBodyContent(
            compassViewModel = compassViewModel,
            modifier = Modifier
                .padding(innerPadding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompassScreenTopBar(
    modifier: Modifier = Modifier
) {
    // Back button is automatically handled by the navigation code (?)
    // ... so it's not programmed here
    CenterAlignedTopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        id = R.string.compass_screen_top_bar_header
                    ),
                    color = Color.White,
                    style = MaterialTheme.typography.displayMedium
                )
            }
        },
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = Purple200
        ),
        modifier = modifier
    )
}

/**
 * Displays the main body of content
 */
@Composable
fun CompassScreenBodyContent(
    compassViewModel: CompassViewModel,
    modifier: Modifier = Modifier
) {
    val bearing = calculateBearingBetweenPoints(
        startLatitude = compassViewModel.currentUserLocation.value.latitude,
        startLongitude = compassViewModel.currentUserLocation.value.longitude,
        endLatitude = compassViewModel.matchedUserLocation.value.latitude,
        endLongitude = compassViewModel.matchedUserLocation.value.longitude
    )
    val distance = calculateDistanceBetweenPoints(
        startLatitude = compassViewModel.currentUserLocation.value.latitude,
        startLongitude = compassViewModel.currentUserLocation.value.longitude,
        endLatitude = compassViewModel.matchedUserLocation.value.latitude,
        endLongitude = compassViewModel.matchedUserLocation.value.longitude
    )

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        CompassScreenCompassVisual(
            compassViewModel = compassViewModel
        )
        CompassScreenMeasurementText(
            measurementText = R.string.compass_screen_bearing_message,
            measurement = bearing
        )
        CompassScreenMeasurementText(
            measurementText = R.string.compass_screen_distance_message,
            measurement = distance
        )
        if (compassViewModel.isMeetingWithMatch.value) {
            CompassScreenCancelButton(
                compassViewModel = compassViewModel
            )
        }
        TempCompassScreenLocationUpdating(
            compassViewModel = compassViewModel
        )
        TempCompassScreenLogMessages(
            compassViewModel = compassViewModel
        )
    }
}

/**
 * Displays the compass that points matches toward each other
 */
@Composable
fun CompassScreenCompassVisual(
    compassViewModel: CompassViewModel,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = R.drawable.tempcompass),
            contentDescription = "Compass pointing you to your matching user",
            modifier = Modifier
                // Image rotation is clockwise
                .rotate(
                    degrees = calculateBearingBetweenPoints(
                        startLatitude = compassViewModel.currentUserLocation.value.latitude,
                        startLongitude = compassViewModel.currentUserLocation.value.longitude,
                        endLatitude = compassViewModel.matchedUserLocation.value.latitude,
                        endLongitude = compassViewModel.matchedUserLocation.value.longitude
                    ).toFloat()
                )
        )
    }
}

@Composable
fun CompassScreenMeasurementText(
    @StringRes measurementText: Int,
    measurement: Double,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
    ) {
        Text(
            text = stringResource(id = measurementText, measurement)
        )
    }
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
    val deltaLatitude = (endLatitude - startLatitude) * KM_PER_ONE_LATITUDE_DIFF
    val deltaLongitude = (endLongitude - startLongitude) * KM_PER_ONE_LONGITUDE_DIFF
    return sqrt(deltaLatitude.pow(2) + deltaLongitude.pow(2))
}

// TODO Need to make sure this cancels the matching process for both users
@Composable
fun CompassScreenCancelButton(
    compassViewModel: CompassViewModel,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
    ) {
        Button(
            onClick = {
                compassViewModel.toggleMeetingWithMatch()
                Log.i(
                    "tempTag",
                    "User has canceled meeting with match"
                )
            }
        ) {
            Text(
                text = stringResource(
                    id = R.string.compass_screen_cancel_message
                )
            )
        }
    }
}

// region TemporaryUI
/**
 * Displays temporary text fields for inputting and changing matching users' locations
 */
@Composable
fun TempCompassScreenLocationUpdating(
    compassViewModel: CompassViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = "Current location:",
                modifier = Modifier
                    .weight(0.33f)
            )
            TempCompassScreenLocationUpdater(
                userLocationParameterValue = compassViewModel.currentUserLatitude.value,
                updateUserLocationParameter = { compassViewModel.updateCurrentUserLatitude(it) },
                modifier = Modifier
                    .weight(0.33f)
            )
            TempCompassScreenLocationUpdater(
                userLocationParameterValue = compassViewModel.currentUserLongitude.value,
                updateUserLocationParameter = { compassViewModel.updateCurrentUserLongitude(it) },
                modifier = Modifier
                    .weight(0.33f)
            )
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = "Match location:",
                modifier = Modifier
                    .weight(0.33f)
            )
            TempCompassScreenLocationUpdater(
                userLocationParameterValue = compassViewModel.matchedUserLatitude.value,
                updateUserLocationParameter = { compassViewModel.updateMatchedUserLatitude(it) },
                modifier = Modifier
                    .weight(0.33f)
            )
            TempCompassScreenLocationUpdater(
                userLocationParameterValue = compassViewModel.matchedUserLongitude.value,
                updateUserLocationParameter = { compassViewModel.updateMatchedUserLongitude(it) },
                modifier = Modifier
                    .weight(0.33f)
            )
        }
    }
}

/**
 * Displays a temporary text field for updating a user's location
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TempCompassScreenLocationUpdater(
    userLocationParameterValue: String,
    updateUserLocationParameter: ((String) -> Unit),
    modifier: Modifier = Modifier
) {
    TextField(
        value = userLocationParameterValue,
        onValueChange = {
            updateUserLocationParameter(it)
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Done
        ),
        singleLine = true,
        // Invalid location data will be shown as an error in the text box
        isError = userLocationParameterValue.toDoubleOrNull() == null,
        modifier = modifier
            .padding(all = 4.dp)
            .width(100.dp)
    )
}

@Composable
fun TempCompassScreenLogMessages(
    compassViewModel: CompassViewModel,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
    ) {
        // For debugging purposes only
        Button(
            onClick = {
                Log.i(
                    "tempTag",
                    "Mutable state data = " +
                            "${compassViewModel.currentUserLatitude.value} ; " +
                            "${compassViewModel.currentUserLongitude.value} ; " +
                            "${compassViewModel.matchedUserLatitude.value} ; " +
                            "${compassViewModel.matchedUserLongitude.value} ; "
                )
                Log.i(
                    "tempTag",
                    "Mutable state bearing = " +
                            "${calculateBearingBetweenPoints(
                                compassViewModel.currentUserLocation.value.latitude,
                                compassViewModel.currentUserLocation.value.longitude,
                                compassViewModel.matchedUserLocation.value.latitude,
                                compassViewModel.matchedUserLocation.value.longitude
                            )}"
                )
            },
            modifier = Modifier
        ) {
            Text(
                text = "Print log message showing currently saved data"
            )
        }
    }
}
// endregion TemporaryUI

@Preview(showBackground = true)
@Composable
fun CompassScreenLayoutPreview() {
    MyApplicationTheme {
        val repository = MockRepository()
        CompassScreenLayout(
            compassViewModel = CompassViewModel()
        )
    }
}