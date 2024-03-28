package com.mongodb.app.data.compassscreen


/*
Contributions:
- Kevin Kubota (entire file)
 */


/**
 * Denotes what state a user is in
 * <pre></pre>
 * OFFLINE: when a user is not waiting or meeting with another user
 * <pre></pre>
 * WAITING: when a user is waiting for their matched user to accept the connection
 * <pre></pre>
 * MEETING: when a user is currently in a successful connection with their matched user
 */
enum class CompassConnectionType{
    // When a user is not currently waiting for or meeting with another user
    OFFLINE,
    // When a user is waiting for their matched user to accept the connection
    WAITING,
    // When a user is currently meeting with their matched user
    MEETING
}