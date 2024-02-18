package com.mongodb.app.data


/*
Contributions:
- Kevin Kubota (entire file)
 */


/**
 * The maximum amount of characters the (first and last) name text fields can have
 */
const val USER_PROFILE_NAME_MAXIMUM_CHARACTER_AMOUNT = 32

/**
 * The maximum amount of characters the biography text field can have
 */
const val USER_PROFILE_BIOGRAPHY_MAXIMUM_CHARACTER_AMOUNT = 256

/**
 * How many lines of text to show at once when editing a text field that may have multiple lines
 */
// Currently, the keyboard that shows up when editing the profile information does not have an Enter key
// ... so this may not be necessary but is included anyway
const val USER_PROFILE_EDIT_MODE_MAXIMUM_LINE_AMOUNT = 4

/**
 * The column (horizontal) weight of the header of a single row of information in the user profile.
 * The column weight of the corresponding row of information will scale inversely to this.
 * (A higher value for the header weight will make headers occupy more space and information occupy less.)
 */
const val USER_PROFILE_ROW_HEADER_WEIGHT = 0.25f

/**
 * A temporary switch value for whether to use the original tasks/items implementation for the entire application.
 * (This is mainly used for debugging purposes.)
 */
const val SHOULD_USE_TASKS_ITEMS = false