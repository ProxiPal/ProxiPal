package com.mongodb.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mongodb.app.presentation.tasks.ToolbarViewModel
import com.mongodb.app.ui.tasks.ConnectWithOthersScreen
import com.mongodb.app.ui.tasks.LocationPermissionScreen

//TODO add more parameters as needed

@Composable
fun NavigationGraph(toolbarViewModel: ToolbarViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.ConnectWithOthersScreen.route) {
        composable(Routes.ConnectWithOthersScreen.route) {
            ConnectWithOthersScreen(toolbarViewModel = toolbarViewModel, navController = navController)
        }
        composable(Routes.LocationPermissionsScreen.route) {
            LocationPermissionScreen(navController)
        }
    }
}