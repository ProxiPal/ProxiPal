package com.mongodb.app.domain


import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import org.mongodb.kbson.ObjectId
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class Report: RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var userReported: String = ""
    var reasons: RealmList<String> = realmListOf()
    var comments: String = ""
    var ownerId: String = ""
}

