package com.mongodb.app.data.messages


/**
 * Designates what message-related action is currently being taken by the user
 */
enum class MessagesUserAction {
    /**
     * User is typing a regular message
     */
    IDLE,

    /**
     * User is updating one of their messages
     */
    UPDATE,

    /**
     * User is deleting one of their messages
     */
    DELETE,

    /**
     * User is replying to another user's message
     */
    REPLY
}