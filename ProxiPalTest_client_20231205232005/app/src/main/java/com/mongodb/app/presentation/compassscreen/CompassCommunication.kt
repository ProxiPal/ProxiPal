package com.mongodb.app.presentation.compassscreen

import android.app.Activity
import android.util.Log
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

class CompassCommunication constructor(
    private val compassViewModel: CompassViewModel,
    private val packageName: String
){
    /*
    ===== Variables =====
     */
    /**
     * From Nearby Connections API, how to discover and connect to other devices.
     * P2P_STAR = Can discover multiple devices, but can only communicate with 1 at a time
     */
    private val strategy: Strategy = Strategy.P2P_STAR

    private lateinit var connectionsClient: ConnectionsClient

    /**
     * Used to track data of matched user
     */
    private var matchedUserEndpointId: String? = null

    /**
     * Callback for receiving payloads
     */
    private val payloadCallback: PayloadCallback = object: PayloadCallback(){
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            payload.asBytes()?.let{
                // The byte array sent should only have 2 elements (latitude, longitude)
                compassViewModel.updateMatchedUserLatitude(it[0].toDouble())
                compassViewModel.updateMatchedUserLongitude(it[1].toDouble())
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            if (update.status == PayloadTransferUpdate.Status.SUCCESS){
                Log.i(
                    "tempTag",
                    "CompassCommunication: OnPayloadTransferUpdate() payload transfer success"
                )
                // TODO Logic for after the payload transfer update between users goes here
                // Update UI and view model values here
            }
        }
    }

    private val connectionLifecycleCallback = object: ConnectionLifecycleCallback(){
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess){
                connectionsClient.stopAdvertising()
                connectionsClient.stopDiscovery()
                matchedUserEndpointId = endpointId
            }
        }

        override fun onDisconnected(endpointId: String) {
        }
    }

    private val endpointDiscoveryCallback = object: EndpointDiscoveryCallback(){
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            connectionsClient.requestConnection("tempName", packageName, connectionLifecycleCallback)
        }

        override fun onEndpointLost(endpointId: String) {
            TODO("Not yet implemented")
        }
    }


    /*
    ===== Functions =====
     */
    /**
     * Creates the connections client (will be called in the compass screen activity since it requires
     * an activity to be created)
     */
    fun setConnectionsClient(activity: Activity){
        connectionsClient = Nearby.getConnectionsClient(activity)
    }

    /**
     * Releases all assets, for when the Nearby API is no longer necessary
     */
    fun releaseAssets(){
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
    fun sendCurrentLocation(currentLatitude: Double, currentLongitude: Double){
        connectionsClient.sendPayload(
            matchedUserEndpointId!!,
            // Need a ByteArray, not to be confused with an Array<Byte>
            Payload.fromBytes(byteArrayOf(
                currentLatitude.toInt().toByte(),
                currentLongitude.toInt().toByte()
            ))
        )
    }

    /**
     * Starts advertising, or allowing other users nearby the possibility to connect with you
     */
    fun startAdvertising(){
        val options = AdvertisingOptions.Builder().setStrategy(strategy).build()
        // TODO Advertising may fail, so need to handle cases when it does
        connectionsClient.startAdvertising(
            "tempName", packageName, connectionLifecycleCallback, options
        )
    }

    /**
     * Starts discovering, or finding other users nearby to connect to
     */
    fun startDiscovery(){
        val options = DiscoveryOptions.Builder().setStrategy(strategy).build()
        connectionsClient.startDiscovery(packageName, endpointDiscoveryCallback, options)
    }

    /**
     * Resets all necessary values in preparation for meeting with another matched user
     */
    private fun resetCompassMatchData(){
        matchedUserEndpointId = null
    }
}