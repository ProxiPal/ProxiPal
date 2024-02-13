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
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.mongodb.app.ComposeLoginActivity
import com.mongodb.app.R
import com.mongodb.app.TAG
import com.mongodb.app.data.MockRepository
import com.mongodb.app.data.RealmSyncRepository
import com.mongodb.app.data.USER_PROFILE_EDIT_MODE_MAXIMUM_LINE_AMOUNT
import com.mongodb.app.data.USER_PROFILE_ROW_HEADER_WEIGHT
import com.mongodb.app.presentation.tasks.ToolbarEvent
import com.mongodb.app.presentation.tasks.ToolbarViewModel
import com.mongodb.app.presentation.userprofiles.AddUserProfileEvent
import com.mongodb.app.presentation.userprofiles.UserProfileViewModel
import com.mongodb.app.ui.components.MultiLineText
import com.mongodb.app.ui.components.SingleLineText
import com.mongodb.app.ui.tasks.TaskAppToolbar
import com.mongodb.app.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

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
                    this@UserProfileScreen,
                    getString(R.string.permissions_error),
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


    /*
    ===== Functions =====
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i(TAG(), "UPScreen: On create")

        lifecycleScope.launch {
            userProfileViewModel.event
                .collect {
                    Log.i(TAG(), "Tried to modify or remove a task that doesn't belong to the current user.")
                    Toast.makeText(
                        this@UserProfileScreen,
                        getString(R.string.permissions_warning),
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

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
                            startActivity(Intent(this@UserProfileScreen, ComposeLoginActivity::class.java))
                            finish()
                        }
                        is ToolbarEvent.Info ->
                            Log.e(TAG(), toolbarEvent.message)
                        is ToolbarEvent.Error ->
                            Log.e(TAG(), "${toolbarEvent.message}: ${toolbarEvent.throwable.message}")
                    }
                }
        }

        setContent {
            MyApplicationTheme {
                UserProfileLayout(
                    userProfileViewModel = userProfileViewModel,
                    toolbarViewModel = toolbarViewModel
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        repository.close()
    }
}


/*
===== Functions =====
 */

/**
Displays the entire user profile screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun UserProfileLayout(
    userProfileViewModel: UserProfileViewModel,
    toolbarViewModel: ToolbarViewModel,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
//            UserProfileTopBar()
            // This topbar is used because it already has log out functionality implemented
                 TaskAppToolbar(viewModel = toolbarViewModel)
        },
        modifier = modifier
    ) { innerPadding ->
        UserProfileBody(
            contentPadding = innerPadding,
            userProfileViewModel = userProfileViewModel
        )
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
            toolbarViewModel = ToolbarViewModel(repository)
        )
    }
}

/**
 * The top bar portion of the user profile screen (currently just a title)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileTopBar(modifier: Modifier = Modifier) {
    CenterAlignedTopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(id = R.string.user_profile_header),
                    style = MaterialTheme.typography.displayLarge
                )
            }
        },
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun UserProfileTopBarPreview() {
    MyApplicationTheme {
        UserProfileTopBar()
    }
}

/**
 * The middle body content of the user profile screen
 */
@Composable
fun UserProfileBody(
    userProfileViewModel: UserProfileViewModel,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues? = null
) {
    var isCardExpanded by rememberSaveable { mutableStateOf(false) }
    var columnModifier = modifier
        .verticalScroll(rememberScrollState())
        .fillMaxSize()
    if (contentPadding != null) {
        columnModifier = columnModifier.padding(contentPadding)
    }
    var cardModifier = Modifier
        .fillMaxHeight()
    // Only allow card expanding/shrinking if not editing the user profile
    if (!userProfileViewModel.isEditingUserProfile) {
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
                isEditingUserProfile = userProfileViewModel.isEditingUserProfile,
                onTextChange = { userProfileViewModel.updateUserProfileFirstName(it) }
            )
            UserProfileLayoutRow(
                rowInformationHeader = R.string.user_profile_last_name_header,
                rowInformation = userProfileViewModel.userProfileLastName.value,
                remainingCharacterAmount = userProfileViewModel.getRemainingCharacterAmountLastName(),
                isInformationExpanded = isCardExpanded,
                isEditingUserProfile = userProfileViewModel.isEditingUserProfile,
                onTextChange = { userProfileViewModel.updateUserProfileLastName(it) }
            )
            UserProfileLayoutRow(
                rowInformationHeader = R.string.user_profile_biography_header,
                rowInformation = userProfileViewModel.userProfileBiography.value,
                remainingCharacterAmount = userProfileViewModel.getRemainingCharacterAmountBiography(),
                isInformationExpanded = isCardExpanded,
                isEditingUserProfile = userProfileViewModel.isEditingUserProfile,
                onTextChange = { userProfileViewModel.updateUserProfileBiography(it) }
            )
        }
        UserProfileEditButton(
            userProfileViewModel.isEditingUserProfile,
            onClick = {
                userProfileViewModel.toggleUserProfileEditMode()
                // Automatically show/hide all information when switching to/from edit mode
                isCardExpanded = userProfileViewModel.isEditingUserProfile
            }
        )
        // TODO Temporary use only (until account registration and database saving is established)
        TemporaryUserProfileOwnerIdField(
            userProfileViewModel = userProfileViewModel
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
                userProfileListState = userProfiles
            )
        )
    }
}

/**
Displays a single row of information in the user profile screen
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
        // Display the corresponding user's information for a specific row
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .weight(1 - USER_PROFILE_ROW_HEADER_WEIGHT)
        ) {
            // Read-only text
            if (!isEditingUserProfile) {
                // Hide information if the card is not expanded to shorten it if long
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
            // Text is editable
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
                    // This line does not seem to change anything currently
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
            rowInformation = "John",
            remainingCharacterAmount = 99,
            isInformationExpanded = false,
            isEditingUserProfile = false,
            onTextChange = {}
        )
    }
}

/**
Displays the button to toggle user profile editing
 */
@Composable
fun UserProfileEditButton(
    isEditingUserProfile: Boolean,
    onClick: (() -> Unit),
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
        ) {
            Text(
                // Set the text depending on if the user is currently editing their profile
                text = stringResource(
                    id =
                    when (isEditingUserProfile) {
                        false -> R.string.user_profile_start_editing_message
                        true -> R.string.user_profile_finish_editing_message
                    }
                ),
                modifier = Modifier
            )
        }
    }
}

@Composable
fun TemporaryUserProfileOwnerIdField(
    userProfileViewModel: UserProfileViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            TextField(
                value = userProfileViewModel.temporaryOwnerId,
                onValueChange = { userProfileViewModel.updateTemporaryOwnerId(it) },
                // Make the keyboard action button hide the keyboard instead of entering a new line
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                // Only allow updating the owner ID when the user is not updating their profile
                readOnly = userProfileViewModel.isEditingUserProfile,
                modifier = Modifier
            )
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Button(
                onClick = { userProfileViewModel.addUserProfile() },
                modifier = Modifier
            ) {
                Text(
                    text = "Add to database",
                    modifier = Modifier
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TemporaryUserProfileOwnerIdFieldPreview() {
    MyApplicationTheme {
        val repository = MockRepository()
        val userProfiles = (1..30).map { index ->
            MockRepository.getMockUserProfile(index)
        }.toMutableStateList()
        TemporaryUserProfileOwnerIdField(
            userProfileViewModel = UserProfileViewModel(
                repository = repository,
                userProfileListState = userProfiles
            )
        )
    }
}