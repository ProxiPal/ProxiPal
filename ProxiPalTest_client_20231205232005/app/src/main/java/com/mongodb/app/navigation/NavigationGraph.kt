package com.mongodb.app.navigation

import ProfileSetupScaffold
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mongodb.app.data.RealmSyncRepository
import com.mongodb.app.data.SyncRepository
import com.mongodb.app.data.compassscreen.COMPASS_PERMISSION_REQUEST_CODE
import com.mongodb.app.data.compassscreen.CompassPermissionHandler
import com.mongodb.app.data.compassscreen.DANGEROUS_COMPASS_SCREEN_PERMISSIONS
import com.mongodb.app.data.messages.MessagesData
import com.mongodb.app.friends.FriendRequestViewModel
import com.mongodb.app.friends.FriendsListLayout
import com.mongodb.app.friends.Friendslist
import com.mongodb.app.home.AdvancedScreenSettings
import com.mongodb.app.home.FilterScreen
import com.mongodb.app.home.HomeScreen
import com.mongodb.app.home.HomeViewModel
import com.mongodb.app.home.ScreenSettings
import com.mongodb.app.location.LocationPermissionScreen
import com.mongodb.app.presentation.blocking_censoring.BlockingViewModel
import com.mongodb.app.presentation.blocking_censoring.CensoringViewModel
import com.mongodb.app.presentation.compassscreen.CompassViewModel
import com.mongodb.app.presentation.messages.MessagesViewModel
import com.mongodb.app.presentation.tasks.ToolbarViewModel
import com.mongodb.app.presentation.userprofiles.UserProfileViewModel
import com.mongodb.app.screens.FriendRequestScreen
import com.mongodb.app.screens.FriendRequestScreenLayout
import com.mongodb.app.tutorial.OnboardingScreen
import com.mongodb.app.ui.compassscreen.CompassScreenLayout
import com.mongodb.app.ui.messages.MessagesScreenLayout
import com.mongodb.app.ui.tasks.ConnectWithOthersScreen
import com.mongodb.app.ui.userprofiles.IndustryScreen
import com.mongodb.app.ui.userprofiles.InterestScreen
import com.mongodb.app.ui.userprofiles.UserProfileLayout
import io.realm.kotlin.mongodb.exceptions.SyncException
import io.realm.kotlin.mongodb.sync.SyncSession
import java.util.SortedSet


//TODO add more parameters as needed

// Contribution: Marco Pacini
// Vichet Chim - added first time login
// - Kevin Kubota (updated navigation to user profile screen and set up navigation to messages screen)


/**
 * Navigation graph for the different screens in Proxipal
 */
@Composable
fun NavigationGraph(
    toolbarViewModel: ToolbarViewModel,
    userProfileViewModel: UserProfileViewModel,
    homeViewModel: HomeViewModel,
    messagesViewModel: MessagesViewModel,
    blockingViewModel: BlockingViewModel,
    censoringViewModel: CensoringViewModel,
    friendRequestViewModel: FriendRequestViewModel,
    compassViewModel: CompassViewModel,
    compassPermissionHandler: CompassPermissionHandler
) {

    var currentUserId by remember { mutableStateOf("") }

    var state by remember { mutableStateOf(false) }
    val navController = rememberNavController()
    //APRIL
    val syncErrorHandling: (SyncSession, SyncException) -> Unit = { session, error ->
        println("Sync error: ${error.message}")
    }
    //APRIL
    val repository: SyncRepository = RealmSyncRepository(syncErrorHandling)

    var startDest = Routes.UserProfileScreen.route
    if (userProfileViewModel.userProfileListState.isEmpty()) {
        startDest = Routes.OnboardingScreen.route
    }
    NavHost(navController = navController, startDestination = startDest) {
        composable(Routes.UserProfileScreen.route) {
            //checks if its the user's first time login in, added by Vichet Chim
            if (userProfileViewModel.userProfileListState.isEmpty()) {
                state = true
            }
            if (state) { // displays user setup screens
                ProfileSetupScaffold(
                    userProfileViewModel = userProfileViewModel,
                    toolbarViewModel = toolbarViewModel,
                    navController = navController,
                    onPreviousClicked = { /*TODO*/ },
                    onNextClicked = { navController.navigate(Routes.UserInterestsScreen.route) })
            } else {
                UserProfileLayout(
                    userProfileViewModel = userProfileViewModel,
                    toolbarViewModel = toolbarViewModel,
                    homeViewModel = homeViewModel,
                    navController = navController
                )
                currentUserId = userProfileViewModel.repository.getCurrentUserId()
            }
        }
        composable(Routes.UserInterestsScreen.route) {
            InterestScreen(userProfileViewModel = userProfileViewModel,
                onPreviousClicked = { navController.popBackStack() },
                onNextClicked = { navController.navigate(Routes.UserIndustriesScreen.route) })
        }
        composable(Routes.UserIndustriesScreen.route) {
            IndustryScreen(userProfileViewModel = userProfileViewModel,
                onPreviousClicked = { navController.popBackStack() },
                onNextClicked = {
                    state = false;navController.popBackStack();navController.popBackStack()
                }) //end of user setup screen
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
        //march7
        composable(Routes.HomeScreen.route) {
            HomeScreen(
                navController = navController,
                viewModel = homeViewModel,
                userProfileViewModel = userProfileViewModel
            )
        }
        composable(Routes.ScreenSettings.route) {
            ScreenSettings(navController = navController)
        }
        composable(Routes.FilterScreen.route) {
            FilterScreen(
                navController = navController,
                viewModel = userProfileViewModel
            ) //march17
        }
        composable(Routes.OnboardingScreen.route) {
            OnboardingScreen(
                userProfileViewModel = userProfileViewModel,
                toolbarViewModel = toolbarViewModel,
                navController = navController,
                homeViewModel = homeViewModel
            )
        }
        composable(Routes.AdvancedScreenSettings.route) {
            AdvancedScreenSettings(navController)
        }
        composable(Routes.MessagesScreen.route) {
            val usersInvolved: SortedSet<String> = sortedSetOf()
            usersInvolved.add(currentUserId)
            usersInvolved.add(MessagesData.userIdInFocus.value)

            MessagesScreenLayout(
                navController = navController,
                messagesViewModel = messagesViewModel,
                conversationUsersInvolved = usersInvolved,
                blockingViewModel = blockingViewModel,
                censoringViewModel = censoringViewModel
            )
        }
        composable(Routes.FriendListScreen.route) {
            FriendsListLayout(
                navController = navController,
                viewModel = userProfileViewModel,
                friendRequestViewModel = friendRequestViewModel,
                blockingViewModel = blockingViewModel,
                toolbarViewModel = toolbarViewModel
            )
        }
        composable(Routes.FriendRequestScreen.route) {
            FriendRequestScreenLayout(
                friendRequestViewModel = friendRequestViewModel,
                viewModel = userProfileViewModel,
                navController = navController
            )
        }
        composable(Routes.CompassScreen.route){
            if (ActivityCompat.checkSelfPermission(
                    compassPermissionHandler.activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    compassPermissionHandler.activity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
//                return@composable
                Log.i(
                    "NavGraph",
                    "Permissions not granted; Requesting them now"
                )
                ActivityCompat.requestPermissions(
                    compassPermissionHandler.activity,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    COMPASS_PERMISSION_REQUEST_CODE
                )
            }
            CompassScreenLayout(
                compassViewModel = compassViewModel,
                compassPermissionHandler = compassPermissionHandler,
                navController = navController
            )
        }
    }
}
