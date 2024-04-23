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
    private val _uiState = MutableStateFlow(FriendRequestUiState())
    val uiState: StateFlow<FriendRequestUiState> = _uiState.asStateFlow()

    private val _users = MutableStateFlow<List<UserProfile>>(emptyList())
    val users: StateFlow<List<UserProfile>> = _users.asStateFlow()

    private val _feedback = MutableSharedFlow<String>()
    val feedback: SharedFlow<String> = _feedback.asSharedFlow()

    init {
        fetchFriendRequests()
        fetchUsers()
    }

    private fun fetchFriendRequests() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val currentUserId = repository.getCurrentUserId()
                Log.d("FriendRequestViewModel", "Current User ID: $currentUserId")
                repository.getFriendRequests(currentUserId).collect { resultsChange ->
                    Log.d("FriendRequestViewModel", "Fetched requests: ${resultsChange.list}")
                    _uiState.value = _uiState.value.copy(
                        friendRequests = resultsChange.list.filter { it.status == "pending" && it.receiverFriendId == currentUserId },
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("FriendRequestViewModel", "Error fetching friend requests", e)
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
                        users = userProfiles.filter { it.ownerId != currentUserId }, // Filter out the current user
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.localizedMessage, isLoading = false)
            }
        }
    }


    fun onSendFriendRequestButtonClicked(receiverUserId: String) {
        viewModelScope.launch {
            if (receiverUserId.isEmpty()) {
                _feedback.emit("Please enter a valid User ID.")
            } else {
                val userExists = repository.isUserIdValid(receiverUserId) // This should be an existing method in your repository
                if (!userExists) {
                    _feedback.emit("No user found with this ID.")
                } else {
                    // Proceed with sending the friend request because the ID is valid
                    val currentUserId = repository.getCurrentUserId()
                    if (currentUserId != receiverUserId) {
                        repository.sendFriendRequest(currentUserId, receiverUserId)
                        _feedback.emit("Friend request successfully sent.")
                    } else {
                        _feedback.emit("Cannot send a friend request to yourself.")
                    }
                }
            }
        }
    }





    fun respondToFriendRequest(requestId: String, accepted: Boolean) {
        viewModelScope.launch {
            if (accepted) {
                repository.respondToFriendRequest(requestId, true).also {
                    // Add each user to the other's friend list upon acceptance
                    repository.addUserToFriendList(requestId)
                }
            } else {
                repository.respondToFriendRequest(requestId, false)
            }
        }
    }
    fun clearFeedback() {
        viewModelScope.launch {
            _feedback.emit("")
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