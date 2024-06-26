package com.mongodb.app.ui.compassscreen

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.mongodb.app.R
import com.mongodb.app.TAG
import com.mongodb.app.data.compassscreen.ALL_NEARBY_API_PERMISSIONS
import com.mongodb.app.ui.theme.MyApplicationTheme


/*
Contributions:
- Kevin Kubota (entire file, excluding navigation between screens, if any)
 */


/**
 * Displays the screen for the user to accept or deny permissions needed for communicating
 * with their matched user. Should show this screen first, if necessary, before CompassScreen
 */
@Deprecated(
    message = "Not currently being updated (or in use)"
)
class CompassPermissions : ComponentActivity() {
    /*
    ===== Functions =====
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i(
            TAG(),
            "CompassPermissions: All permissions allowed? = \"${areAllPermissionsGranted()}\""
        )

        setContent {
            MyApplicationTheme {
                CompassPermissionsLayout(
                    requiredPermissions = ALL_NEARBY_API_PERMISSIONS
                )
            }
        }
    }

    /**
     * Checks if all necessary permissions are granted or not
     */
    private fun areAllPermissionsGranted(): Boolean {
        for (permission in ALL_NEARBY_API_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }
}

/**
 * Displays the main permissions screen
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CompassPermissionsLayout(
    requiredPermissions: List<String>,
    modifier: Modifier = Modifier
) {
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

// TODO Permissions should not be asked every single time the user enters this screen if already granted
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
    val permissionDelimiter = "android.permission."
    for (permission in requiredPermissions) {
        permissionsString += "\n* "
        // Shorten permission names when printed out, if possible
        permissionsString += if (permission.indexOf(permissionDelimiter) != -1) {
            permission.substringAfter(permissionDelimiter)
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
                    text = stringResource(
                        id = R.string.compass_permissions_required_permissions_list,
                        permissionsString
                    )
                )
            },
            text = {
                Text(
                    text = stringResource(
                        id = R.string.compass_permissions_required_permissions_rationale
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = { onPermissionsAllowed() }
                ) {
                    Text(
                        text = stringResource(
                            id = R.string.compass_permissions_confirm_permissions_button
                        )
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
                        text = stringResource(
                            id = R.string.compass_permissions_dismiss_permissions_button
                        )
                    )
                }
            },
            modifier = modifier
        )
    }
    if (!shouldShowPopup && shouldShowPermissionsDeniedMessage) {
        CompassPermissionsDenied()
    }
}

@Preview(showBackground = true)
@Composable
fun CompassPermissionsPopupPreview() {
    MyApplicationTheme {
        CompassPermissionsPopup(
            requiredPermissions = ALL_NEARBY_API_PERMISSIONS,
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
fun CompassPermissionsDenied(
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
    ) {
        Text(
            text = stringResource(
                id = R.string.compass_permissions_screen_permissions_denied
            ),
            textAlign = TextAlign.Center,
            softWrap = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CompassPermissionsDeniedPreview() {
    MyApplicationTheme {
        CompassPermissionsDenied()
    }
}