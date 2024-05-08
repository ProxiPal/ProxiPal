package com.mongodb.app.data.messages

import androidx.compose.runtime.mutableStateOf


/*
Contributions:
- Kevin Kubota (everything in this file)
 */


const val MESSAGE_WIDTH_WEIGHT = 0.4f

/**
 * A debugging switch variable on whether to print log messages about Realm instances
 * and their configuration details
 */
const val SHOULD_PRINT_REALM_CONFIG_INFO = false

/**
 * How many characters makes a message "long"
 */
const val LONG_MESSAGE_CHARACTER_THRESHOLD = 10

/**
 * How many users maximum can be in a conversation at once
 */
const val MAX_USERS_PER_CONVERSATION = 2

class MessagesData {
    companion object{
        private val _userIdInFocus = mutableStateOf("")
        val userIdInFocus
            get() = _userIdInFocus

        fun updateUserIdInFocus(userId: String){
            if (userId.isNotEmpty() && userId.isNotBlank()){
                userIdInFocus.value = userId
            }
        }
    }
}