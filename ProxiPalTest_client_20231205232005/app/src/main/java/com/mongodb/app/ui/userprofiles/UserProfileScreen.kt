@file:OptIn(ExperimentalMaterial3Api::class)

package com.mongodb.app.ui.userprofiles

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.input.key.Key.Companion.Window
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.mongodb.app.ComposeLoginActivity
import com.mongodb.app.R
import com.mongodb.app.TAG
import com.mongodb.app.data.MockRepository
import com.mongodb.app.data.RealmSyncRepository
import com.mongodb.app.data.compassscreen.CompassPermissionHandler
import com.mongodb.app.data.userprofiles.USER_PROFILE_EDIT_MODE_MAXIMUM_LINE_AMOUNT
import com.mongodb.app.data.userprofiles.USER_PROFILE_ROW_HEADER_WEIGHT
import com.mongodb.app.friends.FriendRequestViewModel
import com.mongodb.app.home.HomeScreen
import com.mongodb.app.home.HomeViewModel
import com.mongodb.app.navigation.NavigationGraph
import com.mongodb.app.presentation.blocking_censoring.BlockingViewModel
import com.mongodb.app.presentation.blocking_censoring.CensoringViewModel
import com.mongodb.app.presentation.compassscreen.CompassViewModel
import com.mongodb.app.presentation.messages.MessagesViewModel
import com.mongodb.app.presentation.tasks.ToolbarEvent
import com.mongodb.app.presentation.tasks.ToolbarViewModel
import com.mongodb.app.presentation.userprofiles.AddUserProfileEvent
import com.mongodb.app.presentation.userprofiles.UserProfileViewModel
import com.mongodb.app.ui.components.MultiLineText
import com.mongodb.app.ui.components.ProxiPalBottomAppBar
import com.mongodb.app.ui.components.SingleLineText
import com.mongodb.app.ui.events.EventsViewModel
import com.mongodb.app.ui.report.ReportViewModel
import com.mongodb.app.ui.tasks.TaskAppToolbar
import com.mongodb.app.ui.theme.MyApplicationTheme
import com.mongodb.app.userratingsystem.UserRatingsDisplayScreen
import kotlinx.coroutines.launch


/*
Contributions:
- Kevin Kubota (all user profile UI, except for navigation between screens)
 */


class UserProfileScreen : ComponentActivity() {
    // region Variables
    private val repository = RealmSyncRepository { _, _ -> }

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

    private lateinit var compassPermissionHandler: CompassPermissionHandler
    // endregion Variables


    // region Functions
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

        // Need to update repository when a configuration change occurs
        // ... otherwise app will crash when trying to access Realm after it has closed
        compassViewModel.updateRepository(
            newRepository = repository
        )
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

        compassPermissionHandler = CompassPermissionHandler(
            repository = repository,
            activity = this,
            compassViewModel = compassViewModel
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
                    compassPermissionHandler = compassPermissionHandler,
                    eventsViewModel = EventsViewModel(repository=repository),
                    reportViewModel = ReportViewModel(repository=repository)
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Repository must be closed to free resources
        repository.close()
        compassPermissionHandler.endSetup()
    }
    // endregion Functions
}


// region Functions
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
        Column (
            Modifier
                .verticalScroll(rememberScrollState())
                .height((calculateScreenHeight()+100).dp)
        ){
            UserProfileBody(
                contentPadding = innerPadding,
                userProfileViewModel = userProfileViewModel,
                toolbarViewModel = toolbarViewModel
            )
            UserRatingsDisplayScreen(userProfileViewModel = userProfileViewModel)
            HomeScreen(navController = navController, viewModel = homeViewModel, userProfileViewModel = userProfileViewModel)
        }
    }
}

/**
 * Added by Marco to calculate screen height in order to specify a height for the column layout
 * so that vertical scroll is functional.
 */
@Composable
fun calculateScreenHeight(): Int {
    val screenHeight = LocalConfiguration.current.screenHeightDp
    return screenHeight
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
                onTextChange = { userProfileViewModel.setUserProfileFirstName(it) },
                testTag = "userProfileInputRowFirstName"
            )
            UserProfileLayoutRow(
                rowInformationHeader = R.string.user_profile_last_name_header,
                rowInformation = userProfileViewModel.userProfileLastName.value,
                remainingCharacterAmount = userProfileViewModel.getRemainingCharacterAmountLastName(),
                isInformationExpanded = isCardExpanded,
                isEditingUserProfile = userProfileViewModel.isEditingUserProfile.value,
                onTextChange = { userProfileViewModel.setUserProfileLastName(it) },
                testTag = "userProfileInputRowLastName"
            )
            UserProfileLayoutRow(
                rowInformationHeader = R.string.user_profile_biography_header,
                rowInformation = userProfileViewModel.userProfileBiography.value,
                remainingCharacterAmount = userProfileViewModel.getRemainingCharacterAmountBiography(),
                isInformationExpanded = isCardExpanded,
                isEditingUserProfile = userProfileViewModel.isEditingUserProfile.value,
                onTextChange = { userProfileViewModel.setUserProfileBiography(it) },
                testTag = "userProfileInputRowBiography"
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
    testTag: String,
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
                    maxLines = USER_PROFILE_EDIT_MODE_MAXIMUM_LINE_AMOUNT,
                    // Allows this to be uniquely identified in tests
                    modifier = Modifier
                        .testTag(testTag)
                )
            }
        }
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
            onClick = onEditButtonClick,
            modifier = Modifier
                .testTag("userProfileEditButton")
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
                onClick = onDiscardEditButtonClick,
                modifier = Modifier
                    .testTag("userProfileDiscardButton")
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
// endregion Functions


// region Previews
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
            onTextChange = {},
            testTag = ""
        )
    }
}
// endregion Previews
