package com.mongodb.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mongodb.app.presentation.tasks.ToolbarViewModel
import com.mongodb.app.ui.tasks.ConnectWithOthersScreen
import com.mongodb.app.location.LocationPermissionScreen
import com.mongodb.app.presentation.userprofiles.UserProfileViewModel
import com.mongodb.app.ui.userprofiles.UserProfileLayout

//TODO add more parameters as needed

@Composable
fun NavigationGraph(toolbarViewModel: ToolbarViewModel, userProfileViewModel: UserProfileViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.UserProfileScreen.route) {
        composable(Routes.UserProfileScreen.route) {
            UserProfileLayout(
                userProfileViewModel = userProfileViewModel,
                toolbarViewModel = toolbarViewModel,
                navController = navController
            )
        }
        composable(Routes.ConnectWithOthersScreen.route) {
            ConnectWithOthersScreen(toolbarViewModel = toolbarViewModel, navController = navController)
        }
        composable(Routes.LocationPermissionsScreen.route) {
            LocationPermissionScreen(navController)
        }
    }
}