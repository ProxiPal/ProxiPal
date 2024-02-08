@file:OptIn(ExperimentalMaterial3Api::class)

package com.mongodb.app.ui.userprofiles

import android.annotation.SuppressLint
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mongodb.app.R
import com.mongodb.app.data.USER_PROFILE_EDIT_MODE_MAXIMUM_LINE_AMOUNT
import com.mongodb.app.data.USER_PROFILE_ROW_HEADER_WEIGHT
import com.mongodb.app.data.UserProfileInformationType
import com.mongodb.app.ui.components.MultiLineText
import com.mongodb.app.ui.components.SingleLineText
import com.mongodb.app.ui.theme.MyApplicationTheme

/*
===== Functions =====
 */
/**
Displays the entire user profile screen
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun UserProfileLayout(
    modifier: Modifier = Modifier,
    userProfileViewModel: UserProfileViewModel = viewModel()
) {
    Scaffold(
        topBar = {
            UserProfileTopBar()
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
        UserProfileLayout()
    }
}

/**
 * The top bar portion of the user profile screen (currently just a title)
 */
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
    modifier: Modifier = Modifier,
    userProfileViewModel: UserProfileViewModel = viewModel(),
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
                rowInformation = userProfileViewModel.userProfileFirstName,
                remainingCharacterAmount = userProfileViewModel.getRemainingCharacterAmount(
                    UserProfileInformationType.FirstName
                ),
                isInformationExpanded = isCardExpanded,
                isEditingUserProfile = userProfileViewModel.isEditingUserProfile,
                onTextChange = { userProfileViewModel.updateUserProfileFirstName(it) }
            )
            UserProfileLayoutRow(
                rowInformationHeader = R.string.user_profile_last_name_header,
                rowInformation = userProfileViewModel.userProfileLastName,
                remainingCharacterAmount = userProfileViewModel.getRemainingCharacterAmount(
                    UserProfileInformationType.LastName
                ),
                isInformationExpanded = isCardExpanded,
                isEditingUserProfile = userProfileViewModel.isEditingUserProfile,
                onTextChange = { userProfileViewModel.updateUserProfileLastName(it) }
            )
            UserProfileLayoutRow(
                rowInformationHeader = R.string.user_profile_biography_header,
                rowInformation = userProfileViewModel.userProfileBiography,
                remainingCharacterAmount = userProfileViewModel.getRemainingCharacterAmount(
                    UserProfileInformationType.Biography
                ),
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
    }
}

@Preview(showBackground = true)
@Composable
fun UserProfileBodyPreview() {
    MyApplicationTheme {
        UserProfileBody()
    }
}

/**
Displays a single row of information in the user profile screen
 */
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
                if (isInformationExpanded){
                    when (rowInformation.isNotEmpty()) {
                        false -> MultiLineText(text = nonEmptyRowInformation, isItalic = true)
                        true -> MultiLineText(text = rowInformation)
                    }
                }
                else{
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