@file:OptIn(ExperimentalMaterial3Api::class)

package com.mongodb.app.ui.compassscreen

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.mongodb.app.R
import com.mongodb.app.data.MockRepository
import com.mongodb.app.data.compassscreen.CompassConnectionType
import com.mongodb.app.data.compassscreen.CompassPermissionHandler
import com.mongodb.app.navigation.Routes
import com.mongodb.app.presentation.compassscreen.CompassNearbyAPI
import com.mongodb.app.presentation.compassscreen.CompassViewModel
import com.mongodb.app.ui.components.SingleButtonRow
import com.mongodb.app.ui.components.SingleTextRow
import com.mongodb.app.ui.theme.MyApplicationTheme
import com.mongodb.app.ui.theme.Purple200
import com.mongodb.app.ui.userprofiles.UserProfileScreen


/*
Contributions:
- Kevin Kubota (entire file, excluding navigation between screens)
 */


/**
 * Displays the screen that points matching users toward each other
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompassScreenLayout(
    compassViewModel: CompassViewModel,
    compassPermissionHandler: CompassPermissionHandler,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    compassViewModel.refreshUserProfileInstances()

    if (compassPermissionHandler.areAllPermissionsGranted()){
        Log.e(
            "CompassPermissionHandler",
            "All permissions are granted"
        )
        compassPermissionHandler.startSetup()

        Scaffold(
            topBar = {
                CompassScreenTopBar()
            },
            modifier = modifier
            // Pad the body of content so it does not get cut off by the scaffold top bar
        ) { innerPadding ->
            CompassScreenBodyContent(
                compassViewModel = compassViewModel,
                compassNearbyAPI = compassPermissionHandler.compassNearbyAPI,
                navController = navController,
                modifier = Modifier
                    .padding(innerPadding)
            )
        }
    }
    else{
        Log.e(
            "CompassPermissionHandler",
            "1 or more permissions are not granted"
        )
        compassPermissionHandler.requestPermissions()
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
    compassNearbyAPI: CompassNearbyAPI,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val onBackButtonClick = {
//        compassNearbyAPI.updateConnectionType(CompassConnectionType.OFFLINE)

        // TODO Remove this later; Right now is for temporarily and quickly showing compass updating
        compassNearbyAPI.updateConnectionType(CompassConnectionType.MEETING)
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        when (compassNearbyAPI.connectionType.value) {
            CompassConnectionType.MEETING -> {
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
                CompassScreenCurrentLocations(
                    compassViewModel = compassViewModel,
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 8.dp)
                )
                CompassScreenReturnButton(
                    compassNearbyAPI = compassNearbyAPI,
                    onButtonClick = {
                        navController.navigate(Routes.FriendListScreen.route)
//                        compassNearbyAPI.updateConnectionType(CompassConnectionType.OFFLINE)
                    }
                )
            }
            // Connection is not yet established with matched user
            // Show button to go back
            CompassConnectionType.WAITING -> {
                SingleTextRow(
                    textId = R.string.compass_screen_awaiting_connection_message,
                    isTextBold = false,
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 8.dp)
                )
                CompassScreenReturnButton(
                    compassNearbyAPI = compassNearbyAPI,
                    onButtonClick = {
                        navController.navigate(Routes.FriendListScreen.route)
//                        onBackButtonClick()
                    }
                )
            }
            // The current or matched user canceled the connection
            CompassConnectionType.OFFLINE -> {
                SingleTextRow(
                    textId = R.string.compass_screen_canceled_connection_message,
                    isTextBold = false,
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 8.dp)
                )
                CompassScreenReturnButton(
                    compassNearbyAPI = compassNearbyAPI,
                    onButtonClick = {
                        navController.navigate(Routes.FriendListScreen.route)
//                        onBackButtonClick()
                    }
                )
            }
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
            text = stringResource(id = measurementText, measurement),
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Displays the back button for returning to the connect with others screen
 */
@Composable
fun CompassScreenReturnButton(
    compassNearbyAPI: CompassNearbyAPI,
    onButtonClick: (() -> Unit),
    modifier: Modifier = Modifier
) {
    SingleButtonRow(
        onButtonClick = {
            onButtonClick()
        },
        textId =
        if (compassNearbyAPI.connectionType.value == CompassConnectionType.MEETING)
            R.string.compass_screen_cancel_button
        else
            R.string.compass_screen_return_button,
        modifier = modifier
    )
}

/**
 * Displays temporary text showing users' locations
 */
@Composable
fun CompassScreenCurrentLocations(
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
                        "\tLat. ${compassViewModel.currentUserLocation.first}\n" +
                        "\tLong. ${compassViewModel.currentUserLocation.second}"
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = "Match location:\n" +
                        "\tLat. ${compassViewModel.focusedUserLocation.first}\n" +
                        "\tLong. ${compassViewModel.focusedUserLocation.second}"
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
            compassPermissionHandler = CompassPermissionHandler(
                repository = repository,
                activity = UserProfileScreen(),
                compassViewModel = compassViewModel
            ),
            navController = rememberNavController()
        )
    }
}