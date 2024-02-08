package com.mongodb.app.presentation.userprofiles
// TODO Might need to move this class to a new package to avoid confusion being in the "tasks" package

import com.mongodb.app.data.SyncRepository
import com.mongodb.app.domain.UserProfile
import com.mongodb.app.presentation.tasks.ContextualMenuViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
