package com.mongodb.app.presentation.compassscreen

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.mongodb.app.TAG
import com.mongodb.app.data.compassscreen.CompassConnectionType

class CompassCommunication constructor(
    private val userId: String,
    private val packageName: String
) {
    /*
    ===== Variables =====
     */
    /**
     * From Nearby Connections API, how to discover and connect to other devices.
     * P2P_STAR = Can discover multiple devices, but can only communicate with 1 at a time
     */
    private val strategy: Strategy = Strategy.P2P_STAR

    private lateinit var connectionsClient: ConnectionsClient

    private lateinit var compassViewModel: CompassViewModel

    /**
     * Used to track data of matched user
     */
    private var matchedUserEndpointId: String? = null

    private val _connectionType: MutableState<CompassConnectionType> =
        mutableStateOf(CompassConnectionType.OFFLINE)


    /*
    ===== Properties =====
     */
    val connectionType: State<CompassConnectionType>
        get() = _connectionType


    /*
    ===== Callback Objects =====
     */
    // region CallbackObjects
    /**
     * Callback for receiving payloads
     */
    private val payloadCallback: PayloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            Log.i(
                TAG(),
                "CompassCommunication: Start of OnPayloadReceived()"
            )
            payload.asBytes()?.let {
                // The byte array sent should only have 2 elements (latitude, longitude)
                compassViewModel.updateMatchedUserLatitude(it[0].toDouble())
                compassViewModel.updateMatchedUserLongitude(it[1].toDouble())
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            Log.i(
                TAG(),
                "CompassCommunication: Start of OnPayloadTransferUpdate()"
            )
            if (update.status == PayloadTransferUpdate.Status.SUCCESS) {
                Log.i(
                    TAG(),
                    "CompassCommunication: Payload transfer success"
                )
                // TODO Logic for after the payload transfer update between users goes here
                // Update UI and view model values here
            }
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            Log.i(
                TAG(),
                "CompassCommunication: Start of OnConnectionInitiated()"
            )
            connectionsClient.acceptConnection(endpointId, payloadCallback)
            // For privacy concerns, should see https://developer.android.com/codelabs/nearby-connections#5
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            Log.i(
                TAG(),
                "CompassCommunication: Start of OnConnectionResult()"
            )
            if (result.status.isSuccess) {
                Log.i(
                    TAG(),
                    "CompassCommunication: Connection success"
                )
                connectionsClient.stopAdvertising()
                connectionsClient.stopDiscovery()
                matchedUserEndpointId = endpointId
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.i(
                TAG(),
                "CompassCommunication: Start of OnDisconnected()"
            )
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Log.i(
                TAG(),
                "CompassCommunication: Start of OnEndpointFound()"
            )
            connectionsClient.requestConnection(
                userId,
                packageName,
                connectionLifecycleCallback
            )
        }

        override fun onEndpointLost(endpointId: String) {
            Log.i(
                TAG(),
                "CompassCommunication: Start of OnEndpointLost()"
            )
        }
    }
    // endregion CallbackObjects


    /*
    ===== Functions =====
     */
    /**
     * Creates the connections client (will be called in the compass screen activity since it requires
     * an activity to be created)
     */
    fun setConnectionsClient(activity: Activity) {
        connectionsClient = Nearby.getConnectionsClient(activity)
    }

    /**
     * Sets the compass view model (app crashes if assigning in constructor using CompassScreen's
     * "by viewModels{}")
     */
    fun setCompassViewModel(compassViewModel: CompassViewModel) {
        this.compassViewModel = compassViewModel
    }

    /**
     * Releases all assets, for when the Nearby API is no longer necessary
     */
    fun releaseAssets() {
        connectionsClient.apply {
            stopAdvertising()
            stopDiscovery()
            stopAllEndpoints()
        }
        resetCompassMatchData()
    }

    /**
     * Sends the current user's location to their matched user
     */
    fun sendCurrentLocation(currentLatitude: Double, currentLongitude: Double) {
        connectionsClient.sendPayload(
            matchedUserEndpointId!!,
            // Need a ByteArray, not to be confused with an Array<Byte>
            Payload.fromBytes(
                byteArrayOf(
                    currentLatitude.toInt().toByte(),
                    currentLongitude.toInt().toByte()
                )
            )
        )
    }

    /**
     * Starts advertising, or allowing other users nearby the possibility to connect with you
     */
    private fun startAdvertising() {
        Log.i(
            TAG(),
            "CompassCommunication: Start of advertising"
        )
        val options = AdvertisingOptions.Builder().setStrategy(strategy).build()
        // TODO Advertising may fail, so need to handle cases when it does
        connectionsClient.startAdvertising(
            userId,
            packageName,
            connectionLifecycleCallback,
            options
        )
    }

    /**
     * Starts discovering, or finding other users nearby to connect to
     */
    private fun startDiscovery() {
        Log.i(
            TAG(),
            "CompassCommunication: Start of discovery"
        )
        val options = DiscoveryOptions.Builder().setStrategy(strategy).build()
        connectionsClient.startDiscovery(packageName, endpointDiscoveryCallback, options)
    }

    private fun disconnect() {
        connectionsClient.disconnectFromEndpoint(matchedUserEndpointId.toString())
        resetCompassMatchData()
    }

    /**
     * Resets all necessary values in preparation for meeting with another matched user
     */
    private fun resetCompassMatchData() {
        matchedUserEndpointId = null
    }

    /**
     * Updates the current connection type
     */
    fun updateConnectionType(newConnectionType: CompassConnectionType) {
//        // Can only transition from OFFLINE to WAITING
//        if (_connectionType.value == CompassConnectionType.OFFLINE && newConnectionType != CompassConnectionType.MEETING){
//            _connectionType.value = newConnectionType
//            compassViewModel.updateConnectionType(newConnectionType)
//        }
//        // Can only transition from MEETING to OFFLINE
//        else if (_connectionType.value == CompassConnectionType.MEETING && newConnectionType != CompassConnectionType.OFFLINE){
//            _connectionType.value = newConnectionType
//            compassViewModel.updateConnectionType(newConnectionType)
//        }
        _connectionType.value = newConnectionType
        compassViewModel.updateConnectionType(newConnectionType)
        when (connectionType.value) {
            CompassConnectionType.OFFLINE -> {
                Log.i(
                    TAG(),
                    "CompassCommunication: Now OFFLINE"
                )
                disconnect()
            }
            CompassConnectionType.WAITING -> {
                Log.i(
                    TAG(),
                    "CompassCommunication: Now WAITING"
                )
                startDiscovery()
                startAdvertising()
            }
            CompassConnectionType.MEETING -> {
                Log.i(
                    TAG(),
                    "CompassCommunication: Now MEETING"
                )
            }
        }
    }
}