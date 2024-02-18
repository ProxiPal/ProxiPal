package com.mongodb.app.navigation

sealed class Routes(val route: String) {
    object ConnectWithOthersScreen : Routes("connectwithothersscreen")
    object LocationPermissionsScreen : Routes("locationpermissionsscreen")
}