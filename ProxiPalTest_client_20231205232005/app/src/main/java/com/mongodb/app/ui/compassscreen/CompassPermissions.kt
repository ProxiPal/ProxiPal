package com.mongodb.app.ui.compassscreen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.mongodb.app.ui.theme.MyApplicationTheme

class CompassPermissions : ComponentActivity() {
    /*
    ===== Variables =====
     */
    private val requiredPermissions = listOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.NEARBY_WIFI_DEVICES,
        Manifest.permission.BLUETOOTH_SCAN
    )
    var areAllPermissionsGranted = true


    /*
    ===== Functions =====
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkPermissions()

        Log.i(
            "tempTag",
            "CompassPermissions: All permissions allowed? = \"$areAllPermissionsGranted\""
        )

        setContent {
            MyApplicationTheme {
                CompassPermissionsLayout(
                    requiredPermissions = requiredPermissions
                )
            }
        }
    }

    private fun checkPermissions() {
        for (permission in requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                areAllPermissionsGranted = false
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CompassPermissionsLayout(
    requiredPermissions: List<String>,
    modifier: Modifier = Modifier
) {
//    // All permissions needed for connecting with other devices
//    val permissionStateBluetooth = rememberPermissionState(
//        permission = Manifest.permission.BLUETOOTH
//    )
//    val permissionStateBluetoothAdmin = rememberPermissionState(
//        permission = Manifest.permission.BLUETOOTH_ADMIN
//    )
//    val permissionStateAccessWifiState = rememberPermissionState(
//        permission = Manifest.permission.ACCESS_WIFI_STATE
//    )
//    val permissionStateChangeWifiState = rememberPermissionState(
//        permission = Manifest.permission.CHANGE_WIFI_STATE
//    )
//    // This is already checked in another file
//    val permissionStateAccessFineLocation = rememberPermissionState(
//        permission = Manifest.permission.ACCESS_FINE_LOCATION
//    )
//    val permissionStateNearbyWifiDevices = rememberPermissionState(
//        permission = Manifest.permission.NEARBY_WIFI_DEVICES
//    )
//    val permissionStateBluetoothScan = rememberPermissionState(
//        permission = Manifest.permission.BLUETOOTH_SCAN
//    )
//    val deviceConnectionPermissions = listOf(
//        permissionStateBluetooth,
//        permissionStateBluetoothAdmin,
//        permissionStateAccessWifiState,
//        permissionStateChangeWifiState,
//        permissionStateAccessFineLocation,
//        permissionStateNearbyWifiDevices,
//        permissionStateBluetoothScan
//    )
//    val deviceConnectionPermissionStrings = deviceConnectionPermissions.map {
//        it.toString()
//    }

    val multiplePermissionState = rememberMultiplePermissionsState(
        permissions = requiredPermissions
    )
    var shouldShowPopup by remember(multiplePermissionState) {
        mutableStateOf(true)
    }

    CompassPermissionsPopup(
        requiredPermissions = requiredPermissions,
        shouldShowPopup = shouldShowPopup,
        onPermissionsAllowed = {
            multiplePermissionState.launchMultiplePermissionRequest()
            shouldShowPopup = false
        },
        onPermissionsDenied = {
            shouldShowPopup = false
        },
        modifier = modifier
    )
}

/**
 * Displays an alert dialog to let user decide whether to accept or deny permissions
 * for connecting to other devices
 */
@Composable
fun CompassPermissionsPopup(
    requiredPermissions: List<String>,
    shouldShowPopup: Boolean,
    onPermissionsAllowed: (() -> Unit),
    onPermissionsDenied: (() -> Unit),
    modifier: Modifier = Modifier
) {
    var permissionsString = ""
    for (permission in requiredPermissions) {
        permissionsString += "\n* "
        // Shorten permission names when printed out, if possible
        permissionsString += if (permission.indexOf("android.permission.") != -1) {
            permission.substringAfter("android.permission.")
        } else {
            permission
        }
    }
    var shouldShowPermissionsDeniedMessage by remember { mutableStateOf(false) }

    if (shouldShowPopup) {
        AlertDialog(
            onDismissRequest = {
                onPermissionsDenied()
                shouldShowPermissionsDeniedMessage = true
            },
            title = {
                Text(
                    text = "Required permissions: $permissionsString"
                )
            },
            text = {
                Text(
                    text = "To be able to detect and connect with other nearby devices, " +
                            "these permissions are required"
                )
            },
            confirmButton = {
                Button(
                    onClick = { onPermissionsAllowed() }
                ) {
                    Text(
                        text = "Confirm"
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        onPermissionsDenied()
                        shouldShowPermissionsDeniedMessage = true
                    }
                ) {
                    Text(
                        text = "Dismiss"
                    )
                }
            },
            modifier = modifier
        )
    }
    if (!shouldShowPopup && shouldShowPermissionsDeniedMessage) {
        CompassPermissionsNotGrantedMessage()
    }
}

@Preview(showBackground = true)
@Composable
fun CompassPermissionsPopupPreview() {
    MyApplicationTheme {
        val dummyPermissions = listOf(
            "dummyPermission0",
            "dummyPermission1",
            Manifest.permission.BLUETOOTH
        )
        CompassPermissionsPopup(
            requiredPermissions = dummyPermissions,
            shouldShowPopup = true,
            onPermissionsAllowed = {},
            onPermissionsDenied = {}
        )
    }
}

/**
 * Displays a simple message when the user does not allow the required permissions
 */
@Composable
fun CompassPermissionsNotGrantedMessage(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Text(
            text = "Cannot connect to other devices without required permissions",
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CompassPermissionsNotGrantedMessagePreview() {
    MyApplicationTheme {
        CompassPermissionsNotGrantedMessage()
    }
}