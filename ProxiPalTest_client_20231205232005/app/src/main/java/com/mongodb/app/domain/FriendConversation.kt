package com.mongodb.app.domain

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId


/*
Contributions:
- Kevin Kubota (everything in this file)
 */


/**
 * A database object for uniquely identifying a friend conversation
 */
class FriendConversation : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    /**
     * Collection of user string IDs (see FriendMessage "ownerId" field)
     */
    var usersInvolved: RealmList<String> = realmListOf()
    /**
     * Collection of message string IDs (the FriendMessage ObjectId "_id" field as a string)
     */
    var messagesSent: RealmList<String> = realmListOf()

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is FriendConversation) return false
        if (this._id != other._id) return false
        if (this.usersInvolved != other.usersInvolved) return false
        if (this.messagesSent != other.messagesSent) return false
        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + usersInvolved.hashCode()
        result = 31 * result + messagesSent.hashCode()
        return result
    }

    /**
     * Add a reference to a [FriendMessage] object
     */
    fun addMessage(messageId: ObjectId){
        // Do not use .toString(), it returns a string representation of the ID,
        // ... not the ID value itself
        messagesSent.add(messageId.toHexString())
    }

    /**
     * Removes a reference to a [FriendMessage] object
     */
    fun removeMessage(messageId: ObjectId){
        // Do not use .toString(), it returns a string representation of the ID,
        // ... not the ID value itself
        messagesSent.remove(messageId.toHexString())
    }
}