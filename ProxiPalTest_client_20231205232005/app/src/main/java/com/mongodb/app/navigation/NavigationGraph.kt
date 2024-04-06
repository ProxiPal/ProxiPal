package com.mongodb.app.navigation

import ProfileSetupScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mongodb.app.friends.ReportDropDownMenu
import com.mongodb.app.friends.ReportViewModel
import com.mongodb.app.home.AdvancedScreenSettings
import com.mongodb.app.home.FilterScreen
import com.mongodb.app.home.HomeScreen
import com.mongodb.app.home.HomeViewModel
import com.mongodb.app.home.ScreenSettings
import com.mongodb.app.presentation.tasks.ToolbarViewModel
import com.mongodb.app.ui.tasks.ConnectWithOthersScreen
import com.mongodb.app.location.LocationPermissionScreen
import com.mongodb.app.presentation.userprofiles.UserProfileViewModel
import com.mongodb.app.tutorial.OnboardingScreen
import com.mongodb.app.ui.userprofiles.IndustryScreen
import com.mongodb.app.ui.userprofiles.InterestScreen

import com.mongodb.app.ui.userprofiles.UserProfileLayout


//TODO add more parameters as needed

// Contribution: Marco Pacini
// Vichet Chim - added first time login
/**
 * Navigation graph for the different screens in Proxipal
 */
@Composable
fun NavigationGraph(toolbarViewModel: ToolbarViewModel, userProfileViewModel: UserProfileViewModel, homeViewModel: HomeViewModel, reportViewModel: ReportViewModel) {
    var state by remember{ mutableStateOf(false)}
    val navController = rememberNavController()
    var startDest = Routes.UserProfileScreen.route
    if (userProfileViewModel.userProfileListState.isEmpty()){
        startDest = Routes.OnboardingScreen.route
    }
    NavHost(navController = navController, startDestination = startDest) {
        composable(Routes.UserProfileScreen.route) {
            //checks if its the user's first time login in, added by Vichet Chim
            if (userProfileViewModel.userProfileListState.isEmpty()){
                state = true
            }
            if (state){ // displays user setup screens
                ProfileSetupScaffold(
                    userProfileViewModel = userProfileViewModel,
                    toolbarViewModel = toolbarViewModel,
                    navController = navController,
                    onPreviousClicked = { /*TODO*/ },
                    onNextClicked = { navController.navigate(Routes.UserInterestsScreen.route) })
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
            InterestScreen(userProfileViewModel = userProfileViewModel,
                onPreviousClicked = { navController.popBackStack() },
                onNextClicked = {navController.navigate(Routes.UserIndustriesScreen.route)})
        }
        composable(Routes.UserIndustriesScreen.route){
            IndustryScreen(userProfileViewModel = userProfileViewModel,
                onPreviousClicked = { navController.popBackStack() },
                onNextClicked = {state = false;navController.popBackStack();navController.popBackStack()}) //end of user setup screen
        }
        composable(Routes.ConnectWithOthersScreen.route) {
            ConnectWithOthersScreen(
                toolbarViewModel = toolbarViewModel,
                navController = navController,
                userProfileViewModel = userProfileViewModel,
                reportViewModel = reportViewModel
            )
        }
        composable(Routes.LocationPermissionsScreen.route) {
            LocationPermissionScreen(navController)
        }
        //march7
        composable(Routes.HomeScreen.route) {
            HomeScreen(navController = navController, viewModel = homeViewModel, userProfileViewModel = userProfileViewModel)
        }
        composable(Routes.ScreenSettings.route){
            ScreenSettings(navController = navController)
        }
        composable(Routes.FilterScreen.route) {
            FilterScreen(navController = navController, viewModel = userProfileViewModel) //march17
        }
        composable(Routes.OnboardingScreen.route){
            OnboardingScreen(
                userProfileViewModel = userProfileViewModel,
                toolbarViewModel = toolbarViewModel,
                navController = navController,
                homeViewModel = homeViewModel,
                reportViewModel= reportViewModel
                )
        }
        composable(Routes.AdvancedScreenSettings.route){
            AdvancedScreenSettings(navController)
        }

        composable(Routes.ReportScreen.route){
            ReportDropDownMenu(reportViewModel = reportViewModel)
        }

    }
}