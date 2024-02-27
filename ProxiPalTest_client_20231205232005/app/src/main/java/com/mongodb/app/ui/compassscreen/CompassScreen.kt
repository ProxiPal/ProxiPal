@file:OptIn(ExperimentalMaterial3Api::class)

package com.mongodb.app.ui.compassscreen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.CallSuper
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.mongodb.app.R
import com.mongodb.app.TAG
import com.mongodb.app.data.MockRepository
import com.mongodb.app.data.RealmSyncRepository
import com.mongodb.app.data.compassscreen.CompassConnectionType
import com.mongodb.app.data.compassscreen.UserLocation
import com.mongodb.app.presentation.compassscreen.CompassCommunication
import com.mongodb.app.presentation.compassscreen.CompassViewModel
import com.mongodb.app.ui.components.SingleButtonRow
import com.mongodb.app.ui.components.SingleTextRow
import com.mongodb.app.ui.theme.MyApplicationTheme
import com.mongodb.app.ui.theme.Purple200
import kotlinx.coroutines.launch

/*
TODO Current plan for order of events (See CompassConnectionType.kt for more)
(1) When searching for other users to connect to
(a) Use case #8
(2) When selecting a user to connect to
(a) Start discovering and advertising
(b) Have a variable set to other user's ID and only connect to a device with that ID
(c) If successful, use connection lifecycle object to show each other's location
(3) If a user cancels the connection
(a) Show the cancellation message to both users
 */
class CompassScreen : ComponentActivity(){
    /*
    ===== Variables =====
     */
    private val repository = RealmSyncRepository { _, _ ->
        lifecycleScope.launch {
        }
    }

    private val compassViewModel: CompassViewModel by viewModels{
        CompassViewModel.factory(repository, this)
    }

    // App will crash if trying to set package name at this point
    // ... so set it later
    private var compassCommunication: CompassCommunication? = null

    /**
     * Request code for verifying call to [requestPermissions]
     */
    private val REQUEST_CODE_REQUIRED_PERMISSIONS = 1


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

        compassCommunication = CompassCommunication(
            userId = repository.getCurrentUserId(),
            packageName = packageName
        )
        // Need to create connections client in compass screen communication class
        compassCommunication!!.setConnectionsClient(this)
        compassCommunication!!.setCompassViewModel(compassViewModel)

        setContent{
            MyApplicationTheme {
                CompassScreenLayout(
                    compassViewModel = compassViewModel,
                    compassCommunication = compassCommunication!!
                )
            }
        }
    }

    @CallSuper
    override fun onStart() {
        super.onStart()
        // Check that the required permissions are allowed by the user
        // Ask user to grant permissions if they are not allowed already
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_REQUIRED_PERMISSIONS
            )
        }
        // Ask for permissions before allowing device connections
        if (checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
            || checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
            || checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
            || checkSelfPermission(Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE
                ),
                REQUEST_CODE_REQUIRED_PERMISSIONS
            )
        }

        // This screen is entered only when the matched user accepts the connection
        // ... so as soon as this screen is shown, start the connection process
        compassCommunication!!.updateConnectionType(CompassConnectionType.WAITING)
    }

    @Deprecated("Deprecated in Java")
    @CallSuper
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // If user does not grant any required app permissions, show error that app cannot function without them
        val errMsg = "Cannot start without required permissions"
        if (requestCode == REQUEST_CODE_REQUIRED_PERMISSIONS) {
            grantResults.forEach {
                if (it == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, errMsg, Toast.LENGTH_LONG).show()
                    finish()
                    return
                }
            }
            recreate()
        }
    }

    @CallSuper
    override fun onStop() {
        compassCommunication!!.updateConnectionType(CompassConnectionType.OFFLINE)
        // Release all assets when the Nearby API is no longer necessary
        compassCommunication!!.releaseAssets()
        super.onStop()
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
    compassCommunication: CompassCommunication,
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
                compassCommunication = compassCommunication,
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
    compassCommunication: CompassCommunication,
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
            compassViewModel = compassViewModel,
            compassCommunication = compassCommunication
        )
        TempCompassScreenLocationUpdating(
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
        // Connection is not yet established with matched user
        // Show button to go back
        else{
            SingleTextRow(
                textId = R.string.compass_screen_connection_error_message
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
            .fillMaxWidth()
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

/**
 * Displays a button for canceling directing matches toward each other
 */
@Composable
fun CompassScreenCancelButton(
    compassViewModel: CompassViewModel,
    compassCommunication: CompassCommunication,
    modifier: Modifier = Modifier
) {
    SingleButtonRow(
        onButtonClick = {
            Log.i(
                "tempTag",
                "User has canceled meeting with match"
            )
            compassViewModel.toggleMeetingWithMatch()
            compassCommunication.updateConnectionType(CompassConnectionType.OFFLINE)
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

/**
 * Displays temporary text showing users' locations
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
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = "Current location:\n" +
                        "\t\t${compassViewModel.currentUserLocation.value.latitude}\n" +
                        "\t\t${compassViewModel.currentUserLocation.value.longitude}"
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = "Match location:\n" +
                        "\t\t${compassViewModel.matchedUserLocation.value.latitude}\n" +
                        "\t\t${compassViewModel.matchedUserLocation.value.longitude}"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CompassScreenLayoutPreview() {
    MyApplicationTheme {
        val repository = MockRepository()
        val compassViewModel = CompassViewModel(repository = repository)
        CompassScreenLayout(
            compassViewModel = compassViewModel,
            compassCommunication = CompassCommunication(
                userId = "fakeUserId",
                packageName = "fakePackageName"
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