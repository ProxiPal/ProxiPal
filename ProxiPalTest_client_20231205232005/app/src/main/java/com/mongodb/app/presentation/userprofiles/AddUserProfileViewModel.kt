package com.mongodb.app.presentation.userprofiles

import android.os.Bundle
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.mongodb.app.data.SyncRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class AddUserProfileEvent {
    class Info(val message: String) : AddUserProfileEvent()
    class Error(val message: String, val throwable: Throwable) : AddUserProfileEvent()
}

class AddUserProfileViewModel(
    private val repository: SyncRepository
) : ViewModel() {

    private val _addUserProfilePopupVisible: MutableState<Boolean> = mutableStateOf(false)
    val addUserProfilePopupVisible: State<Boolean>
        get() = _addUserProfilePopupVisible

    private val _userProfileBiography: MutableState<String> = mutableStateOf("")
    val userProfileBiography: State<String>
        get() = _userProfileBiography

    private val _addUserProfileEvent: MutableSharedFlow<AddUserProfileEvent> = MutableSharedFlow()
    val addUserProfileEvent: Flow<AddUserProfileEvent>
        get() = _addUserProfileEvent

    fun openAddUserProfileDialog() {
        _addUserProfilePopupVisible.value = true
    }

    fun closeAddUserProfileDialog() {
        cleanUpAndClose()
    }

    fun updateUserProfileBiography(userProfileBiography: String) {
        _userProfileBiography.value = userProfileBiography
    }

    fun addUserProfile() {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                repository.addUserProfile(userProfileBiography.value)
            }.onSuccess {
                withContext(Dispatchers.Main) {
                    _addUserProfileEvent.emit(AddUserProfileEvent.Info("User profile '$userProfileBiography' added successfully."))
                }
            }.onFailure {
                withContext(Dispatchers.Main) {
                    _addUserProfileEvent.emit(
                        AddUserProfileEvent.Error(
                            "There was an error while adding the user profile '$userProfileBiography'",
                            it
                        )
                    )
                }
            }
            cleanUpAndClose()
        }
    }

    private fun cleanUpAndClose() {
        _userProfileBiography.value = ""
        _addUserProfilePopupVisible.value = false
    }

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
                    return AddUserProfileViewModel (repository) as T
                }
            }
        }
    }
}
