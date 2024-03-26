package com.mongodb.app.domain

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId


/**
 * A database object for a single message within a [FriendConversation]
 */
class FriendMessage : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var message: String = ""
    // Cannot use BsonDateTime, also Bson timestamp is for Mongo internal use
    var timeSent: Long = Long.MIN_VALUE
    /**
     * The ObjectId of the user who sent the message
     */
    var ownerId: String = ""
    var hasBeenUpdated: Boolean = false
    /**
     * The ObjectId of the [FriendMessage] replying to
     * (Or the empty string if this is not a reply or "deleted" if the original message was deleted)
     */
    var messageIdRepliedTo: String = ""


    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is FriendMessage) return false
        if (this._id != other._id) return false
        if (this.message != other.message) return false
        if (this.timeSent != other.timeSent) return false
        if (this.ownerId != other.ownerId) return false
        if (this.hasBeenUpdated != other.hasBeenUpdated) return false
        if (this.messageIdRepliedTo != other.messageIdRepliedTo) return false
        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + message.hashCode()
        result = 31 * result + timeSent.hashCode()
        result = 31 * result + ownerId.hashCode()
        result = 31 * result + hasBeenUpdated.hashCode()
        result = 31 * result + messageIdRepliedTo.hashCode()
        return result
    }

    /**
     * Checks if a specific [FriendMessage] has been updated
     */
    fun isUpdated(): Boolean{
        return hasBeenUpdated
    }

    /**
     * Checks if a specific [FriendMessage] is a reply to another [FriendMessage]
     */
    fun isAReply(): Boolean{
        return messageIdRepliedTo.isNotEmpty()
    }
}