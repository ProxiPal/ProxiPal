package com.mongodb.app.data.blocking_censoring

import org.mongodb.kbson.ObjectId


/**
 * A mock class to be used in preview Composable functions
 */
class MockBlockingCensoring: IBlockingCensoring {
    override suspend fun updateUsersBlocked(
        userId: ObjectId,
        shouldBlock: Boolean
    ) = Unit

    override suspend fun updateTextCensoringState(userId: ObjectId) = Unit
}