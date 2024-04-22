package com.mongodb.app.data.blocking_censoring

import com.mongodb.app.domain.UserProfile
import org.mongodb.kbson.ObjectId


/**
 * Contains necessary functions for blocking and text censoring with [UserProfile] objects
 */
interface IBlockingCensoring {
    // region Blocking
    suspend fun updateUsersBlocked(userId: ObjectId, shouldBlock: Boolean)
    // endregion Blocking


    // region Censoring
    suspend fun updateTextCensoringState(userId: ObjectId)
    // endregion Censoring
}