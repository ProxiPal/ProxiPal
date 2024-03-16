package com.mongodb.app.domain

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.BsonObjectId
import org.mongodb.kbson.ObjectId

/**
 * A database object for uniquely identifying a friend conversation
 */
class FriendConversation : RealmObject {
    @PrimaryKey
    var _id: ObjectId = BsonObjectId()
    // Collection of users' IDs (see ownerId field of FriendMessage class)
    var usersInvolved: RealmList<String> = realmListOf()

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is FriendConversation) return false
        if (this._id != other._id) return false
        if (this.usersInvolved != other.usersInvolved) return false
        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + usersInvolved.hashCode()
        return result
    }
}