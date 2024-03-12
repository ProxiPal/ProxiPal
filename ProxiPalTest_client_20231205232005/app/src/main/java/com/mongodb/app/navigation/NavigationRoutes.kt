package com.mongodb.app.navigation

/**
 * Navigation routes for the different screens in Proxipal, used in the Navigation Graph
 */
sealed class Routes(val route: String) {
    object ConnectWithOthersScreen : Routes("connectwithothersscreen")
    object LocationPermissionsScreen : Routes("locationpermissionsscreen")
    object UserProfileScreen : Routes("userprofilescreen")
    object HomeScreen : Routes("homescreen")
    object ScreenSettings : Routes("screensettings")
    object FilterScreen: Routes("filterscreen")
}