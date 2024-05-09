@file:OptIn(ExperimentalMaterial3Api::class)

package com.mongodb.app.ui.userprofiles

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.mongodb.app.ComposeLoginActivity
import com.mongodb.app.R
import com.mongodb.app.TAG
import com.mongodb.app.data.MockRepository
import com.mongodb.app.data.RealmSyncRepository
import com.mongodb.app.data.compassscreen.ALL_NEARBY_API_PERMISSIONS
import com.mongodb.app.data.compassscreen.ALL_WIFIP2P_PERMISSIONS
import com.mongodb.app.data.compassscreen.CompassConnectionType
import com.mongodb.app.data.userprofiles.USER_PROFILE_EDIT_MODE_MAXIMUM_LINE_AMOUNT
import com.mongodb.app.data.userprofiles.USER_PROFILE_ROW_HEADER_WEIGHT
import com.mongodb.app.friends.FriendRequestViewModel
import com.mongodb.app.home.HomeScreen
import com.mongodb.app.home.HomeViewModel
import com.mongodb.app.navigation.NavigationGraph
import com.mongodb.app.presentation.blocking_censoring.BlockingViewModel
import com.mongodb.app.presentation.blocking_censoring.CensoringViewModel
import com.mongodb.app.presentation.compassscreen.CompassNearbyAPI
import com.mongodb.app.presentation.compassscreen.CompassViewModel
import com.mongodb.app.presentation.compassscreen.WiFiDirectBroadcastReceiver
import com.mongodb.app.presentation.messages.MessagesViewModel
import com.mongodb.app.presentation.tasks.ToolbarEvent
import com.mongodb.app.presentation.tasks.ToolbarViewModel
import com.mongodb.app.presentation.userprofiles.AddUserProfileEvent
import com.mongodb.app.presentation.userprofiles.UserProfileViewModel
import com.mongodb.app.ui.components.MultiLineText
import com.mongodb.app.ui.components.ProxiPalBottomAppBar
import com.mongodb.app.ui.components.SingleLineText
import com.mongodb.app.ui.tasks.TaskAppToolbar
import com.mongodb.app.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch


/*
Contributions:
- Kevin Kubota (all user profile UI, except for navigation between screens)
 */


class UserProfileScreen : ComponentActivity() {
    /*
    ===== Variables =====
     */


    private val repository = RealmSyncRepository { _, error ->
        // Sync errors come from a background thread so route the Toast through the UI thread
        lifecycleScope.launch {
            // Catch write permission errors and notify user. This is just a 2nd line of defense
            // since we prevent users from modifying someone else's tasks
            // TODO the SDK does not have an enum for this type of error yet so make sure to update this once it has been added
            if (error.message?.contains("CompensatingWrite") == true) {
                Toast.makeText(
                    this@UserProfileScreen, getString(R.string.user_profile_permissions_warning),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }

    private val userProfileViewModel: UserProfileViewModel by viewModels {
        UserProfileViewModel.factory(repository, this)
    }

    private val toolbarViewModel: ToolbarViewModel by viewModels {
        ToolbarViewModel.factory(repository, this)
    }

    private val messagesViewModel: MessagesViewModel by viewModels {
        MessagesViewModel.factory(
            repository = repository,
            messagesRealm = repository,
            conversationsRealm = repository,
            this
        )
    }
    //april2
    private val friendRequestViewModel: FriendRequestViewModel by viewModels {
        FriendRequestViewModel.factory(repository)
    }

    private val blockingViewModel: BlockingViewModel by viewModels {
        BlockingViewModel.factory(
            repository = repository,
            blockingCensoringRealm = repository,
            this
        )
    }

    private val censoringViewModel: CensoringViewModel by viewModels {
        CensoringViewModel.factory(
            repository = repository,
            blockingCensoringRealm = repository,
            shouldReadCensoredTextOnInit = true,
            this
        )
    }

    private val compassViewModel: CompassViewModel by viewModels {
        CompassViewModel.factory(
            repository = repository,
            this
        )
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

        // region ViewModel events
        lifecycleScope.launch {
            userProfileViewModel.addUserProfileEvent
                .collect { fabEvent ->
                    when (fabEvent) {
                        is AddUserProfileEvent.Error ->
                            Log.e(TAG(), "${fabEvent.message}: ${fabEvent.throwable.message}")

                        is AddUserProfileEvent.Info ->
                            Log.e(TAG(), fabEvent.message)
                    }
                }
        }

        lifecycleScope.launch {
            toolbarViewModel.toolbarEvent
                .collect { toolbarEvent ->
                    when (toolbarEvent) {
                        ToolbarEvent.LogOut -> {
                            startActivity(
                                Intent(
                                    this@UserProfileScreen,
                                    ComposeLoginActivity::class.java
                                )
                            )
                            finish()
                        }

                        is ToolbarEvent.Info ->
                            Log.e(TAG(), toolbarEvent.message)

                        is ToolbarEvent.Error ->
                            Log.e(
                                TAG(),
                                "${toolbarEvent.message}: ${toolbarEvent.throwable.message}"
                            )
                    }
                }
        }
        // endregion ViewModel events


        // Needed when working with either the Nearby API or Wifi P2P Direct
        verifyPermissions()

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


        // Need to update repository when a configuration change occurs
        // ... otherwise app will crash when trying to access Realm after it has closed
        userProfileViewModel.updateRepository(
            newRepository = repository
        )
        messagesViewModel.updateRepository(
            newRepository = repository
        )
        blockingViewModel.updateRepositories(
            newRepository = repository
        )
        censoringViewModel.updateRepositories(
            newRepository = repository,
            newBlockingCensoringRealm = repository
        )

        setContent {
            MyApplicationTheme {
                NavigationGraph(
                    toolbarViewModel,
                    userProfileViewModel,
                    homeViewModel = HomeViewModel(repository = repository),
                    messagesViewModel = messagesViewModel,
                    blockingViewModel = blockingViewModel,
                    censoringViewModel = censoringViewModel,
                    friendRequestViewModel = friendRequestViewModel,
                    compassViewModel = compassViewModel,
                    compassNearbyAPI = compassNearbyAPI
                )
            }
        }
    }

    // region MostlyDeviceConnectionUseCase
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

    private fun verifyPermissions(){
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
    // endregion MostlyDeviceConnectionUseCase

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
 * Displays the entire user profile screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun UserProfileLayout(
    userProfileViewModel: UserProfileViewModel,
    toolbarViewModel: ToolbarViewModel,
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            // This top bar is used because it already has logging out of account implemented
            TaskAppToolbar(viewModel = toolbarViewModel, navController = navController)
        },
        bottomBar = { ProxiPalBottomAppBar(navController) },
        modifier = modifier
    ) { innerPadding ->
        Column {
            UserProfileBody(
                contentPadding = innerPadding,
                userProfileViewModel = userProfileViewModel,
                toolbarViewModel = toolbarViewModel
            )
            HomeScreen(navController = navController, viewModel = homeViewModel, userProfileViewModel = userProfileViewModel)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserProfileLayoutPreview() {
    MyApplicationTheme {
        val repository = MockRepository()
        val userProfiles = (1..30).map { index ->
            MockRepository.getMockUserProfile(index)
        }.toMutableStateList()
        UserProfileLayout(
            userProfileViewModel = UserProfileViewModel(
                repository = repository,
                userProfileListState = userProfiles
            ),
            toolbarViewModel = ToolbarViewModel(repository),
            navController = rememberNavController(),
            homeViewModel = HomeViewModel(repository = repository)
        )
    }
}

/**
 * The middle body content of the user profile screen
 */
@Composable
fun UserProfileBody(
    userProfileViewModel: UserProfileViewModel,
    toolbarViewModel: ToolbarViewModel,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues? = null
) {
    var isCardExpanded by rememberSaveable { mutableStateOf(false) }
    var columnModifier = modifier
        .verticalScroll(rememberScrollState())
        .fillMaxSize()
        .padding(all = dimensionResource(id = R.dimen.user_profile_spacer_height))
    if (contentPadding != null) {
        columnModifier = columnModifier.padding(contentPadding)
    }
    var cardModifier = Modifier
        .fillMaxHeight()
    // Only allow card expanding/shrinking if not editing the user profile
    if (!userProfileViewModel.isEditingUserProfile.value) {
        cardModifier = cardModifier.clickable {
            isCardExpanded = !isCardExpanded
        }
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = columnModifier
    ) {
        Card(
            modifier = cardModifier
        )
        {
            UserProfileLayoutRow(
                rowInformationHeader = R.string.user_profile_first_name_header,
                rowInformation = userProfileViewModel.userProfileFirstName.value,
                remainingCharacterAmount = userProfileViewModel.getRemainingCharacterAmountFirstName(),
                isInformationExpanded = isCardExpanded,
                isEditingUserProfile = userProfileViewModel.isEditingUserProfile.value,
                onTextChange = { userProfileViewModel.setUserProfileFirstName(it) }
            )
            UserProfileLayoutRow(
                rowInformationHeader = R.string.user_profile_last_name_header,
                rowInformation = userProfileViewModel.userProfileLastName.value,
                remainingCharacterAmount = userProfileViewModel.getRemainingCharacterAmountLastName(),
                isInformationExpanded = isCardExpanded,
                isEditingUserProfile = userProfileViewModel.isEditingUserProfile.value,
                onTextChange = { userProfileViewModel.setUserProfileLastName(it) }
            )
            UserProfileLayoutRow(
                rowInformationHeader = R.string.user_profile_biography_header,
                rowInformation = userProfileViewModel.userProfileBiography.value,
                remainingCharacterAmount = userProfileViewModel.getRemainingCharacterAmountBiography(),
                isInformationExpanded = isCardExpanded,
                isEditingUserProfile = userProfileViewModel.isEditingUserProfile.value,
                onTextChange = { userProfileViewModel.setUserProfileBiography(it) }
            )
        }
        Spacer(
            modifier = Modifier
                .height(dimensionResource(id = R.dimen.user_profile_spacer_height))
        )
        UserProfileEditButtons(
            isEditingUserProfile = userProfileViewModel.isEditingUserProfile.value,
            // Will default to automatic card shrinking when the save or discard changes
            // ... button is clicked
            onEditButtonClick = {
                userProfileViewModel.toggleUserProfileEditMode()
                // Automatically show/hide all information when switching to/from edit mode
                isCardExpanded = userProfileViewModel.isEditingUserProfile.value
            },
            onDiscardEditButtonClick = {
                userProfileViewModel.discardUserProfileChanges()
                isCardExpanded = false
            },
            onDeleteAccountConfirmed = {
                userProfileViewModel.deleteAccount()
            },
            toolbarViewModel = toolbarViewModel
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UserProfileBodyPreview() {
    MyApplicationTheme {
        val repository = MockRepository()
        val userProfiles = (1..30).map { index ->
            MockRepository.getMockUserProfile(index)
        }.toMutableStateList()

        UserProfileBody(
            userProfileViewModel = UserProfileViewModel(
                repository = repository,
                userProfileListState = userProfiles,
            ),
            toolbarViewModel = ToolbarViewModel(repository=repository)
        )
    }
}

/**
 * Displays a single row of information in the user profile screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileLayoutRow(
    @StringRes rowInformationHeader: Int,
    rowInformation: String,
    remainingCharacterAmount: Int,
    isInformationExpanded: Boolean,
    isEditingUserProfile: Boolean,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // If the supplied row information is empty, use a temporary placeholder instead
    val nonEmptyRowInformation =
        rowInformation.ifEmpty { stringResource(id = R.string.user_profile_empty_string_replacement) }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .padding(all = dimensionResource(id = R.dimen.user_profile_row_padding))
    ) {
        // Display the row header
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .weight(USER_PROFILE_ROW_HEADER_WEIGHT)
        ) {
            SingleLineText(
                text = stringResource(
                    R.string.user_profile_row_header,
                    stringResource(id = rowInformationHeader)
                )
            )
        }
        // Display the corresponding information for a specific row
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .weight(1 - USER_PROFILE_ROW_HEADER_WEIGHT)
        ) {
            // Read-only text
            if (!isEditingUserProfile) {
                // If the card is not expanded, hide information to shorten it if it's long
                if (isInformationExpanded) {
                    when (rowInformation.isNotEmpty()) {
                        false -> MultiLineText(text = nonEmptyRowInformation, isItalic = true)
                        true -> MultiLineText(text = rowInformation)
                    }
                } else {
                    when (rowInformation.isNotEmpty()) {
                        false -> SingleLineText(text = nonEmptyRowInformation, isItalic = true)
                        true -> SingleLineText(text = rowInformation)
                    }
                }
            }
            // Editable text
            else {
                TextField(
                    // Do not replace any empty input with replacements here
                    value = rowInformation,
                    // Displays how many available characters are left
                    label = {
                        SingleLineText(
                            text = stringResource(
                                id = R.string.user_profile_characters_remaining,
                                remainingCharacterAmount.toString()
                            )
                        )
                    },
                    // This line does not seem to change anything currently (?)
                    placeholder = { stringResource(id = R.string.user_profile_empty_string_replacement) },
                    onValueChange = onTextChange,
                    // Make the keyboard action button hide the keyboard instead of entering a new line
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    // Limit the amount of lines shown when typing in a multi-line text field
                    maxLines = USER_PROFILE_EDIT_MODE_MAXIMUM_LINE_AMOUNT
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserProfileLayoutRowPreview() {
    MyApplicationTheme {
        UserProfileLayoutRow(
            rowInformationHeader = R.string.user_profile_first_name_header,
            rowInformation = stringResource(id = R.string.app_name),
            remainingCharacterAmount = 99,
            isInformationExpanded = false,
            isEditingUserProfile = false,
            onTextChange = {}
        )
    }
}

/**
 * Displays the button to toggle user profile editing
 */
@Composable
fun UserProfileEditButtons(
    isEditingUserProfile: Boolean,
    onEditButtonClick: (() -> Unit),
    onDiscardEditButtonClick: () -> Unit,
    onDeleteAccountConfirmed: () -> Unit,
    toolbarViewModel: ToolbarViewModel,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier
            .fillMaxWidth()
    ) {
        Button(
            onClick = onEditButtonClick
        ) {
            Text(
                // Set the text depending on if the user is currently editing their profile
                text = stringResource(
                    id =
                    when (isEditingUserProfile) {
                        false -> R.string.user_profile_start_editing_message
                        true -> R.string.user_profile_finish_editing_message
                    }
                )
            )
        }
        // Only display the edit canceling button when editing the user profile
        if (isEditingUserProfile){
            Button(
                onClick = onDiscardEditButtonClick
            ){
                Text(
                    text = stringResource(id = R.string.user_profile_cancel_editing_message)
                )
            }
        }
    }
    if (isEditingUserProfile){
        Button(onClick = {showDeleteConfirmationDialog = true}){
            Text(
                text = stringResource(id = R.string.delete_account)
            )
        }
    }
    DeleteConfirmationDialog(
        showDeleteConfirmationDialog = showDeleteConfirmationDialog,
        onDeleteAccountConfirmed =  onDeleteAccountConfirmed,
        onDismissRequest = { showDeleteConfirmationDialog= false },
        toolbarViewModel = toolbarViewModel
    )

//    if (showDeleteConfirmationDialog) {
//        AlertDialog(onDismissRequest = { showDeleteConfirmationDialog = false}) {
//            Surface(
//                modifier=Modifier.fillMaxWidth(),
////                tonalElevation = AlertDialogDefaults.TonalElevation,
//                color = Color.White,
//                shape = RoundedCornerShape(12.dp)
//            ) {
//            Column(modifier=Modifier.padding(16.dp),
//                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement =  Arrangement.spacedBy(24.dp))
//            {
//                Text(text = stringResource(id = R.string.are_you_sure ))
//                Text(text = stringResource(id = R.string.delete_account_confirmation_message))
//                Row(
//                    modifier= Modifier.fillMaxWidth(),
//                    verticalAlignment = Alignment.CenterVertically
//                ){
//                    TextButton(
//                        onClick = {
//                            showDeleteConfirmationDialog = false
//                            onDeleteAccountConfirmed()
//                            toolbarViewModel.logOut()
//                        }, modifier = Modifier.fillMaxWidth().weight(1f)
//                    ) {
//                        Text(text = stringResource(id = R.string.delete_account))
//                    }
//                    TextButton(
//                        onClick = { showDeleteConfirmationDialog = false },
//                        modifier = Modifier.fillMaxWidth().weight(1f)
//                    ) {
//                        Text(text = stringResource(id = R.string.cancel))
//                    }
//                }
//            }
//
//            }
//        }
//    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteConfirmationDialog(
    showDeleteConfirmationDialog: Boolean,
    onDeleteAccountConfirmed: () -> Unit,
    onDismissRequest: () -> Unit,
    toolbarViewModel: ToolbarViewModel
) {
    if (showDeleteConfirmationDialog) {
        AlertDialog(onDismissRequest = onDismissRequest) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                tonalElevation = AlertDialogDefaults.TonalElevation,
                shadowElevation = 20.dp,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(text = stringResource(id = R.string.are_you_sure))
                    Text(text = stringResource(id = R.string.delete_account_confirmation_message))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onDismissRequest,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            Text(text = stringResource(id = R.string.cancel))
                        }
                        TextButton(
                            onClick = {
                                onDismissRequest()
                                onDeleteAccountConfirmed()
                                toolbarViewModel.logOut()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            Text(text = stringResource(id = R.string.confirm))
                        }

                    }
                }
            }
        }
    }
}
