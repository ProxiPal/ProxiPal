package com.mongodb.app.presentation.tasks
// TODO Might need to move this class to a new package to avoid confusion being in the "tasks" package

import android.os.Bundle
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.mongodb.app.data.SyncRepository
import com.mongodb.app.domain.UserProfile
import io.realm.kotlin.notifications.InitialResults
import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.notifications.UpdatedResults
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

object UserProfileViewEvent

class UserProfileViewModel constructor(
    private val repository: SyncRepository,
    val userProfileListState: SnapshotStateList<UserProfile> = mutableStateListOf()
) : ViewModel() {

    private val _event: MutableSharedFlow<UserProfileViewEvent> = MutableSharedFlow()
    val event: Flow<UserProfileViewEvent>
        get() = _event

    init {
        viewModelScope.launch {
            repository.getUserProfileList()
                .collect { event: ResultsChange<UserProfile> ->
                    when (event) {
                        is InitialResults -> {
                            userProfileListState.clear()
                            userProfileListState.addAll(event.list)
                        }
                        is UpdatedResults -> {
                            if (event.deletions.isNotEmpty() && userProfileListState.isNotEmpty()) {
                                event.deletions.reversed().forEach {
                                    userProfileListState.removeAt(it)
                                }
                            }
                            if (event.insertions.isNotEmpty()) {
                                event.insertions.forEach {
                                    userProfileListState.add(it, event.list[it])
                                }
                            }
                            if (event.changes.isNotEmpty()) {
                                event.changes.forEach {
                                    userProfileListState.removeAt(it)
                                    userProfileListState.add(it, event.list[it])
                                }
                            }
                        }
                        else -> Unit // No-op
                    }
                }
        }
    }

    // Not in use since toggleIsComplete function is not added/copied for UserProfiles
//    fun toggleIsComplete(task: Item) {
//        CoroutineScope(Dispatchers.IO).launch {
//            repository.toggleIsComplete(task)
//        }
//    }

    fun showPermissionsMessage() {
        viewModelScope.launch {
            _event.emit(UserProfileViewEvent)
        }
    }

    fun isUserProfileMine(userProfile: UserProfile): Boolean = repository.isUserProfileMine(userProfile)

    companion object {
        fun factory(
            repository: SyncRepository,
            owner: SavedStateRegistryOwner,
            defaultArgs: Bundle? = null
        ): AbstractSavedStateViewModelFactory {
            return object : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    return UserProfileViewModel (repository) as T
                }
            }
        }
    }
}
