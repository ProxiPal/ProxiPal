package com.mongodb.app.presentation.compassscreen

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.mongodb.app.TAG
import java.net.ServerSocket

/**
 * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
 */
class WiFiDirectBroadcastReceiver(
    private val manager: WifiP2pManager,
    private val channel: WifiP2pManager.Channel,
    private val activity: Activity
) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null && intent.action != null){
            when (intent.action!!){
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    // Check to see if Wi-Fi is enabled and notify appropriate activity
                    when (intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)) {
                        WifiP2pManager.WIFI_P2P_STATE_ENABLED -> {
                            // Wifi P2P is enabled
                            Log.i(
                                TAG(),
                                "WiFiDirectBroadcastReceiver: State changed and Wi-Fi P2P is enabled"
                            )
                        }
                        else -> {
                            // Wi-Fi P2P is not enabled
                            Log.i(
                                TAG(),
                                "WiFiDirectBroadcastReceiver: State changed and Wi-Fi P2P is disabled"
                            )
                        }
                    }
                }
                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    // Call WifiP2pManager.requestPeers() to get a list of current peers
                    // Permission check
                    if (ActivityCompat.checkSelfPermission(
                            activity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                            activity,
                            Manifest.permission.NEARBY_WIFI_DEVICES
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        Log.i(
                            TAG(),
                            "WiFiDirectBroadcastReceiver: Could not receive without permissions"
                        )
                        return
                    }
                    Log.i(
                        TAG(),
                        "WiFiDirectBroadcastReceiver: Peers changed"
                    )
                    manager.requestPeers(channel) { peers: WifiP2pDeviceList? ->
                        connectToPeers(peers)
                    }
                }
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    // Respond to new connection or disconnections
                    Log.i(
                        TAG(),
                        "WiFiDirectBroadcastReceiver: Connection changed"
                    )
                }
                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                    // Respond to this device's wifi state changing
                    Log.i(
                        TAG(),
                        "WiFiDirectBroadcastReceiver: This device changed"
                    )
                }
            }
        }
    }

    /**
     * Starts the process for discovering peers to connect to
     */
    fun discoverPeers(){
        // Permission check
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.i(
                TAG(),
                "WiFiDirectBroadcastReceiver: Could not discover without permissions"
            )
            return
        }
        // Initiates peer discovery
        manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {

            /**
             * Successful in initiating discovery
             */
            override fun onSuccess() {
                // Code for when the discovery initiation is successful goes here.
                // No services have actually been discovered yet, so this method
                // can often be left blank. Code for peer discovery goes in the
                // onReceive method, detailed below.
                Log.i(
                    TAG(),
                    "WiFiDirectBroadcastReceiver: Discover peers successful"
                )
            }

            /**
             * Failed in initiating discovery
             */
            override fun onFailure(reasonCode: Int) {
                // Code for when the discovery initiation fails goes here.
                // Alert the user that something went wrong.
                Log.i(
                    TAG(),
                    "WiFiDirectBroadcastReceiver: Discover peers failed"
                )
            }
        })
    }

    fun requestPeers(){
        // Permission check
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.i(
                TAG(),
                "WiFiDirectBroadcastReceiver: Could not request without permissions"
            )
            return
        }
        manager.requestPeers(channel) { peers: WifiP2pDeviceList? ->
            Log.i(
                TAG(),
                "WiFiDirectBroadcastReceiver: Current peer amount = \"${peers?.deviceList?.size}\""
            )
        }
    }

    fun connectToPeers(peers: WifiP2pDeviceList?){
        if (peers == null) return
        // Get the first peer device
        val device: WifiP2pDevice = peers.deviceList.elementAt(0)
        val config = WifiP2pConfig()
        config.deviceAddress = device.deviceAddress
        channel.also { channel ->
            // Permission check
            if (ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.NEARBY_WIFI_DEVICES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Log.i(
                    TAG(),
                    "WiFiDirectBroadcastReceiver: Could not connect without permissions"
                )
                return
            }
            manager.connect(channel, config, object : WifiP2pManager.ActionListener {

                override fun onSuccess() {
                    //success logic
                    Log.i(
                        TAG(),
                        "WiFiDirectBroadcastReceiver: Connect to peers successful"
                    )
                }

                override fun onFailure(reason: Int) {
                    //failure logic
                    Log.i(
                        TAG(),
                        "WiFiDirectBroadcastReceiver: Connect to peers failed"
                    )
                }
            }
            )
        }
    }
}