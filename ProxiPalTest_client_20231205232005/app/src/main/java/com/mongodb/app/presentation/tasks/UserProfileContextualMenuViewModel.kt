package com.mongodb.app.presentation.tasks
// TODO Might need to move this class to a new package to avoid confusion being in the "tasks" package

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.mongodb.app.data.SyncRepository
import com.mongodb.app.domain.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class UserProfileContextualMenuViewModel constructor(
    private val repository: SyncRepository,
    private val userProfileViewModel: UserProfileViewModel
) : ContextualMenuViewModel(repository = repository) {

    fun deleteUserProfile(userProfile: UserProfile) {
        CoroutineScope(Dispatchers.IO).launch {
            if (repository.isUserProfileMine(userProfile)) {
                runCatching {
                    repository.deleteUserProfile(userProfile)
                }
            } else {
                userProfileViewModel.showPermissionsMessage()
            }
        }
        close()
    }
}
