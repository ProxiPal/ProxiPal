package com.mongodb.app.domain

import com.mongodb.app.location.CustomGeoPoint

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import org.mongodb.kbson.ObjectId
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey



/*
Contributions:
- Kevin Kubota (first name, last name, and biography fields)
- Marco Pacini (location attribute)
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

    /**
     * List of user IDs which are blocked by the current user
     */
    var usersBlocked: RealmList<String> = realmListOf()

    // Added by Marco Pacini, stores latitude and longitude
    var location: CustomGeoPoint? = null
    var instagramHandle: String = ""
    var twitterHandle: String = ""
    var linktreeHandle: String =""
    var linkedinHandle: String = ""
  
    var interests : RealmList<String> = realmListOf()
    var industries : RealmList<String> = realmListOf()

    //march16 George Fu
    var profilePhotos: RealmList<String> = realmListOf()

    //march17 George Fu
    var selectedInterests : RealmList<String> = realmListOf()
    var selectedIndustries : RealmList<String> = realmListOf()
    var otherFilters : RealmList<String> = realmListOf()

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

    fun isUserBlocked(userIdToCheck: String): Boolean{
        return usersBlocked.contains(userIdToCheck)
    }
}
