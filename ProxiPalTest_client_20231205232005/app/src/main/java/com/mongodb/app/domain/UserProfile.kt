package com.mongodb.app.domain

import org.mongodb.kbson.ObjectId
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey


/*
Contributions:
- Kevin Kubota (entire file)
 */


/**
 * The user profile object that gets added and saved to the database
 */
class UserProfile : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var firstName: String = ""
    var lastName: String = ""
    var biography: String = ""
    var ownerId: String = ""

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is UserProfile) return false
        if (this._id != other._id) return false
        if (this.firstName != other.firstName) return false
        if (this.lastName != other.lastName) return false
        if (this.biography != other.biography) return false
        if (this.ownerId != other.ownerId) return false
        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + firstName.hashCode()
        result = 31 * result + lastName.hashCode()
        result = 31 * result + biography.hashCode()
        result = 31 * result + ownerId.hashCode()
        return result
    }
}
