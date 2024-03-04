package com.mongodb.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mongodb.app.home.HomeScreen
import com.mongodb.app.home.HomeViewModel
import com.mongodb.app.home.ScreenSettings
import com.mongodb.app.presentation.tasks.ToolbarViewModel
import com.mongodb.app.ui.tasks.ConnectWithOthersScreen
import com.mongodb.app.location.LocationPermissionScreen
import com.mongodb.app.presentation.userprofiles.UserProfileViewModel
import com.mongodb.app.ui.userprofiles.InterestScreen
import com.mongodb.app.ui.userprofiles.ProfileSetup
import com.mongodb.app.ui.userprofiles.ProfileSetupScaffold
import com.mongodb.app.ui.userprofiles.UserProfileLayout
import com.mongodb.app.ui.userprofiles.test

//TODO add more parameters as needed

// Contribution: Marco Pacini
/**
 * Navigation graph for the different screens in Proxipal
 */
@Composable
fun NavigationGraph(toolbarViewModel: ToolbarViewModel, userProfileViewModel: UserProfileViewModel, homeViewModel: HomeViewModel) {
    var state by remember{ mutableStateOf(false)}
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.UserProfileScreen.route) {
        composable(Routes.UserProfileScreen.route) {
            if (userProfileViewModel.userProfileListState.isEmpty()){
                state = true
            }
            if (state){
                ProfileSetupScaffold(userProfileViewModel = userProfileViewModel, onPreviousClicked = { /*TODO*/ }, onNextClicked = {userProfileViewModel.addUserProfile();navController.navigate(Routes.UserInterestsScreen.route)})


            }
            else {
                UserProfileLayout(
                    userProfileViewModel = userProfileViewModel,
                    toolbarViewModel = toolbarViewModel,
                    homeViewModel = homeViewModel,
                    navController = navController
                )
            }
        }
        composable(Routes.UserInterestsScreen.route){
            InterestScreen(userProfileViewModel = userProfileViewModel ,onPreviousClicked = { navController.popBackStack() }, onNextClicked = {state = false;Routes.UserProfileScreen.route})
        }
        composable(Routes.ConnectWithOthersScreen.route) {
            ConnectWithOthersScreen(
                toolbarViewModel = toolbarViewModel,
                navController = navController,
                userProfileViewModel = userProfileViewModel
            )
        }
        composable(Routes.LocationPermissionsScreen.route) {
            LocationPermissionScreen(navController)
        }
        composable(Routes.HomeScreen.route) {
            HomeScreen(navController = navController, viewModel = HomeViewModel())
        }
        
        composable(Routes.ScreenSettings.route){
            ScreenSettings(navController = navController)
        }
    }
}