package com.mongodb.app.domain

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class Report : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var userReported: String = ""
    var reasons: RealmList<String> = realmListOf()
    var comments: String = ""
    var ownerId: String = ""


    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is Report) return false
        if (this._id != other._id) return false
        if (this.userReported != other.userReported) return false
        if (this.reasons != other.reasons) return false
        if (this.comments != other.comments) return false
        if (this.ownerId != other.ownerId) return false
        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + userReported.hashCode()
        result = 31 * result + reasons.hashCode()
        result = 31 * result + comments.hashCode()
        result = 31 * result + ownerId.hashCode()
        return result
    }
}

