package com.mongodb.app.location
import android.Manifest
import android.annotation.SuppressLint
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.mongodb.app.R
import com.mongodb.app.presentation.userprofiles.UserProfileViewModel
import java.util.concurrent.TimeUnit

// Contribution: Marco Pacini
/**
 * This screen displays a clickable switch that enables location updates for the user
 * when the switch is checked, displays a dynamically updating list of nearby users
 * Location gathering based on example from https://github.com/android/platform-samples/tree/main/samples/location
 */

@SuppressLint("MissingPermission")
@Composable
fun LocationUpdatesScreen(userProfileViewModel: UserProfileViewModel) {
    val permissions = listOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
    )
    // Requires at least coarse permission
    PermissionBox(
        permissions = permissions,
        requiredPermissions = listOf(permissions.first()),
    ) {
        LocationUpdatesContent(
            usePreciseLocation = it.contains(Manifest.permission.ACCESS_FINE_LOCATION),
            userProfileViewModel = userProfileViewModel
        )
    }
}

@RequiresPermission(
    anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION],
)
@Composable
fun LocationUpdatesContent(usePreciseLocation: Boolean, userProfileViewModel: UserProfileViewModel) {
    // The location request that defines the location updates
    var locationRequest by remember {
        mutableStateOf<LocationRequest?>(null)
    }
    // Keeps track of received location updates as text
    var locationUpdates by remember {
        mutableStateOf("")
    }
    var isLookingForUsers by remember{
        mutableStateOf(false)
    }

    // Only register the location updates effect when we have a request
    if (locationRequest != null) {
        LocationUpdatesEffect(locationRequest!!) { result ->
            // For each result update the text
            for (currentLocation in result.locations) {
                locationUpdates = "System time: ${System.currentTimeMillis()}:\n" +
                        "Latitude: ${currentLocation.latitude}\n" +
                        "Longitude: ${currentLocation.longitude}\n" +
                        "Accuracy: ${currentLocation.accuracy}\n\n" //+
                        //locationUpdates

                // Update the user's the latitude and longitude
                userProfileViewModel.setUserProfileLocation(currentLocation.latitude, currentLocation.longitude)

                // After updating the user's location, fetch and store nearby user profiles in the viewmodel
                userProfileViewModel.fetchAndStoreNearbyUserProfiles()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(16.dp),
        //verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {

        // Toggle to start and stop location updates
        // before asking for periodic location updates,
        // it's good practice to fetch the current location
        // or get the last known location
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.height(150.dp))
            Text(text = stringResource(R.string.enable_location_updates))
            Spacer(modifier = Modifier.padding(8.dp))
            Switch(
                checked = locationRequest != null,
                onCheckedChange = { checked ->
                    isLookingForUsers = checked
                    locationRequest = if (checked) {
                        // Define the accuracy based on your needs and granted permissions
                        val priority = if (usePreciseLocation) {
                            Priority.PRIORITY_HIGH_ACCURACY
                        } else {
                            Priority.PRIORITY_BALANCED_POWER_ACCURACY
                        }
                        // Updates location at specified interval
                        LocationRequest.Builder(priority, TimeUnit.SECONDS.toMillis(1)).build()
                    } else {
                        null
                    }
                },
            )
        }
        Text(text = locationUpdates)
        ProximityRadiusAdjuster(userProfileViewModel)
        UserProfileDisplayList(userProfiles = userProfileViewModel.nearbyUserProfiles, isLookingForUsers)
    }
}

/**
 * An effect that request location updates based on the provided request and ensures that the
 * updates are added and removed whenever the composable enters or exists the composition.
 */
@RequiresPermission(
    anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION],
)
@Composable
fun LocationUpdatesEffect(
    locationRequest: LocationRequest,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onUpdate: (result: LocationResult) -> Unit,
) {
    val context = LocalContext.current
    val currentOnUpdate by rememberUpdatedState(newValue = onUpdate)

    // Whenever one of these parameters changes, dispose and restart the effect.
    DisposableEffect(locationRequest, lifecycleOwner) {
        val locationClient = LocationServices.getFusedLocationProviderClient(context)
        val locationCallback: LocationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                currentOnUpdate(result)
            }
        }
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                locationClient.requestLocationUpdates(
                    locationRequest, locationCallback, Looper.getMainLooper(),
                )
            } else if (event == Lifecycle.Event.ON_STOP) {
                locationClient.removeLocationUpdates(locationCallback)
            }
        }

        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the effect leaves the Composition, remove the observer
        onDispose {
            locationClient.removeLocationUpdates(locationCallback)
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}