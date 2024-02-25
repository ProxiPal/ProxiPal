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

        // Need to update repository when a configuration change occurs
        // ... otherwise app will crash when trying to access Realm after it has closed
        compassViewModel.updateRepository(
            newRepository = repository
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
 * Displays the screen that points matching users toward each other
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

/**
 * Displays the top bar for the compass screen
 */
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
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        CompassScreenCompassVisual(
            compassViewModel = compassViewModel
        )
        CompassScreenMeasurementText(
            measurementText = R.string.compass_screen_bearing_message,
            measurement = compassViewModel.bearing.value
        )
        CompassScreenMeasurementText(
            measurementText = R.string.compass_screen_distance_message,
            measurement = compassViewModel.distance.value
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
                    degrees = compassViewModel.bearing.value.toFloat()
                )
        )
    }
}

/**
 * Displays text listing some measurement (currently either the bearing or distance)
 */
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

// TODO Need to make sure this cancels the matching process for both users
/**
 * Displays a button for canceling directing matches toward each other
 */
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

/**
 * Displays a temporary button for printing log messages (for debugging only)
 */
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
                    "Mutable location state data = " +
                            "${compassViewModel.currentUserLocation.value.latitude} ; " +
                            "${compassViewModel.currentUserLocation.value.longitude} ; " +
                            "${compassViewModel.matchedUserLocation.value.latitude} ; " +
                            "${compassViewModel.matchedUserLocation.value.longitude} ; "
                )
                Log.i(
                    "tempTag",
                    "Mutable location state bearing = " +
                            "${compassViewModel.bearing.value}"
                )
                Log.i(
                    "tempTag",
                    "Mutable location state distance = " +
                            "${compassViewModel.distance.value}"
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
            compassViewModel = CompassViewModel(
                repository = repository
            )
        )
    }
}