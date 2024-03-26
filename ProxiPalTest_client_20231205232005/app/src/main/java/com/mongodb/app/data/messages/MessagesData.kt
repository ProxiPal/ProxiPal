package com.mongodb.app.data.messages


val MOCK_MESSAGE_LIST = listOf(
    "Hello",
    "World",
    "This is a very long message."
)

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