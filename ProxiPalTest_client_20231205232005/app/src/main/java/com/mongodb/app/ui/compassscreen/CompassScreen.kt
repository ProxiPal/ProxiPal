@file:OptIn(ExperimentalMaterial3Api::class)

package com.mongodb.app.ui.compassscreen

import android.os.Bundle
import android.util.Log
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.mongodb.app.R
import com.mongodb.app.TAG
import com.mongodb.app.data.MockRepository
import com.mongodb.app.data.RealmSyncRepository
import com.mongodb.app.presentation.compassscreen.CompassViewModel
import com.mongodb.app.ui.components.SingleButtonRow
import com.mongodb.app.ui.components.SingleTextRow
import com.mongodb.app.ui.theme.MyApplicationTheme
import com.mongodb.app.ui.theme.Purple200
import kotlinx.coroutines.launch

/*
TODO Current plan for order of events (See CompassConnectionType.kt for more)
(1) Use case #8: Connect with others functionality happens first
(2) 2 matched users are brought to this screen
(3) Send a request to meet with your match
(4) If your match is offline or does not respond to your request within some
... time limit, show an error saying this profile could not be connected to.
... Show some buttons to either retry or cancel the connection
(Ex: Person A tries to connect to person B, but person B is offline. Person B
... could get a notification saying person A tried to connect with them. Let's
... say later person A is now offline but person B is online. Person B would see
... the notification from person A and could try to re-connect with them. Since
... person A is offline, person B would send a notification to person A. This
... process could loop until eventually persons A and B are online at the same
... time.)
(5) If you could connect to your match, show the compass screen for both users
(6) If a user cancels the meeting, stop showing the compass screen for both users
(7) If the distance between 2 matches is close enough, automatically cancel the
... connection
 */
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
        if (compassViewModel.isMeetingWithMatch.value){
            CompassScreenBodyContent(
                compassViewModel = compassViewModel,
                modifier = Modifier
                    .padding(innerPadding)
            )
        }
        // If canceling the meeting with a matched user
        else{
            CompassScreenBodyErrorContent(
                compassViewModel = compassViewModel,
                modifier = Modifier
                    .padding(innerPadding)
            )
        }
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
            .verticalScroll(rememberScrollState())
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
        CompassScreenCancelButton(
            compassViewModel = compassViewModel
        )
        TempCompassScreenLocationUpdating(
            compassViewModel = compassViewModel
        )
        TempCompassScreenLogMessages(
            compassViewModel = compassViewModel
        )
    }
}

@Composable
fun CompassScreenBodyErrorContent(
    compassViewModel: CompassViewModel,
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // The current or matched user canceled the connection
        if (compassViewModel.isMeetingWithMatch.value){
            SingleTextRow(
                textId = R.string.compass_screen_canceled_error_message
            )
            CompassScreenReturnButton()
        }
        // Could not establish connection with matched user
        // Show options to go back or try again
        else{
            SingleTextRow(
                textId = R.string.compass_screen_connection_error_message
            )
            SingleButtonRow(
                onButtonClick = { /*TODO*/ },
                textId = R.string.compass_screen_retry_button
            )
            CompassScreenReturnButton()
        }
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
            contentDescription = null,
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
    SingleButtonRow(
        onButtonClick = {
            compassViewModel.toggleMeetingWithMatch()
            Log.i(
                "tempTag",
                "User has canceled meeting with match"
            )
        },
        textId = R.string.compass_screen_cancel_message,
        modifier = modifier
    )
}

/**
 * Displays the back button for returning to the connect with others screen
 */
@Composable
fun CompassScreenReturnButton(
    modifier: Modifier = Modifier
){
    SingleButtonRow(
        onButtonClick = { /*TODO*/ },
        textId = R.string.compass_screen_return_button,
        modifier = modifier
    )
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

@Preview(showBackground = true)
@Composable
fun CompassScreenConnectionErrorPreview(){
    MyApplicationTheme {
        val repository = MockRepository()
        CompassScreenBodyErrorContent(
            compassViewModel = CompassViewModel(
                repository = repository
            )
        )
    }
}