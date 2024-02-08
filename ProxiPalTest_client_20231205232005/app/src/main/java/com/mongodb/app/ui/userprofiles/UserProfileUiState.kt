package com.mongodb.app.ui.userprofiles

/*
TODO Unsure if this needs to be used in the UserProfileViewModel as it is currently not in use and works fine
 */
data class UserProfileUiState (
    val firstName: String = "",
    val lastName: String = "",
    val biography: String = ""
)