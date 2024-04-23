package com.mongodb.app.friends


import org.mongodb.kbson.ObjectId
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey


class FriendshipRequest : RealmObject {
    @PrimaryKey
    var _id: String = "" // Change to string type
    var senderId: String = "" // ID of the user sending the request
    var receiverFriendId: String = "" // FriendID of the user receiving the request
    var status: String = "pending"
    var senderName: String = ""
    var receiverName: String = ""
}



