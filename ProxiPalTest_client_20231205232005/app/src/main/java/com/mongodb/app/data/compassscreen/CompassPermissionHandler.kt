package com.mongodb.app.data.compassscreen

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mongodb.app.TAG
import com.mongodb.app.data.SyncRepository
import com.mongodb.app.presentation.compassscreen.CompassNearbyAPI
import com.mongodb.app.presentation.compassscreen.CompassViewModel
import com.mongodb.app.presentation.compassscreen.WiFiDirectBroadcastReceiver
import com.mongodb.app.ui.userprofiles.UserProfileScreen


/**
 * For isolating compass screen permission checking and requesting
 */
class CompassPermissionHandler(
    private val repository: SyncRepository,
    private val userProfileScreen: UserProfileScreen,
    private val compassViewModel: CompassViewModel
) {
    // region Variables


    // region NearbyAPI
    private lateinit var _compassNearbyAPI: CompassNearbyAPI
    val compassNearbyAPI
        get() = _compassNearbyAPI
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


    // endregion Variables

    fun onCreate(){
        // Needed when working with either the Nearby API or Wifi P2P Direct
        verifyPermissions()

        // region NearbyAPI
        _compassNearbyAPI = CompassNearbyAPI(
            userId = repository.getCurrentUserId(),
            packageName = userProfileScreen.packageName
        )
        // Need to create connections client in compass screen communication class
        _compassNearbyAPI.setConnectionsClient(userProfileScreen)
        _compassNearbyAPI.setCompassViewModel(compassViewModel)
        // endregion NearbyAPI

        // region WifiP2P
        manager = userProfileScreen
            .getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(
            userProfileScreen,
            userProfileScreen.mainLooper,
            null)
        // endregion WifiP2P
    }

    fun onStart(){
        // region NearbyAPI
        // This screen is entered only when the matched user accepts the connection
        // ... so as soon as this screen is shown, start the connection process
        compassNearbyAPI.updateConnectionType(CompassConnectionType.WAITING)

        // TODO Temporarily and quickly allow showing compass updating
        compassNearbyAPI.updateConnectionType(CompassConnectionType.MEETING)
        // endregion NearbyAPI
    }

    fun onResume(){
        // region WifiP2P
        // Register the broadcast receiver with the intent values to be matched
        receiver = WiFiDirectBroadcastReceiver(manager, channel, userProfileScreen)
        userProfileScreen.registerReceiver(receiver, intentFilter)

        receiver?.discoverPeers()
        receiver?.requestPeers()

        receiver?.tempCheckForPeers()
        // endregion WifiP2P
    }

    fun onPause(){
        // region WifiP2P
        // Unregister the broadcast receiver
        userProfileScreen.unregisterReceiver(receiver)
        // endregion WifiP2P
    }

    fun onStop(){
        // region NearbyAPI
        compassNearbyAPI.updateConnectionType(CompassConnectionType.OFFLINE)
        // Release all assets when the Nearby API is no longer necessary
        compassNearbyAPI.releaseAssets()
        // endregion NearbyAPI
    }

    private fun verifyPermissions(){
        // TODO Should show permission rationale with shouldShowRequestPermissionRationale()
        val requestPermissionLauncher = userProfileScreen
            .registerForActivityResult(ActivityResultContracts.RequestPermission()){
                isGranted: Boolean ->
            Log.i(
                TAG(),
                "CompassScreen: Permission launcher granted? = \"$isGranted\""
            )
        }

        for (permission in ALL_NEARBY_API_PERMISSIONS + ALL_WIFIP2P_PERMISSIONS){
            when {
                ContextCompat.checkSelfPermission(userProfileScreen, permission)
                        == PackageManager.PERMISSION_GRANTED -> {
                    Log.i(
                        TAG(),
                        "CompassScreen: Permission granted = \"$permission\""
                    )
                }

                ActivityCompat.shouldShowRequestPermissionRationale(
                    userProfileScreen, permission
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
                    val temp = (ContextCompat.checkSelfPermission(
                        userProfileScreen, permission)
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