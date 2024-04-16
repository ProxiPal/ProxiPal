package com.mongodb.app.friends

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mongodb.app.data.SyncRepository
import com.mongodb.app.domain.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow


data class FriendRequestUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val friendRequests: List<FriendshipRequest> = emptyList(),
    val users: List<UserProfile> = emptyList()
)

//ADDED BY GEORGE FU
class FriendRequestViewModel(private val repository: SyncRepository) : ViewModel() {
    private val _friendRequests = MutableStateFlow<List<FriendshipRequest>>(emptyList())
    val friendRequests: StateFlow<List<FriendshipRequest>> = _friendRequests.asStateFlow()

    private val _users = MutableStateFlow<List<UserProfile>>(emptyList())
    val users: StateFlow<List<UserProfile>> = _users.asStateFlow()

    private val _feedback = MutableSharedFlow<String>()
    val feedback: SharedFlow<String> = _feedback.asSharedFlow()

    private val _uiState = MutableStateFlow(FriendRequestUiState())
    val uiState: StateFlow<FriendRequestUiState> = _uiState.asStateFlow()

    init {
        fetchFriendRequests()
        fetchUsers()
    }

    private fun fetchFriendRequests() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val currentUserId = repository.getCurrentUserId()
                repository.getFriendRequests(currentUserId).collect { resultsChange ->
                    _uiState.value = _uiState.value.copy(
                        friendRequests = resultsChange.list.filter { it.status == "pending" && it.receiverFriendId == currentUserId },
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.localizedMessage, isLoading = false)
            }
        }
    }

    private fun fetchUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val currentUserId = repository.getCurrentUserId()
                repository.getAllUserProfiles().collect { userProfiles ->
                    _uiState.value = _uiState.value.copy(
                        users = userProfiles.filter { it.ownerId != currentUserId && !it.friends.contains(currentUserId) },
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.localizedMessage, isLoading = false)
            }
        }
    }

    fun onSendFriendRequestButtonClicked(friendId: String) {
        viewModelScope.launch {
            val isValidFriendId = repository.validateFriendId(friendId)
            if (isValidFriendId) {
                val currentUserID = repository.getCurrentUserId()
                repository.sendFriendRequest(currentUserID, friendId)
                _feedback.emit("Friend request successfully sent.")
            } else {
                _feedback.emit("Invalid Friend ID")
            }
        }
    }

    fun respondToFriendRequest(requestId: String, accepted: Boolean) {
        viewModelScope.launch {
            repository.respondToFriendRequest(requestId, accepted)
            fetchFriendRequests() // Refresh friend requests list
        }
    }

    companion object {
        fun factory(repository: SyncRepository) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(FriendRequestViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return FriendRequestViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}