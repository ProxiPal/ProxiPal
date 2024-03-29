@file:OptIn(ExperimentalMaterial3Api::class)

package com.mongodb.app.ui.compassscreen

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.mongodb.app.R
import com.mongodb.app.TAG
import com.mongodb.app.data.MockRepository
import com.mongodb.app.data.RealmSyncRepository
import com.mongodb.app.data.compassscreen.ALL_NEARBY_API_PERMISSIONS
import com.mongodb.app.data.compassscreen.ALL_NEARBY_API_PERMISSIONS_ARRAY
import com.mongodb.app.data.compassscreen.ALL_WIFIP2P_PERMISSIONS
import com.mongodb.app.data.compassscreen.CompassConnectionType
import com.mongodb.app.presentation.compassscreen.CompassNearbyAPI
import com.mongodb.app.presentation.compassscreen.CompassViewModel
import com.mongodb.app.presentation.compassscreen.WiFiDirectBroadcastReceiver
import com.mongodb.app.presentation.userprofiles.UserProfileViewModel
import com.mongodb.app.ui.components.SingleButtonRow
import com.mongodb.app.ui.components.SingleTextRow
import com.mongodb.app.ui.theme.MyApplicationTheme
import com.mongodb.app.ui.theme.Purple200
import kotlinx.coroutines.launch


/*
Contributions:
- Kevin Kubota (entire file, excluding navigation between screens)
 */


class CompassScreen : ComponentActivity() {
    /*
    ===== Variables =====
     */
    private val repository = RealmSyncRepository { _, _ ->
        lifecycleScope.launch {
        }
    }

    private val compassViewModel: CompassViewModel by viewModels {
        CompassViewModel.factory(repository, this)
    }

    // TODO May need to use UserProfileViewModel instance created in UserProfileScreen instead of this
    private val userProfileViewModel: UserProfileViewModel by viewModels{
        UserProfileViewModel.factory(repository, this)
    }


    // region NearbyAPI
    private lateinit var compassNearbyAPI: CompassNearbyAPI

    /**
     * Request code for verifying call to [requestPermissions]
     */
    private val REQUEST_CODE_REQUIRED_PERMISSIONS = 1
    // endregion NearbyAPI


    // region WifiP2P
//    private val manager: WifiP2pManager? by lazy(LazyThreadSafetyMode.NONE) {
//        getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager?
//    }

    private lateinit var channel: WifiP2pManager.Channel
    private lateinit var manager: WifiP2pManager

    private var receiver: WiFiDirectBroadcastReceiver? = null

    private val intentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)

        // These may not be necessary
        addAction(BluetoothDevice.ACTION_FOUND)
        addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
    }
    // endregion WifiP2P


    /*
    ===== Functions =====
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i(
            TAG(),
            "CompassScreen: Start of OnCreate()"
        )

        // Needed when working with either the Nearby API or Wifi P2P Direct
        verifyPermissions2()

        // Need to update repository when a configuration change occurs
        // ... otherwise app will crash when trying to access Realm after it has closed
        compassViewModel.updateRepository(
            newRepository = repository
        )

        compassViewModel.setViewModels(userProfileViewModel)


        // region NearbyAPI
        compassNearbyAPI = CompassNearbyAPI(
            userId = repository.getCurrentUserId(),
            packageName = packageName
        )
        // Need to create connections client in compass screen communication class
        compassNearbyAPI.setConnectionsClient(this)
        compassNearbyAPI.setCompassViewModel(compassViewModel)
        // endregion NearbyAPI


        // region WifiP2P
        manager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(this, mainLooper, null)
        // endregion WifiP2P


        setContent {
            MyApplicationTheme {
                CompassScreenLayout(
                    compassViewModel = compassViewModel,
                    compassNearbyAPI = compassNearbyAPI
                )
            }
        }
    }

    @CallSuper
    override fun onStart() {
        super.onStart()


        // region NearbyAPI
        // This screen is entered only when the matched user accepts the connection
        // ... so as soon as this screen is shown, start the connection process
        compassNearbyAPI.updateConnectionType(CompassConnectionType.WAITING)

        // TODO Temporarily and quickly allow showing compass updating
        compassNearbyAPI.updateConnectionType(CompassConnectionType.MEETING)
        // endregion NearbyAPI
    }

    override fun onResume() {
        super.onResume()


        // region WifiP2P
        // Register the broadcast receiver with the intent values to be matched
        receiver = WiFiDirectBroadcastReceiver(manager, channel, this)
        registerReceiver(receiver, intentFilter)

        receiver?.discoverPeers()
        receiver?.requestPeers()

        receiver?.tempCheckForPeers()
        // endregion WifiP2P
    }

    override fun onPause() {
        super.onPause()


        // region WifiP2P
        // Unregister the broadcast receiver
        unregisterReceiver(receiver)
        // endregion WifiP2P
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
        // region NearbyAPI
        compassNearbyAPI.updateConnectionType(CompassConnectionType.OFFLINE)
        // Release all assets when the Nearby API is no longer necessary
        compassNearbyAPI.releaseAssets()
        // endregion NearbyAPI


        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()

        // Repository must be closed to free resources
        repository.close()
    }

    @Deprecated(
        message = "This does not work 100%; " +
                "\"Missing required permissions, aborting call to <advertisingFunctionName> ...\""
    )
    private fun verifyPermissions(){
        // Ask for permissions before allowing device connections
        // Check that the required permissions are allowed by the user
        var hasAllPermissionsGranted = true
        for (permission in ALL_NEARBY_API_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                hasAllPermissionsGranted = false
                break
            }
        }
        // Ask user to grant permissions if they are not allowed already
        if (!hasAllPermissionsGranted) {
            Log.i(
                TAG(),
                "CompassScreen: Not all permissions are granted, asking now"
            )
            ActivityCompat.requestPermissions(
                this, ALL_NEARBY_API_PERMISSIONS_ARRAY, REQUEST_CODE_REQUIRED_PERMISSIONS
            )
        }
        else{
            Log.i(
                TAG(),
                "CompassScreen: It seems permissions are already granted, but asking anyway"
            )
            ActivityCompat.requestPermissions(
                this, ALL_NEARBY_API_PERMISSIONS_ARRAY, REQUEST_CODE_REQUIRED_PERMISSIONS
            )
        }
    }

    private fun verifyPermissions2(){
        // TODO Should show permission rationale with shouldShowRequestPermissionRationale()
        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
                isGranted: Boolean ->
            Log.i(
                TAG(),
                "CompassScreen: Permission launcher granted? = \"$isGranted\""
            )
        }

        for (permission in ALL_NEARBY_API_PERMISSIONS + ALL_WIFIP2P_PERMISSIONS){
            when {
                ContextCompat.checkSelfPermission(this, permission)
                        == PackageManager.PERMISSION_GRANTED -> {
                    Log.i(
                        TAG(),
                        "CompassScreen: Permission granted = \"$permission\""
                    )
                }

                ActivityCompat.shouldShowRequestPermissionRationale(
                    this, permission
                ) -> {
                    Log.i(
                        TAG(),
                        "CompassScreen: Permission denied = \"$permission\""
                    )
                    // Show some UI for rationale behind requesting a permission here
                }

                else -> {
                    Log.i(
                        TAG(),
                        "CompassScreen: Permission unasked = \"$permission\""
                    )
                    requestPermissionLauncher.launch(
                        permission
                    )
                    val temp = (ContextCompat.checkSelfPermission(this, permission)
                    == PackageManager.PERMISSION_GRANTED)
                    Log.i(
                        TAG(),
                        "CompassScreen: Permission after asking = \"$temp\""
                    )
                }
            }
        }
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
    compassNearbyAPI: CompassNearbyAPI,
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
            compassNearbyAPI = compassNearbyAPI,
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
                        compassNearbyAPI.updateConnectionType(CompassConnectionType.OFFLINE)
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
                    onButtonClick = { onBackButtonClick() }
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
                    onButtonClick = { onBackButtonClick() }
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
                text = "Current location:\n${compassViewModel.getCurrentUserLocation()}"
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = "Match location:\n${compassViewModel.getMatchedUserLocation()}"
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
            compassNearbyAPI = CompassNearbyAPI(
                userId = "fakeUserId",
                packageName = "fakePackageName"
            )
        )
    }
}