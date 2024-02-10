package com.mongodb.app.presentation.userprofiles

import com.mongodb.app.data.SyncRepository
import com.mongodb.app.domain.UserProfile
import com.mongodb.app.presentation.tasks.ContextualMenuViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * TODO View model for the contextual menu, but may not be necessary as users should not be able to delete their profile like this
 */
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
