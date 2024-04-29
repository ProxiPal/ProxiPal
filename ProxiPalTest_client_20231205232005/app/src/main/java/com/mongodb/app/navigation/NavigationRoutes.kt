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
    object UserInterestsScreen : Routes("userinterestscreen")
    object UserIndustriesScreen : Routes("userindustriescreen")
    object OnboardingScreen : Routes("onboardingscreen")
    object AdvancedScreenSettings : Routes("advancedscreensettings")

    object EventDetails : Routes("event_details/{eventId}") {
        const val EVENT_ID_KEY = "eventId"
        fun createRoute(eventId: String) = "event_details/$eventId"
    }
    object EventScreen:Routes("eventscreen")

    object CreateEvent:Routes("createevent")

    object EditEvent : Routes("event_edit/{eventId}") {
        const val EVENT_ID_KEY = "eventId"
        fun createRoute(eventId: String) = "event_edit/$eventId"
    }

}