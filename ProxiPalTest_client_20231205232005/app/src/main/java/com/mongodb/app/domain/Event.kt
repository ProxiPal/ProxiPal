package com.mongodb.app.domain

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId
import java.util.Date

class Event(): RealmObject {
    @PrimaryKey var _id: ObjectId = ObjectId()
    var name: String = ""
    var description: String = ""
    var date: String = ""
    var time: String = ""
    var duration: String = ""
    var location: String = ""
    var attendeeIds: RealmList<String> = realmListOf()
    var announcement: RealmList<String> = realmListOf()

    var owner_id: String = ""
}