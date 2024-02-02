package com.mongodb.app.navigation

interface NavigationDestination {
    /**
     * Unique name to define the path for a composable
     */
    val route: String

    /**
     * String that is the title to be displayed for the screen.
     */
    val title: String
}