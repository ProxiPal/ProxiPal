package com.mongodb.app.data.compassscreen

enum class CompassConnectionType{
    // When a user is not currently waiting for or meeting with another user
    OFFLINE,
    // When a user is currently meeting with their matched user
    MEETING

    /*
     (1) By default, users enter an OFFLINE state
     (2) You can only try to connect with users in an OFFLINE state
     (3) A successful connection between users will change their state from OFFLINE to MEETING
     (4) If canceling a request to meet up, both matched users will change state from MEETING to OFFLINE
     */
}