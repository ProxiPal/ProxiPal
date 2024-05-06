package com.mongodb.app

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import com.mongodb.app.data.RealmSyncRepository
import com.mongodb.app.home.HomeViewModel
import com.mongodb.app.presentation.tasks.ToolbarViewModel
import com.mongodb.app.presentation.userprofiles.UserProfileViewModel
import com.mongodb.app.ui.theme.MyApplicationTheme
import com.mongodb.app.ui.userprofiles.UserProfileLayout
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test

class StringCensoringUITests {

    @get: Rule
    val composeTestRule = createComposeRule()

    private val repository = RealmSyncRepository{ _, _ -> }
    private val owner = ComponentActivity()
    private val homeViewModel = HomeViewModel(
        repository
    )
    private val toolbarViewModel = ToolbarViewModel(
        repository
    )
    private val userProfileViewModel = UserProfileViewModel(
        repository
    )


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun censorStrings(){
        composeTestRule.setContent {
            MyApplicationTheme {
//                val navController = rememberNavController()
//                UserProfileLayout(
//                    userProfileViewModel = userProfileViewModel,
//                    toolbarViewModel = toolbarViewModel,
//                    navController = navController,
//                    homeViewModel = homeViewModel
//                )
                ComposeLoginActivity()
            }
        }
        // Should be the same as the icon button to navigate to settings screen
        // ... in TaskAppToolbar.kt
        composeTestRule.onNodeWithContentDescription("settings screen icon button")
            .performClick()
            composeTestRule.onNodeWithContentDescription("dummy")
            .assertExists("Settings screen icon button not found")
    }
}