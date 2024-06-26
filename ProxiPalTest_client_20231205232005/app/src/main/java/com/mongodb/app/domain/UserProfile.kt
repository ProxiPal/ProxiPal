package com.mongodb.app.domain

import com.mongodb.app.location.CustomGeoPoint

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import org.mongodb.kbson.ObjectId
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlin.random.Random


/*
Contributions:
- Kevin Kubota (first name, last name, and biography fields)
- Marco Pacini (location attribute)
 */


/**
 * The user profile object that gets added and saved to the database
 */
class UserProfile : RealmObject {
    // Written by Kevin Kubota
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
    var hasTextCensoringEnabled: Boolean = false


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

    // Added by Marco Pacini.
    // Stores the ratings that other users have given this user
    var userLikes: Int = 0
    var userDislikes: Int = 0
    // stores the list of userId's that have already rated this user
    var usersThatRatedMe: RealmList<String> = realmListOf()
    var friends: RealmList<String> = realmListOf()


    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is UserProfile) return false
        if (this._id != other._id) return false
        if (this.firstName != other.firstName) return false
        if (this.lastName != other.lastName) return false
        if (this.biography != other.biography) return false
        if (this.ownerId != other.ownerId) return false
        if (this.hasTextCensoringEnabled != other.hasTextCensoringEnabled) return false
        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + firstName.hashCode()
        result = 31 * result + lastName.hashCode()
        result = 31 * result + biography.hashCode()
        result = 31 * result + ownerId.hashCode()
        result = 31 * result + hasTextCensoringEnabled.hashCode()
        return result
    }

    /**
     * Checks whether another user is blocked by the current user
     */
    fun isUserBlocked(userIdToCheck: String): Boolean{
        if (userIdToCheck.isBlank() || userIdToCheck.isEmpty()){
            return false
        }
        return usersBlocked.contains(userIdToCheck)
    }
}
