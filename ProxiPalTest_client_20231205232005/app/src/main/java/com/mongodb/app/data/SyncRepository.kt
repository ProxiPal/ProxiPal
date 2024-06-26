package com.mongodb.app.data

import android.util.Log
import com.mongodb.app.TAG
import com.mongodb.app.app
import com.mongodb.app.data.blocking_censoring.IBlockingCensoringRealm
import com.mongodb.app.data.messages.IConversationsRealm
import com.mongodb.app.data.messages.IMessagesRealm
import com.mongodb.app.data.messages.SHOULD_PRINT_REALM_CONFIG_INFO
import com.mongodb.app.data.messages.SubscriptionNameAllMessages
import com.mongodb.app.data.messages.SubscriptionNameMyFriendConversations
import com.mongodb.app.data.userprofiles.SHOULD_USE_TASKS_ITEMS
import com.mongodb.app.domain.Event
import com.mongodb.app.domain.FriendConversation
import com.mongodb.app.domain.FriendMessage
import com.mongodb.app.domain.Item
import com.mongodb.app.domain.Report
import com.mongodb.app.domain.UserProfile
import com.mongodb.app.friends.FriendshipRequest
import com.mongodb.app.location.CustomGeoPoint
import com.mongodb.app.ui.messages.empty
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.annotations.ExperimentalGeoSpatialApi
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.mongodb.User
import io.realm.kotlin.mongodb.exceptions.SyncException
import io.realm.kotlin.mongodb.subscriptions
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import io.realm.kotlin.mongodb.sync.SyncSession
import io.realm.kotlin.mongodb.syncSession
import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.query.RealmQuery
import io.realm.kotlin.query.Sort
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.geo.Distance
import io.realm.kotlin.types.geo.GeoCircle
import io.realm.kotlin.types.geo.GeoPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
import java.util.SortedSet
import kotlin.time.Duration.Companion.seconds
import java.util.UUID




/*
Contributions:
- Kevin Kubota (Review #1: added functions relating to user profiles, see below)
- Kevin Kubota (Review #3: Implemented functions for friend conversations and messages;
... This mainly includes everything in the regions named "Messages" and "Conversations")
- Kevin Kubota (Review #4: Added database functions related to user blocking and text censoring)
- Marco Pacini (location related tasks only)
 */


// region Extensions
fun String.toObjectId(): ObjectId {
    return ObjectId(this)
}

fun SortedSet<String>.toRealmList(): RealmList<String>{
    val elementAmount = this.size
    // Converts the original set to a list
    val setToList = this.toList()
    // Creates an empty realm list
    val setToRealmList: RealmList<String> = realmListOf()
    // Iterate through the list and set each element to the realm list
    // Sets don't use indexes but list and realm list must
    for (index in 0..<elementAmount){
        setToRealmList.add(setToList[index])
    }
    return setToRealmList
}

fun RealmList<String>.toObjectIdList(): RealmList<ObjectId>{
    val realmList: RealmList<ObjectId> = realmListOf()
    for (string in this){
        try{
            if (string.isNotBlank() && string.isNotEmpty()){
                realmList.add(string.toObjectId())
            }
            realmList.add(string.toObjectId())
        }
        catch (e: Exception){
            Log.e(
                TAG(),
                "!!!: Caught exception \"${e}\" while converting a list of strings to ObjectIds; " +
                        "Skipping converting string = \"$string\" to an ObjectId"
            )
        }
    }
    return realmList
}
// endregion Extensions


private const val SubscriptionNameAllUserProfiles = "AllUserProfiles"


/**
 * Repository for accessing Realm Sync.
 * Working functions and code for Item classes has been copied for UserProfile classes
 */
interface SyncRepository {
    // region Functions


    // region RealmFunctions
    /**
     * Returns the active [SubscriptionType].
     */
    fun getActiveSubscriptionType(realm: Realm? = null): SubscriptionType

    /**
     * Pauses synchronization with MongoDB. This is used to emulate a scenario of no connectivity.
     */
    fun pauseSync()

    /**
     * Resumes synchronization with MongoDB.
     */
    fun resumeSync()

    /**
     * Gets the current user's ID
     */
    fun getCurrentUserId(): String

    /**
     * Closes the realm instance held by this repository.
     */
    fun close()
    // endregion RealmFunctions

    /*
    These functions are from an existing template
     */
    // region Tasks/Items
    /**
     * Returns a flow with the tasks for the current subscription.
     */
    fun getTaskList(): Flow<ResultsChange<Item>>

    /**
     * Update the `isComplete` flag for a specific [Item].
     */
    suspend fun toggleIsComplete(task: Item)

    /**
     * Adds a task that belongs to the current user using the specified [taskSummary].
     */
    suspend fun addTask(taskSummary: String)

    /**
     * Updates the Sync subscriptions based on the specified [SubscriptionType].
     */
    suspend fun updateSubscriptionsItems(subscriptionType: SubscriptionType)

    /**
     * Deletes a given task.
     */
    suspend fun deleteTask(task: Item)

    /**
     * Whether the given [task] belongs to the current user logged in to the app.
     */
    fun isTaskMine(task: Item): Boolean
    // endregion Tasks/Items

    // Contributed by Kevin Kubota
    /*
    These functions currently handle Creating, Reading, and Updating a user profile
     */
    // region User profiles
    /**
     * Returns a query to get a specific [UserProfile]
     */
    fun getQuerySpecificUserProfile(realm: Realm, ownerId: String): RealmQuery<UserProfile>

    /**
     * Returns a query to get all [UserProfile]s
     */
    fun getQueryAllUserProfiles(realm: Realm): RealmQuery<UserProfile>

    /**
     * Returns the specified [UserProfile]
     */
    fun readUserProfile(ownerId: String): Flow<ResultsChange<UserProfile>>

    /**
     * Returns all [UserProfile] objects
     */
    fun readUserProfiles(): Flow<ResultsChange<UserProfile>>
  /*
     * Returns a flow with the current user's profile.
     * Modified by Marco Pacini to fix issues
     */
    /*
       * Returns a flow with the current user's profile.
       * Modified by Marco Pacini to fix issues
       */
    fun getCurrentUserProfileList(): Flow<ResultsChange<UserProfile>>

    /**
     * Adds a user profile that belongs to the current user using the specified parameters
     */
    suspend fun addUserProfile(firstName: String, lastName: String, biography: String, instagramHandle: String, twitterHandle : String, linktreeHandle : String, linkedinHandle : String)

    /**
     * Updates a possible existing user profile for the current user in the database using the specified parameters
     */
    suspend fun updateUserProfile(firstName: String, lastName: String, biography: String, instagramHandle: String, twitterHandle: String, linktreeHandle : String, linkedinHandle : String)

    /**
     * Updates the Sync subscription based on the specified [SubscriptionType].
     */
    suspend fun updateSubscriptionsUserProfiles(subscriptionType: SubscriptionType)

    /**
     * Deletes a given user profile.
     */
    suspend fun deleteUserProfile(userProfile: UserProfile)

    /**
     * Whether the given [userProfile] belongs to the current user logged in to the app.
     */
    fun isUserProfileMine(userProfile: UserProfile): Boolean
    // endregion User profiles

    // Contribution: Marco Pacini
    /*
    These functions currently handle user location related tasks
     */
    // region location
    /**
     * Updates a possible existing user profile's location for the current user in the database using the specified latitude and longitude
     */
    suspend fun updateUserProfileLocation(latitude: Double, longitude: Double)

    /**
     * Returns a flow with nearby user profiles within a specified radius
     */
    fun getNearbyUserProfileList(userLatitude: Double, userLongitude: Double, radiusInKilometers: Double, selectedInterests: List<String> = emptyList(), selectedIndustries: List<String> = emptyList(), otherFilters: List<String> = emptyList()): Flow<ResultsChange<UserProfile>>
    // endregion location
  
    // endregion Functions
  
    suspend fun updateUserProfileInterests(interest:String)

    suspend fun updateUserProfileIndustries(industry:String)

    //march16 George Fu
    suspend fun updateUserProfilePhotos(photoList: List<String>)
    fun getUserProfilePhotos(): Flow<List<String>>

    //march17 George Fu
    suspend fun updateUserSelectedFilters(selectedInterests: List<String>, selectedIndustries: List<String>, otherFilters: List<String>)
    suspend fun clearUserSelectedFilters()

    //region User Rating System

    /**
     * Updates another user's rating score when the current user rates them.
     *
     * Takes in the other user's ownerId as input and adds the current user's ownerId
     * to the list of users that have rated them already (so they cannot rate them more than once).
     *
     * ratingGiven is true for like and false for dislike.
     */
    suspend fun rateOtherUser(otherUserOwnerId: String, ratingGiven: Boolean)

    //april2
    suspend fun sendFriendRequest(senderId: String, receiverFriendId: String)

    suspend fun respondToFriendRequest(requestId: String, accepted: Boolean)

    fun getFriendRequests(receiverFriendId: String): Flow<ResultsChange<FriendshipRequest>>


    //april2
    suspend fun isUserIdValid(userId: String): Boolean

    //april2
    suspend fun getCurrentUserFriendsId(): String?

    //APRIL12
    fun getAllUserProfiles(): Flow<List<UserProfile>>

    fun getRealmInstance(): Realm?

    suspend fun addUserToFriendList(requestId: String)

    suspend fun removeFriendBidirectional(userId: String, friendId: String)

    suspend fun addReport(reportedUser: String, reasonsList: List<String>, comment: String)

    suspend fun addEvent(eventName: String, eventDescription: String, eventDate: String, eventTime: String, eventDuration: String, eventLocation: String)

    suspend fun updateEvent(eventId:String, eventName:String, eventDescription:String, eventDate:String, eventTime:String, eventDuration:String, eventLocation:String)

    suspend fun joinEvent(eventId:String)
    suspend fun leaveEvent(eventId:String)

    suspend fun addAnnouncement(eventId:String, newAnnouncement:String)
    suspend fun getEventAttendees(eventId: String) : List<UserProfile>
    suspend fun getEventById(eventId: String): Flow<ResultsChange<Event>>
    suspend fun getMyEventList(): Flow<ResultsChange<Event>>

    fun isEventOwner(event: Event): Boolean

    fun isEventAttendee(event:Event): Boolean

    suspend fun getOtherEventList(): Flow<ResultsChange<Event>>

    suspend fun deleteEvent(event:Event)

    // endregion Functions
}


/**
 * Repo implementation used in runtime.
 */
class RealmSyncRepository(
    onSyncError: (session: SyncSession, error: SyncException) -> Unit
) : SyncRepository, IMessagesRealm, IConversationsRealm, IBlockingCensoringRealm {

    private val realm: Realm
    private val config: SyncConfiguration
    private val currentUser: User
        get() = app.currentUser!!
    override fun getRealmInstance(): Realm {
        return realm
    }

    init {
        // Contributed by Kevin Kubota
        // This assignment impacts what type of object can be queried.
        // If trying to query A when the sync configuration is set for B,
        // ... the app will crash if querying anything other than B.
        // If errors still persist, try deleting and re-running the app.
        val schemaSet = if (SHOULD_USE_TASKS_ITEMS) setOf(Item::class)
        else setOf(
            UserProfile::class,
            CustomGeoPoint::class,
            FriendMessage::class,
            FriendConversation::class,
            FriendshipRequest::class,
            Report::class,
            Event::class)

        config = SyncConfiguration.Builder(currentUser, schemaSet)
            .schemaVersion(1)
            .initialSubscriptions { realm ->
                // Subscribe to the active subscriptionType - first time defaults to MINE
                val activeSubscriptionType = SubscriptionType.ALL
                if (SHOULD_USE_TASKS_ITEMS) {
                    add(
                        getQueryItems(realm, activeSubscriptionType),
                        activeSubscriptionType.name
                    )
                } else {
                    add(
                        getQueryAllUserProfiles(realm),
                        SubscriptionNameAllUserProfiles
                    )
                    // Subscribe to receive any updates on all messages
                    // Then can possibly query to get only specific messages
                    add(
                        getQueryAllMessages(realm),
                        SubscriptionNameAllMessages
                    )
                    // Subscribe to receive any updates on only my conversations
                    add(
                        getQueryMyConversations(realm),
                        SubscriptionNameMyFriendConversations
                    )
                    add(
                        getQueryFriendshipRequests(realm),
                        "FriendshipRequests")
                    add(
                        getQueryEvents(realm),
                        "Event"
                    )
                    add(
                        getQueryReports(realm),
                        "Report"
                    )

                }

            }

            .errorHandler { session: SyncSession, error: SyncException ->
                onSyncError.invoke(session, error)
            }
            .waitForInitialRemoteData()
            .build()

        realm = Realm.open(config)

        // Mutable states must be updated on the UI thread
        CoroutineScope(Dispatchers.Main).launch {
            // Manually adding subscriptions
            if (realm.subscriptions.size == 0){
                realm.subscriptions.update {
                    add(
                        getQueryAllUserProfiles(realm),
                        SubscriptionNameAllUserProfiles
                    )
                    // Subscribe to receive any updates on all messages
                    // Then can possibly query to get only specific messages
                    add(
                        getQueryAllMessages(realm),
                        SubscriptionNameAllMessages
                    )
                    // Subscribe to receive any updates on only my conversations
                    add(
                        getQueryMyConversations(realm),
                        SubscriptionNameMyFriendConversations
                    )
                    add(
                        getQueryFriendshipRequests(realm),
                        "FriendshipRequests")
                    add(
                        getQueryEvents(realm),
                        "Event"
                    )
                    add(
                        getQueryReports(realm),
                        "Report"
                    )
                }
            }
            realm.subscriptions.waitForSynchronization()
        }

        if (SHOULD_PRINT_REALM_CONFIG_INFO) {
            // After configuration changes, realm stays the same but config changes every time
            // This leads to the app crashing when trying to interact with Realm after a configuration change
            for (subscription in realm.subscriptions){
                Log.i(
                    TAG(),
                    "RealmSyncRepository: Subscription = \"${subscription}\" ;; " +
                            "Subscription name = \"${subscription.name}\" ;; " +
                            "Subscription description = \"${subscription.queryDescription}\""
                )
            }
        }

        Log.i(
            TAG(),
            "RealmSyncRepository: Realm is now set up"
        )
    }


    // region RealmFunctions
    override fun getActiveSubscriptionType(realm: Realm?): SubscriptionType {
        val realmInstance = realm ?: this.realm
        val subscriptions = realmInstance.subscriptions
        val firstOrNull = subscriptions.firstOrNull()
        return when (val name = firstOrNull?.name) {
            null,
            SubscriptionType.MINE.name -> SubscriptionType.MINE

            SubscriptionType.ALL.name -> SubscriptionType.ALL
            else -> throw IllegalArgumentException("Invalid Realm Sync subscription: '$name'")
        }
    }

    override fun pauseSync() {
        realm.syncSession.pause()
    }

    override fun resumeSync() {
        realm.syncSession.resume()
    }

    override fun close() = realm.close()

    override fun getCurrentUserId(): String {
        return currentUser.id
    }
    // endregion RealmFunctions

    /*
    These functions are from an existing template
     */
    // region Tasks/Items
    override fun getTaskList(): Flow<ResultsChange<Item>> {
        return realm.query<Item>()
            .sort(Pair("_id", Sort.ASCENDING))
            .asFlow()
    }

    override suspend fun toggleIsComplete(task: Item) {
        realm.write {
            val latestVersion = findLatest(task)
            latestVersion!!.isComplete = !latestVersion.isComplete
        }
    }

    override suspend fun addTask(taskSummary: String) {
        val task = Item().apply {
            owner_id = currentUser.id
            summary = taskSummary
        }
        realm.write {
            copyToRealm(task)
        }
    }

    // Vichet Chim - Database fuction for Reports
    override suspend fun addReport(reportedUser: String, reasonsList: List<String>, comment: String) {
        val reasonRealmList = reasonsList.toRealmList()
        val report = Report().apply {
            this.userReported = reportedUser
            this.reasons = reasonRealmList
            this.comments = comment
            this.ownerId = currentUser.id
        }
        realm.write{
            copyToRealm(report)
        }
    }

    private fun getQueryReports(realm: Realm): RealmQuery<Report> {
        return realm.query<Report>()
    }

    // Vichet Chim - Database functions for Events
    private fun getValidIdString(idString:String): String {
        return idString.removePrefix("BsonObjectId(").removeSuffix(")")
    }
    override suspend fun addEvent(eventName:String, eventDescription:String, eventDate:String, eventTime:String, eventDuration:String, eventLocation: String){
        val event= Event().apply{
            name = eventName
            description = eventDescription
            date = eventDate
            time = eventTime
            duration = eventDuration
            location = eventLocation
            attendeeIds.add(currentUser.id)
            owner_id = currentUser.id
        }
        realm.write{
            copyToRealm(event)
        }
    }


    override suspend fun updateEvent(eventId:String, eventName:String, eventDescription:String, eventDate:String, eventTime:String, eventDuration:String, eventLocation:String) {
        val objectId = ObjectId(getValidIdString(eventId))
        realm.write{
            val liveEvent = query<Event>("_id == $0", objectId).find().first()
            liveEvent.name = eventName
            liveEvent.description = eventDescription
            liveEvent.date = eventDate
            liveEvent.time = eventTime
            liveEvent.duration = eventDuration
            liveEvent.location = eventLocation
        }

    }

    override suspend fun joinEvent(eventId:String){
        val objectId = ObjectId(getValidIdString(eventId))
        realm.write{
            val liveEvent = query<Event>("_id == $0", objectId).find().first()
            liveEvent.attendeeIds.add(currentUser.id)
        }
    }

    override suspend fun leaveEvent(eventId:String){
        val objectId = ObjectId(getValidIdString(eventId))
        realm.write{
            val liveEvent = query<Event>("_id == $0", objectId).find().first()
            liveEvent.attendeeIds.remove(currentUser.id)
        }
    }

    override suspend fun addAnnouncement(eventId:String, newAnnouncement:String){
        val objectId = ObjectId(getValidIdString(eventId))
        realm.write{
            val liveEvent = query<Event>("_id == $0", objectId).find().first()
            liveEvent.announcement.add(newAnnouncement)
        }
    }


    override suspend fun getEventAttendees(eventId: String) : List<UserProfile> {
        val objectId = ObjectId(getValidIdString(eventId))
        val event = realm.query<Event>("_id == $0", objectId).find().first()

        val attendeeIds = event.attendeeIds
        val attendees = mutableListOf<UserProfile>()
        attendeeIds.forEach { attendeeId ->
            val attendeeProfile = realm.query<UserProfile>("ownerId== $0", attendeeId).find().firstOrNull()
            if (attendeeProfile != null){
                attendees.add(attendeeProfile)
            }
        }
        return attendees
    }

    override fun isEventAttendee(event:Event): Boolean = event.attendeeIds.contains(currentUser.id)

    override suspend fun getEventById(eventId: String): Flow<ResultsChange<Event>> {
        val objectId = ObjectId(getValidIdString(eventId))
        return realm.query<Event>("_id == $0", objectId).asFlow()
    }
    override suspend fun getMyEventList(): Flow<ResultsChange<Event>> {
        return realm.query<Event>("attendeeIds CONTAINS $0", currentUser.id)
            .sort(Pair("date", Sort.ASCENDING))
            .asFlow()
    }

    override fun isEventOwner(event: Event): Boolean = event.owner_id == currentUser.id



    override suspend fun getOtherEventList(): Flow<ResultsChange<Event>> {
        return realm.query<Event>("NOT attendeeIds CONTAINS $0", currentUser.id)
            //.sort(Pair("date", Sort.ASCENDING))
            .asFlow()
    }
    override suspend fun deleteEvent(event:Event){
        realm.write{
            delete(findLatest(event)!!)
        }
        realm.subscriptions.waitForSynchronization(10.seconds)
    }

    private fun getQueryEvents(realm: Realm): RealmQuery<Event> {
        return realm.query<Event>()
    }

    // End of database functions for Events



    override suspend fun updateSubscriptionsItems(subscriptionType: SubscriptionType) {
        realm.subscriptions.update {
            removeAll()
            val query = when (subscriptionType) {
                SubscriptionType.MINE -> getQueryItems(realm, SubscriptionType.MINE)
                SubscriptionType.ALL -> getQueryItems(realm, SubscriptionType.ALL)
            }
            add(query, subscriptionType.name)
        }
    }

    override suspend fun deleteTask(task: Item) {
        realm.write {
            delete(findLatest(task)!!)
        }
        realm.subscriptions.waitForSynchronization(10.seconds)
    }

    override fun isTaskMine(task: Item): Boolean = task.owner_id == currentUser.id

    private fun getQueryItems(realm: Realm, subscriptionType: SubscriptionType): RealmQuery<Item> =
        when (subscriptionType) {
            SubscriptionType.MINE -> realm.query("owner_id == $0", currentUser.id)
            SubscriptionType.ALL -> realm.query()
        }
    // endregion Tasks/Items

    // Contributed by Kevin Kubota
    /*
    These functions currently handle Creating, Reading, and Updating a user profile
    Deleting has not been testing for functionality and may not be necessary, for now
     */
    // region User profiles
    override fun getQuerySpecificUserProfile(
        realm: Realm,
        ownerId: String
    ): RealmQuery<UserProfile> {
        return realm.query<UserProfile>("ownerId == $0", ownerId)
    }

    override fun getQueryAllUserProfiles(realm: Realm): RealmQuery<UserProfile> {
        // Should return all user profiles as "0" should be an invalid ID
        return realm.query<UserProfile>("ownerId != $0", "0")
    }

    override fun readUserProfile(ownerId: String): Flow<ResultsChange<UserProfile>> {
        return getQuerySpecificUserProfile(realm, ownerId)
            .sort(Pair("_id", Sort.ASCENDING))
            .asFlow()
    }

    override fun readUserProfiles(): Flow<ResultsChange<UserProfile>> {
        return getQueryAllUserProfiles(realm)
            .sort(Pair("_id", Sort.ASCENDING))
            .asFlow()
    }

    override fun getCurrentUserProfileList(): Flow<ResultsChange<UserProfile>> {
        return realm.query<UserProfile>("ownerId == $0", currentUser.id)
            .asFlow()
    }

    private fun getQueryFriendshipRequests(realm: Realm): RealmQuery<FriendshipRequest> {
        return realm.query<FriendshipRequest> ("status == $0", "pending")
    }

    //added the social media handling
    override suspend fun addUserProfile(firstName: String, lastName: String, biography: String, instagramHandle: String, twitterHandle: String, linktreeHandle : String, linkedinHandle : String) {
        // The "owner ID" added is associated with the user currently logged into the app
        val userProfile = UserProfile().apply {
            ownerId = currentUser.id
            this.firstName = firstName
            this.lastName = lastName
            this.biography = biography
            this.instagramHandle = instagramHandle
            this.twitterHandle = twitterHandle
            this.linktreeHandle = linktreeHandle
            this.linkedinHandle = linkedinHandle

            // Added by Marco Pacini, to make sure there is an initial location
            // it will be updated by the connect screen
            this.location = CustomGeoPoint(0.0,0.0)
            //this.interests.add("")

            // Initialize a newly created account with no users yet blocked
            this.usersBlocked = realmListOf(String.empty)
            this.hasTextCensoringEnabled = false
        }
        realm.write {
            copyToRealm(userProfile, updatePolicy = UpdatePolicy.ALL)
        }
    }

    override suspend fun updateUserProfile(firstName: String, lastName: String, biography: String, instagramHandle: String, twitterHandle: String, linktreeHandle: String, linkedinHandle: String) {

        // Queries inside write transaction are live objects
        // Queries outside would be frozen objects and require a call to the mutable realm's .findLatest()
        val frozenUserProfile = getCurrentUserProfile(realm = realm).find()
        // In case the query result list is empty, check first before calling ".first()"
        val frozenFirstUserProfile = if (frozenUserProfile.size > 0) {
            frozenUserProfile.first()
        } else {
            null
        }
        if (frozenFirstUserProfile == null) {
            // Create a new user profile before applying the updated changes
            addUserProfile(
                firstName = firstName,
                lastName = lastName,
                biography = biography,

                instagramHandle = instagramHandle,
                twitterHandle = twitterHandle,
                linktreeHandle = linktreeHandle,
                linkedinHandle = linkedinHandle
            )
            return
        }
        realm.write {
            findLatest(frozenFirstUserProfile)?.let { liveUserProfile ->
                liveUserProfile.firstName = firstName
                liveUserProfile.lastName = lastName
                liveUserProfile.biography = biography

                liveUserProfile.instagramHandle = instagramHandle
                liveUserProfile.twitterHandle = twitterHandle
                liveUserProfile.linktreeHandle = linktreeHandle
                liveUserProfile.linkedinHandle = linkedinHandle
            }
        }
    }

    override suspend fun updateSubscriptionsUserProfiles(subscriptionType: SubscriptionType) {
        realm.subscriptions.update {
            removeAll()
            val query = when (subscriptionType) {
                SubscriptionType.MINE -> getQueryUserProfiles(realm, SubscriptionType.MINE)
                SubscriptionType.ALL -> getQueryUserProfiles(realm, SubscriptionType.ALL)
            }
            add(query, subscriptionType.name)
        }
    }

    override suspend fun deleteUserProfile(userProfile: UserProfile) {
        realm.write {
            delete(findLatest(userProfile)!!)
        }
        realm.subscriptions.waitForSynchronization(10.seconds)
    }

    override fun isUserProfileMine(userProfile: UserProfile): Boolean =
        userProfile.ownerId == currentUser.id

    private fun getQueryUserProfiles(
        realm: Realm,
        subscriptionType: SubscriptionType
    ): RealmQuery<UserProfile> =
        when (subscriptionType) {
            // Make sure the fields referenced in the query exactly match their name in the database
            SubscriptionType.MINE -> getQuerySpecificUserProfile(realm, currentUser.id)
            SubscriptionType.ALL -> getQueryAllUserProfiles(realm)
        }

    private fun getCurrentUserProfile(realm: Realm) : RealmQuery<UserProfile>{
        return realm.query("ownerId == $0", currentUser.id)
    }

    // endregion User profiles

    // Contribution: Marco Pacini
    /*
    These functions primarily handle user location related tasks
     */
    // region location
    override suspend fun updateUserProfileLocation(latitude: Double, longitude: Double) {
        // Queries inside write transaction are live objects
        // Queries outside would be frozen objects and require a call to the mutable realm's .findLatest()
        val frozenUserProfile = getCurrentUserProfile(realm = realm).find()
        // In case the query result list is empty, check first before calling ".first()"
        val frozenFirstUserProfile = if (frozenUserProfile.size > 0) {
            frozenUserProfile.first()
        } else {
            null
        }
        if (frozenFirstUserProfile == null) {
            // Create a new user profile before applying the updated changes
            addUserProfile(
                firstName = "empty",
                lastName = "empty",
                biography = "empty",
                instagramHandle = "empty",
                twitterHandle = "empty",
                linktreeHandle = "empty",
                linkedinHandle = "empty"
            )
            return
        }
        realm.write {
            findLatest(frozenFirstUserProfile)?.let { liveUserProfile ->
                liveUserProfile.location?.latitude = latitude
                liveUserProfile.location?.longitude = longitude
            }
        }
    }

    /**
     * Returns a flow with nearby user profiles within a specified radius, at the time of the query.
     * Because the query returns frozen objects, this will likely be used consecutively in intervals to get consistently updated locations.
     * [userLatitude] and [userLongitude] are the current user's location, used to form center of the search radius.
     */
    @OptIn(ExperimentalGeoSpatialApi::class)
    override fun getNearbyUserProfileList(userLatitude: Double, userLongitude: Double, radiusInKilometers: Double, selectedInterests: List<String>, selectedIndustries: List<String>, otherFilters: List<String>): Flow<ResultsChange<UserProfile>>{
        val circleAroundUser = GeoCircle.create(
            center = GeoPoint.create(userLatitude, userLongitude),
            radius = Distance.fromKilometers(radiusInKilometers)
        )
        val interestsQueryPart = if (selectedInterests.isNotEmpty()) {
            selectedInterests.joinToString(prefix = "interests IN [", postfix = "]", separator = ", ") { "'$it'" }
        } else ""

        val industriesQueryPart = if (selectedIndustries.isNotEmpty()) {
            selectedIndustries.joinToString(prefix = "industries IN [", postfix = "]", separator = ", ") { "'$it'" }
        } else ""

        val otherFiltersQueryPart = if (otherFilters.isNotEmpty()) {
            otherFilters.joinToString(prefix = "otherFilters IN [", postfix = "]", separator = ", ") { "'$it'" }
        } else ""

        val filterQueries = listOf(interestsQueryPart, industriesQueryPart, otherFiltersQueryPart).filter { it.isNotEmpty() }.joinToString(separator = " OR ")

        val query = if (filterQueries.isNotEmpty()) {
            "location GEOWITHIN $circleAroundUser AND ownerId != $0 AND ($filterQueries)"
        } else {
            "location GEOWITHIN $circleAroundUser AND ownerId != $0"
        }

        //return realm.query<UserProfile>(query, currentUser.id).find().asFlow()

        // TODO: The below statement just displays all users in the database for testing purposes
        return realm.query<UserProfile>("ownerId != $0", currentUser.id).find().asFlow()
    }


    //endregion location


    // region Messages
    override fun getQueryAllMessages(realm: Realm): RealmQuery<FriendMessage> {
        // Should return all messages since owner ID "0" should not be a valid ID
        return realm.query("ownerId != $0", "0")
    }

    override fun getQuerySpecificMessage(realm: Realm, messageId: ObjectId): RealmQuery<FriendMessage> {
        return realm.query("_id == $0", messageId)
    }

    override fun getQuerySpecificMessages(realm: Realm, friendConversation: FriendConversation): RealmQuery<FriendMessage> {
        return realm.query("_id IN $0", friendConversation.messagesSent.toObjectIdList())
    }

    override suspend fun createMessage(newMessage: FriendMessage){
        realm.write {
            copyToRealm(newMessage, updatePolicy = UpdatePolicy.ALL)
        }
    }

    override fun readMessage(messageId: ObjectId): Flow<ResultsChange<FriendMessage>> {
        return realm.query<FriendMessage>("_id == $0", messageId)
            .sort(Pair("_id", Sort.ASCENDING))
            .asFlow()
    }

    override fun readConversationMessages(friendConversation: FriendConversation): Flow<ResultsChange<FriendMessage>> {
        // Messages sent is a list of strings, but actual message ID is a ObjectId
        // Convert list of strings to list of ObjectIds first before doing the query
        return getQuerySpecificMessages(realm, friendConversation)
            .asFlow()
    }

    override suspend fun updateMessage(messageId: ObjectId, newMessage: String) {
        // Queries inside write transaction are live objects
        // Queries outside would be frozen objects and require a call to the mutable realm's .findLatest()
        val frozenObjects = getQuerySpecificMessage(
            realm = realm,
            messageId = messageId
        )
            .find()
        // In case the query result list is empty, check first before calling ".first()"
        val frozenObject = (if (frozenObjects.size > 0) frozenObjects.first() else null) ?: return
        realm.write {
            findLatest(frozenObject)?.let{
                    liveObject ->
                liveObject.message = newMessage
                liveObject.hasBeenUpdated = true
            }
        }
    }

    override suspend fun deleteMessage(messageId: ObjectId) {
        // Similar process to updating a Realm object
        // Involves frozen and live objects but deletion can only occur on live objects

        // Queries inside write transaction are live objects
        // Queries outside would be frozen objects and require a call to the mutable realm's .findLatest()
        // Only get the specific message being deleted
        val frozenObjects = realm.query<FriendMessage>("_id == $0", messageId)
            .find()

        // In case the query result list is empty, check first before calling ".first()"
        val frozenObject = (if (frozenObjects.size > 0) frozenObjects.first() else null) ?: return

        // Delete the object
        realm.write {
            findLatest(frozenObject)?.let {
                delete(it)
            }
        }
    }
    // endregion Messages


    // region Conversations
    override fun getQueryMyConversations(realm: Realm): RealmQuery<FriendConversation> {
        return realm.query("$0 IN usersInvolved", currentUser.id)
    }

    override fun getQuerySpecificConversation(
        realm: Realm,
        usersInvolved: SortedSet<String>
    ): RealmQuery<FriendConversation> {
        return realm.query<FriendConversation>("usersInvolved == $0", usersInvolved)
    }

    override fun getQuerySpecificConversation(
        realm: Realm,
        conversationId: ObjectId
    ): RealmQuery<FriendConversation> {
        return realm.query<FriendConversation>("_id == $0", conversationId)
    }

    override suspend fun createConversation(usersInvolved: SortedSet<String>) {
        val usersInvolvedRealmList: RealmList<String> = usersInvolved.toRealmList()
        Log.i(
            TAG(),
            "RealmSyncRepository: Creating a conversation for users = \"" +
                    "${usersInvolvedRealmList}\""
        )
        // Initial list with an initial empty message (which will count as "invalid" and not be shown in the UI)
        // This is just so the variable gets initialized in the database (since it does not show on realm object creation)
        val messagesSentRealmList: RealmList<String> = realmListOf("")

        val friendConversation = FriendConversation().apply{
            this.usersInvolved = usersInvolvedRealmList
            // Start with an empty list, then message references are added after
            this.messagesSent = messagesSentRealmList
        }
        realm.write{
            copyToRealm(friendConversation, updatePolicy = UpdatePolicy.ALL)
        }
    }

    override fun readConversation(usersInvolved: SortedSet<String>): Flow<ResultsChange<FriendConversation>> {
        return getQuerySpecificConversation(realm, usersInvolved)
            .sort(Pair("_id", Sort.ASCENDING))
            .asFlow()
    }

    override suspend fun updateConversationAdd(
        friendConversation: FriendConversation,
        messageId: ObjectId
    ) {
        updateConversation(
            friendConversation = friendConversation,
            messageId = messageId,
            shouldAddMessage = true
        )
    }

    override suspend fun updateConversationRemove(
        friendConversation: FriendConversation,
        messageId: ObjectId
    ) {
        updateConversation(
            friendConversation = friendConversation,
            messageId = messageId,
            shouldAddMessage = false
        )
    }

    /**
     * To simplify common code for updating a conversation by either adding or removing a message
     */
    private suspend fun updateConversation(
        friendConversation: FriendConversation,
        messageId: ObjectId,
        shouldAddMessage: Boolean
    ) {
        // Queries inside write transaction are live objects
        // Queries outside would be frozen objects and require a call to the mutable realm's .findLatest()
        val frozenObjects = getQuerySpecificConversation(
            realm,
            friendConversation.usersInvolved.toSortedSet()
        )
            .find()
        // In case the query result list is empty, check first before calling ".first()"
        val frozenObject = if (frozenObjects.size > 0) frozenObjects.first() else null
        if (frozenObject == null) {
            Log.e(
                TAG(),
                "RealmSyncRepository: Could not update conversation with ID = \"${friendConversation._id}\""
            )
            // Do not do this, results in 2 conversation objects being created (MessagesViewModel already creates 1)
//            // This is more of a safety check
//            // Create a new conversation object if it was somehow deleted before or during the updating process
//            createConversation(
//                usersInvolved = friendConversation.usersInvolved.toSortedSet()
//            )
            return
        }
        realm.write {
            if (shouldAddMessage){
                findLatest(frozenObject)?.addMessage(messageId)
            }
            else{
                findLatest(frozenObject)?.removeMessage(messageId)
            }
        }
    }
    // endregion Conversations


    // region BlockingCensoring
    override suspend fun updateUsersBlocked(userId: ObjectId, shouldBlock: Boolean) {
        // Queries inside write transaction are live objects
        // Queries outside would be frozen objects and require a call to the mutable realm's .findLatest()
        val frozenObject = getCurrentUserProfile(realm).find()
        val frozenFirst = (if (frozenObject.size > 0) frozenObject.first() else null) ?: return
        realm.write{
            val liveObject = findLatest(frozenFirst)
            // Use .toHexString() over .toString()
            val userIdString = userId.toHexString()

            if (liveObject != null){
                // Block the user
                if (shouldBlock && !liveObject.usersBlocked.contains(userIdString)){
                    liveObject.usersBlocked.add(userIdString)
                }
                // Unblock the user
                else if (!shouldBlock && liveObject.usersBlocked.contains(userIdString)){
                    liveObject.usersBlocked.remove(userIdString)
                }
            }
        }
    }

    override suspend fun updateTextCensoringState(userId: ObjectId) {
        // Queries inside write transaction are live objects
        // Queries outside would be frozen objects and require a call to the mutable realm's .findLatest()
        val frozenObject = getCurrentUserProfile(realm).find()
        val frozenFirst = (if (frozenObject.size > 0) frozenObject.first() else null) ?: return
        realm.write{
            val liveObject = findLatest(frozenFirst)
            if (liveObject != null){
                liveObject.hasTextCensoringEnabled = !liveObject.hasTextCensoringEnabled
            }
        }
    }
    // endregion BlockingCensoring
    
    
    override suspend fun updateUserProfileInterests(interest:String) {
        // Queries inside write transaction are live objects
        // Queries outside would be frozen objects and require a call to the mutable realm's .findLatest()
        val frozenUserProfile = getCurrentUserProfile(realm = realm).find()
        // In case the query result list is empty, check first before calling ".first()"
        val frozenFirstUserProfile = if (frozenUserProfile.size > 0) {
            frozenUserProfile.first()
        } else {
            null
        }
        if (frozenFirstUserProfile == null) {
            Log.i(
                TAG(),
                "RealmSyncRepository: Creating a new user profile with the given parameters for " +
                        "current user ID = \"${currentUser.id}\"...; " +
                        "Skipping rest of user profile update function..."
            )
            // Create a new user profile before applying the updated changes
            addUserProfile(
                firstName = "empty",
                lastName = "empty",
                biography = "empty",
                instagramHandle = "empty",
                twitterHandle = "empty",
                linktreeHandle = "empty",
                linkedinHandle = "empty"
            )
            return
        }
        when (getCurrentUserProfile(realm = realm).find().size) {
            // Create a new profile for the user if they do not have one already in the database
            // This may not be necessary as users will get their initial profiles added to the database
            // ... once they register an account and deleting their profile will only occur when
            // ... deleting their account (unsure if account deletion will be implemented)
            0 -> {
                Log.i(
                    TAG(),
                    "RealmSyncRepository: No user profiles found with owner ID \"${currentUser.id}\""
                )
            }

            1 -> Log.i(
                TAG(),
                "RealmSyncRepository: Exactly 1 user profile found with owner ID \"${currentUser.id}\""
            )

            else -> Log.i(
                TAG(),
                "RealmSyncRepository: Multiple user profiles found with owner ID \"${currentUser.id}\""
            )
        }
        realm.write {
            findLatest(frozenFirstUserProfile)?.let { liveUserProfile ->
                if (liveUserProfile.interests.contains(interest)){
                    liveUserProfile.interests.remove(interest)
                } else{
                    liveUserProfile.interests.add(interest)
                }

            }
        }
    }


    override suspend fun updateUserProfileIndustries(industry:String) {
        // Queries inside write transaction are live objects
        // Queries outside would be frozen objects and require a call to the mutable realm's .findLatest()
        val frozenUserProfile = getCurrentUserProfile(realm = realm).find()
        // In case the query result list is empty, check first before calling ".first()"
        val frozenFirstUserProfile = if (frozenUserProfile.size > 0) {
            frozenUserProfile.first()
        } else {
            null
        }
        if (frozenFirstUserProfile == null) {
            Log.i(
                TAG(),
                "RealmSyncRepository: Creating a new user profile with the given parameters for " +
                        "current user ID = \"${currentUser.id}\"...; " +
                        "Skipping rest of user profile update function..."
            )
            // Create a new user profile before applying the updated changes
            addUserProfile(
                firstName = "empty",
                lastName = "empty",
                biography = "empty",
                instagramHandle = "empty",
                twitterHandle = "empty",
                linktreeHandle = "empty",
                linkedinHandle = "empty"
            )
            return
        }
        when (getCurrentUserProfile(realm = realm).find().size) {
            // Create a new profile for the user if they do not have one already in the database
            // This may not be necessary as users will get their initial profiles added to the database
            // ... once they register an account and deleting their profile will only occur when
            // ... deleting their account (unsure if account deletion will be implemented)
            0 -> {
                Log.i(
                    TAG(),
                    "RealmSyncRepository: No user profiles found with owner ID \"${currentUser.id}\""
                )
            }

            1 -> Log.i(
                TAG(),
                "RealmSyncRepository: Exactly 1 user profile found with owner ID \"${currentUser.id}\""
            )

            else -> Log.i(
                TAG(),
                "RealmSyncRepository: Multiple user profiles found with owner ID \"${currentUser.id}\""
            )
        }
        realm.write {
            findLatest(frozenFirstUserProfile)?.let { liveUserProfile ->
                if (liveUserProfile.industries.contains(industry)){
                    liveUserProfile.industries.remove(industry)
                } else{
                    liveUserProfile.industries.add(industry)
                }

            }
        }
    }

    //march16 George Fu
    override suspend fun updateUserProfilePhotos(photoList: List<String>) {
        realm.write {
            val userProfile = query<UserProfile>().find().firstOrNull()
            userProfile?.let {
                it.profilePhotos.clear()
                it.profilePhotos.addAll(photoList)
            }
        }
    }

    override fun getUserProfilePhotos(): Flow<List<String>> = flow {
        val userProfile = realm.query<UserProfile>().find().firstOrNull()
        emit(userProfile?.profilePhotos?.toList() ?: emptyList())
    }
    //march17 George Fu
    override suspend fun updateUserSelectedFilters(selectedInterests: List<String>, selectedIndustries: List<String>, otherFilters: List<String>) {
        realm.write {
            val userProfile = query<UserProfile>().find().firstOrNull() ?: return@write
            userProfile.selectedInterests.clear()
            userProfile.selectedInterests.addAll(selectedInterests)
            userProfile.selectedIndustries.clear()
            userProfile.selectedIndustries.addAll(selectedIndustries)
            userProfile.otherFilters.clear()
            userProfile.otherFilters.addAll(otherFilters)
        }
    }
    override suspend fun clearUserSelectedFilters() {
        realm.write {
            // Assuming you have a method to get the current user's profile
            val userProfile = query<UserProfile>("ownerId == $0", app.currentUser!!.id).first().find()
            userProfile?.let { profile ->
                // Clear or reset the selections
                profile.selectedInterests.clear()
                profile.selectedIndustries.clear()
                profile.otherFilters.clear()

            }
        }
    }

    // region rating system

    // takes the other user's ID and the current user's rating (true for like, false for dislike)
    // and rates the other user
    override suspend fun rateOtherUser(otherUserOwnerId: String, ratingGiven: Boolean) {
        val otherUserProfile = getQuerySpecificUserProfile(realm = realm, ownerId = otherUserOwnerId)
            .find()
            .firstOrNull()

        otherUserProfile?.let { userProfile ->
            realm.write {
                findLatest(userProfile)?.let { liveUserProfile ->
                    if (!liveUserProfile.usersThatRatedMe.contains(currentUser.id)) {
                        if (ratingGiven) {
                            liveUserProfile.userLikes++
                        } else {
                            liveUserProfile.userDislikes++
                        }
                        liveUserProfile.usersThatRatedMe.add(currentUser.id)
                    }
                }
            }
        }
    }
    //april2
    override suspend fun sendFriendRequest(senderId: String,receiverFriendId: String) {
        try {
            // Ensure the subscriptions are ready and check specifically for the required subscription
            realm.subscriptions.waitForSynchronization()
            if (!realm.subscriptions.any { it.name == "FriendshipRequests" }) {
                throw IllegalStateException("Subscription for FriendshipRequests not found")
            }

            realm.write {
                // Create and add the FriendshipRequest
                val newRequest = FriendshipRequest().apply {
                    this._id = UUID.randomUUID().toString()
                    this.senderId = senderId
                    this.receiverFriendId = receiverFriendId
                    this.status = "pending"
                }
                copyToRealm(newRequest)
                Log.d("idk", "Friend request sent: ${newRequest._id}")
            }
        } catch (e: Exception) {
            Log.e("idk", "Error sending friend request", e)
        }
    }



    //april2
    override suspend fun respondToFriendRequest(requestId: String, accepted: Boolean) {
        realm.write {
            val request = query<FriendshipRequest>("_id == $0", requestId).first().find()
            request?.status = if (accepted) "accepted" else "declined"
        }
    }
    //april2
    override fun getFriendRequests(receiverFriendId: String): Flow<ResultsChange<FriendshipRequest>> {
        return realm.query<FriendshipRequest>("receiverFriendId == $0", receiverFriendId).asFlow()
    }

    //april2
    override suspend fun isUserIdValid(userId: String): Boolean {
        val realm = getRealmInstance() ?: return false
        val user = realm.query<UserProfile>("ownerId == $0", userId).find().firstOrNull()
        return user != null
    }


    //april2
    override suspend fun getCurrentUserFriendsId(): String? = withContext(Dispatchers.IO) {
        getCurrentUserId()
    }

    //APRIL12
    override fun getAllUserProfiles(): Flow<List<UserProfile>> = flow {
        val userProfiles = realm.query<UserProfile>().find().toList()
        emit(userProfiles)
    }

    override suspend fun addUserToFriendList(requestId: String) {
        realm.write {
            val request = query<FriendshipRequest>("_id == $0", requestId).first().find()
            if (request != null && request.status == "accepted") {
                val senderProfile = query<UserProfile>("ownerId == $0", request.senderId).first().find()
                val receiverProfile = query<UserProfile>("ownerId == $0", request.receiverFriendId).first().find()
                if (senderProfile != null && receiverProfile != null) {
                    // Update both users' friend lists
                    senderProfile.friends.add(receiverProfile.ownerId)
                    receiverProfile.friends.add(senderProfile.ownerId)
                } else {
                    Log.e("FriendTest", "One of the user profiles is null: senderProfile=$senderProfile, receiverProfile=$receiverProfile")
                }
            } else {
                Log.e("FriendTest", "FriendshipRequest not found or not accepted: request=$request")
            }
        }
    }
    override suspend fun removeFriendBidirectional(userId: String, friendId: String) {
        realm.write {
            val user = query<UserProfile>("ownerId == $0", userId).first().find()
            val friend = query<UserProfile>("ownerId == $0", friendId).first().find()

            user?.friends?.remove(friendId)
            friend?.friends?.remove(userId)
        }
    }


}

/**
 * Mock repo for generating the Compose layout preview.
 */
class MockRepository : SyncRepository {
    // region Functions
    override fun getActiveSubscriptionType(realm: Realm?): SubscriptionType = SubscriptionType.ALL
    override fun pauseSync() = Unit
    override fun resumeSync() = Unit
    override fun getCurrentUserId(): String {
        return String.empty
    }

    override fun close() = Unit

    override fun getTaskList(): Flow<ResultsChange<Item>> = flowOf()
    override suspend fun toggleIsComplete(task: Item) = Unit
    override suspend fun addTask(taskSummary: String) = Unit
    override suspend fun updateSubscriptionsItems(subscriptionType: SubscriptionType) = Unit
    override suspend fun deleteTask(task: Item) = Unit
    override fun isTaskMine(task: Item): Boolean = task.owner_id == MOCK_OWNER_ID_MINE


    // Contributed by Kevin Kubota
    override fun getQuerySpecificUserProfile(
        realm: Realm,
        ownerId: String
    ): RealmQuery<UserProfile> {
        TODO("Not yet implemented")
    }

    override fun getQueryAllUserProfiles(realm: Realm): RealmQuery<UserProfile> {
        TODO("Not yet implemented")
    }

    override fun readUserProfile(ownerId: String): Flow<ResultsChange<UserProfile>> {
        TODO("Not yet implemented")
    }
    override fun readUserProfiles(): Flow<ResultsChange<UserProfile>> = flowOf()
    override fun getCurrentUserProfileList(): Flow<ResultsChange<UserProfile>> = flowOf() // modified by Marco
    override suspend fun addUserProfile(firstName: String, lastName: String, biography: String, instagramHandle: String, twitterHandle: String, linktreeHandle: String, linkedinHandle: String) =

        Unit
    override suspend fun updateUserProfile(firstName: String, lastName: String, biography: String, instagramHandle: String, twitterHandle: String, linktreeHandle: String, linkedinHandle: String) =
        Unit
    override suspend fun updateSubscriptionsUserProfiles(subscriptionType: SubscriptionType) = Unit
    override suspend fun deleteUserProfile(userProfile: UserProfile) = Unit
    override fun isUserProfileMine(userProfile: UserProfile): Boolean =
        userProfile.ownerId == MOCK_OWNER_ID_MINE

    override suspend fun updateUserProfileLocation(latitude: Double, longitude: Double) =
        Unit
    // endregion Functions

    override fun getNearbyUserProfileList(userLatitude: Double, userLongitude: Double, radiusInKilometers: Double, selectedInterests: List<String>, selectedIndustries: List<String>, otherFilters: List<String>): Flow<ResultsChange<UserProfile>> = flowOf()

    override suspend fun updateUserProfileInterests(interest:String) = Unit

    override suspend fun updateUserProfileIndustries(industry:String) = Unit

    //march16 George Fu
    override suspend fun updateUserProfilePhotos(photoList: List<String>) = Unit
    override fun getUserProfilePhotos(): Flow<List<String>> = flowOf(listOf())

    //march17 George Fu
    override suspend fun updateUserSelectedFilters(selectedInterests: List<String>, selectedIndustries: List<String>, otherFilters: List<String>) = Unit
    override suspend fun clearUserSelectedFilters() = Unit

    override suspend fun rateOtherUser(otherUserOwnerId: String, ratingGiven: Boolean) = Unit
    //april
    override suspend fun sendFriendRequest(senderId: String, receiverFriendId: String) = Unit
    override suspend fun respondToFriendRequest(requestId: String, accepted: Boolean) = Unit
    override fun getFriendRequests(receiverFriendId: String): Flow<ResultsChange<FriendshipRequest>> = flowOf()


    //april2
    override suspend fun isUserIdValid(userId: String): Boolean {
        return true
    }
    //april2
    override suspend fun getCurrentUserFriendsId(): String? = null

    //APRIL12
    override fun getAllUserProfiles(): Flow<List<UserProfile>> = flowOf()

    override fun getRealmInstance(): Realm? = null

    override suspend fun addUserToFriendList(requestId: String) = Unit

    override suspend fun removeFriendBidirectional(userId: String, friendId: String) = Unit

    // Vichet Chim  - report function
    override suspend fun addReport(reportedUser: String, reasonsList: List<String>, comment: String) = Unit
    // Vichet Chim  - event functions
    override suspend fun addEvent(eventName: String, eventDescription: String, eventDate: String, eventTime: String, eventDuration:String, eventLocation: String) = Unit
    override suspend fun updateEvent(eventId:String, eventName:String, eventDescription:String, eventDate:String, eventTime:String, eventDuration:String, eventLocation:String) {    }

    override suspend fun joinEvent(eventId:String) {}

    override suspend fun leaveEvent(eventId:String) {}

    override suspend fun addAnnouncement(eventId:String, newAnnouncement:String) {}

    override suspend fun getEventAttendees(eventId: String) : List<UserProfile> {return listOf()}

    override suspend fun getEventById(eventId: String): Flow<ResultsChange<Event>> = flowOf()
    override suspend fun getMyEventList(): Flow<ResultsChange<Event>> = flowOf()

    override suspend fun getOtherEventList(): Flow<ResultsChange<Event>> = flowOf()

    override fun isEventOwner(event: Event): Boolean {
        return event.owner_id == MOCK_OWNER_ID_MINE
    }
    override fun isEventAttendee(event:Event): Boolean
    { return event.attendeeIds.contains(MOCK_OWNER_ID_MINE) }

    override suspend fun deleteEvent(event:Event) = Unit
    // end of event functions

    companion object {
        const val MOCK_OWNER_ID_MINE = "A"
        const val MOCK_OWNER_ID_OTHER = "B"

        fun getMockTask(index: Int): Item = Item().apply {
            this.summary = "Task $index"

            // Make every third task complete in preview
            this.isComplete = index % 3 == 0

            // Make every other task mine in preview
            this.owner_id = when {
                index % 2 == 0 -> MOCK_OWNER_ID_MINE
                else -> MOCK_OWNER_ID_OTHER
            }
        }

        // Contributed by Kevin Kubota
        fun getMockUserProfile(index: Int): UserProfile = UserProfile().apply {
            this.firstName = "First Name $index"
            this.lastName = "Last Name $index"
            this.biography = "Biography $index"

            // set the locations to be slightly offset for each mockuser profile
            this.location = CustomGeoPoint(0.0 + index*0.0001,0.0 + index*0.0001)
            // Make every other user profile mine in preview
            // For Compose previews only (in reality, a user will have only 1 profile)
            this.ownerId = when {
                index % 2 == 0 -> MOCK_OWNER_ID_MINE
                else -> MOCK_OWNER_ID_OTHER
            }
        }
    }
}

/**
 * The two types of subscriptions according to item owner.
 */
enum class SubscriptionType {
    MINE, ALL
}