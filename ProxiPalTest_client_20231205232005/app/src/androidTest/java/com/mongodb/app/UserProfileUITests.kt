package com.mongodb.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.rememberNavController
import com.mongodb.app.data.MockRepository
import com.mongodb.app.data.SyncRepository
import com.mongodb.app.home.HomeViewModel
import com.mongodb.app.presentation.tasks.ToolbarViewModel
import com.mongodb.app.presentation.userprofiles.UserProfileViewModel
import com.mongodb.app.ui.theme.MyApplicationTheme
import com.mongodb.app.ui.userprofiles.UserProfileLayout
import org.junit.Rule
import org.junit.Test

class UserProfileUITests {
    @get: Rule
    val composeTestRule = createComposeRule()

    private val repository: SyncRepository = MockRepository()
    private val homeViewModel = HomeViewModel(
        repository
    )
    private val toolbarViewModel = ToolbarViewModel(
        repository
    )
    private val userProfileViewModel = UserProfileViewModel(
        repository
    )

    @Test
    fun saveProfileEdits(){
        composeTestRule.setContent {
            MyApplicationTheme {
                UserProfileLayout(
                    userProfileViewModel = userProfileViewModel,
                    toolbarViewModel = toolbarViewModel,
                    navController = rememberNavController(),
                    homeViewModel = homeViewModel
                )
            }
        }
        composeTestRule.onNodeWithTag("userProfileEditButton")
            .assertExists("Edit/Save button was not found")

        // Start editing the profile
        composeTestRule.onNodeWithTag("userProfileEditButton")
            .performClick()
        composeTestRule.onNodeWithTag("userProfileEditButton")
            .assertExists("Edit/Save button was not found")
        composeTestRule.onNodeWithTag("userProfileDiscardButton")
            .assertExists("Discard edits button was not found")
        composeTestRule.onNodeWithTag("userProfileInputRowFirstName")
            .assertExists("First name input row was not found")
        composeTestRule.onNodeWithTag("userProfileInputRowLastName")
            .assertExists("Last name input row was not found")
        composeTestRule.onNodeWithTag("userProfileInputRowBiography")
            .assertExists("Biography input row was not found")

        // Input test data
        composeTestRule.onNodeWithTag("userProfileInputRowFirstName")
            .performTextInput("Sam")
        composeTestRule.onNodeWithTag("userProfileInputRowLastName")
            .performTextInput("Sung")
        composeTestRule.onNodeWithTag("userProfileInputRowBiography")
            .performTextInput("This is a UI test")
        composeTestRule.onNodeWithTag("userProfileEditButton")
            .performClick()

        // Inputted information should be in the UI at this point
        composeTestRule.onNodeWithTag("userProfileEditButton")
            .assertExists("Edit/Save button was not found")
        composeTestRule.onNodeWithTag("userProfileEditButton")
            .performClick()
        composeTestRule.onNodeWithTag("userProfileInputRowFirstName")
            .assertExists("First name input row was not found")
        composeTestRule.onNodeWithTag("userProfileInputRowLastName")
            .assertExists("Last name input row was not found")
        composeTestRule.onNodeWithTag("userProfileInputRowBiography")
            .assertExists("Biography input row was not found")
        composeTestRule.onNodeWithText("Sam")
            .assertExists("Input row with test first name was not found")
        composeTestRule.onNodeWithText("Sung")
            .assertExists("Input row with test last name was not found")
        composeTestRule.onNodeWithText("This is a UI test")
            .assertExists("Input row with test biography was not found")

        composeTestRule.onNodeWithText("Sam")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Sung")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("This is a UI test")
            .assertIsDisplayed()
    }

    @Test
    fun discardProfileEdits(){
        composeTestRule.setContent {
            MyApplicationTheme {
                UserProfileLayout(
                    userProfileViewModel = userProfileViewModel,
                    toolbarViewModel = toolbarViewModel,
                    navController = rememberNavController(),
                    homeViewModel = homeViewModel
                )
            }
        }
        composeTestRule.onNodeWithTag("userProfileEditButton")
            .assertExists("Edit/Save button was not found")

        // Start editing the profile
        composeTestRule.onNodeWithTag("userProfileEditButton")
            .performClick()
        composeTestRule.onNodeWithTag("userProfileEditButton")
            .assertExists("Edit/Save button was not found")
        composeTestRule.onNodeWithTag("userProfileDiscardButton")
            .assertExists("Discard edits button was not found")
        composeTestRule.onNodeWithTag("userProfileInputRowFirstName")
            .assertExists("First name input row was not found")
        composeTestRule.onNodeWithTag("userProfileInputRowLastName")
            .assertExists("Last name input row was not found")
        composeTestRule.onNodeWithTag("userProfileInputRowBiography")
            .assertExists("Biography input row was not found")

        // Input test data
        composeTestRule.onNodeWithTag("userProfileInputRowFirstName")
            .performTextInput("Sam")
        composeTestRule.onNodeWithTag("userProfileInputRowLastName")
            .performTextInput("Sung")
        composeTestRule.onNodeWithTag("userProfileInputRowBiography")
            .performTextInput("This is a UI test")
        composeTestRule.onNodeWithTag("userProfileDiscardButton")
            .performClick()

        // Inputted information should not be in the UI at this point
        composeTestRule.onNodeWithTag("userProfileEditButton")
            .assertExists("Edit/Save button was not found")
        composeTestRule.onNodeWithTag("userProfileEditButton")
            .performClick()
        composeTestRule.onNodeWithTag("userProfileInputRowFirstName")
            .assertExists("First name input row was not found")
        composeTestRule.onNodeWithTag("userProfileInputRowLastName")
            .assertExists("Last name input row was not found")
        composeTestRule.onNodeWithTag("userProfileInputRowBiography")
            .assertExists("Biography input row was not found")

        composeTestRule.onNodeWithText("Sam")
            .assertIsNotDisplayed()
        composeTestRule.onNodeWithText("Sung")
            .assertIsNotDisplayed()
        composeTestRule.onNodeWithText("This is a UI test")
            .assertIsNotDisplayed()
    }
}