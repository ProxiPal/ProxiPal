package com.mongodb.app.ui.userprofiles

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.mongodb.app.data.USER_PROFILE_BIOGRAPHY_MAXIMUM_CHARACTER_AMOUNT
import com.mongodb.app.data.USER_PROFILE_NAME_MAXIMUM_CHARACTER_AMOUNT
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserProfileViewModel : ViewModel() {
    /*
    ===== Variables =====
     */
    // Accessible and editable only in this class
    // A "data holder observable flow" for current and new states
    private val _userProfileUiState = MutableStateFlow(UserProfileUiState())


    /*
    ===== Properties =====
     */
    // Read-only state flow for access outside this class
    val userProfileUiState: StateFlow<UserProfileUiState> = _userProfileUiState.asStateFlow()

    var userProfileFirstName by mutableStateOf("")
        private set

    var userProfileLastName by mutableStateOf("")
        private set

    var userProfileBiography by mutableStateOf("")
        private set

    var isEditingUserProfile by mutableStateOf(false)
        private set


    /*
    ===== Functions =====
     */
    /**
    Update the user profile first name
     */
    fun updateUserProfileFirstName(newFirstName: String){
        if (newFirstName.length <= USER_PROFILE_NAME_MAXIMUM_CHARACTER_AMOUNT) {
            userProfileFirstName = newFirstName
        }
    }

    /**
    Update the user profile last name
     */
    fun updateUserProfileLastName(newLastName: String){
        if (newLastName.length <= USER_PROFILE_NAME_MAXIMUM_CHARACTER_AMOUNT) {
            userProfileLastName = newLastName
        }
    }

    /**
    Update the user profile biography
     */
    fun updateUserProfileBiography(newBiography: String){
        if (newBiography.length <= USER_PROFILE_BIOGRAPHY_MAXIMUM_CHARACTER_AMOUNT) {
            userProfileBiography = newBiography
        }
    }

    /**
    Toggles whether the user is currently updating their user profile
     */
    fun toggleUserProfileEditMode(){
        isEditingUserProfile = !isEditingUserProfile
    }

    /**
     * Returns how many more characters are allowed before the corresponding character limit is reached
     */
    fun getRemainingCharacterAmountFirstName(): Int{
        return USER_PROFILE_NAME_MAXIMUM_CHARACTER_AMOUNT - userProfileFirstName.length
    }

    /**
     * Returns how many more characters are allowed before the corresponding character limit is reached
     */
    fun getRemainingCharacterAmountLastName(): Int{
        return USER_PROFILE_NAME_MAXIMUM_CHARACTER_AMOUNT - userProfileLastName.length
    }

    /**
     * Returns how many more characters are allowed before the corresponding character limit is reached
     */
    fun getRemainingCharacterAmountBiography(): Int{
        return USER_PROFILE_BIOGRAPHY_MAXIMUM_CHARACTER_AMOUNT - userProfileBiography.length
    }
}