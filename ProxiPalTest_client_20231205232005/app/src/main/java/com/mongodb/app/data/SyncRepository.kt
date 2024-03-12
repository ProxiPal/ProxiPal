package com.mongodb.app.data

import android.util.Log
import com.mongodb.app.TAG
import com.mongodb.app.domain.Item
import com.mongodb.app.app
import com.mongodb.app.domain.UserProfile
import com.mongodb.app.location.CustomGeoPoint
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.annotations.ExperimentalGeoSpatialApi
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.realmListOf
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds


/*
Contributions:
- Kevin Kubota (added functions relating to user profiles, see below)
- Marco Pacini (location related tasks only)
 */


/**
 * Repository for accessing Realm Sync.
 * Working functions and code for Item classes has been copied for UserProfile classes
 */
interface SyncRepository {
    /*
    ===== Functions =====
     */
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
     * Closes the realm instance held by this repository.
     */
    fun close()

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
     * Returns a flow with the user profiles for the current subscription.
     */
    fun getUserProfileList(): Flow<ResultsChange<UserProfile>>

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
    fun getNearbyUserProfileList(userLatitude: Double, userLongitude: Double, radiusInKilometers: Double): Flow<ResultsChange<UserProfile>>

    suspend fun updateUserProfileInterests(interest:String)

    suspend fun updateUserProfileIndustries(industry:String)
    // endregion location
}


/**
 * Repo implementation used in runtime.
 */
class RealmSyncRepository(
    onSyncError: (session: SyncSession, error: SyncException) -> Unit
) : SyncRepository {

    // Unsure if there can be more than 1 instance of a Realm
    private val realm: Realm
    private val config: SyncConfiguration
    private val currentUser: User
        get() = app.currentUser!!

    init {
        Log.i(
            TAG(),
            "RealmSyncRepository: Start of Init{}"
        )
        // Contributed by Kevin Kubota
        // This assignment impacts what type of object can be queried.
        // If trying to query A when the sync configuration is set for B,
        // ... the app will crash if querying anything other than B.
        // If errors still persist, try deleting and re-running the app.
        val set = if (SHOULD_USE_TASKS_ITEMS) setOf(Item::class)
        else setOf(UserProfile::class, CustomGeoPoint::class)
        config = SyncConfiguration.Builder(currentUser, set)
            .initialSubscriptions { realm ->
                // Subscribe to the active subscriptionType - first time defaults to MINE
                val activeSubscriptionType = getActiveSubscriptionType(realm)
                if (SHOULD_USE_TASKS_ITEMS) {
                    add(
                        getQueryItems(realm, activeSubscriptionType),
                        activeSubscriptionType.name
                    )
                } else {
                    add(
                        getQueryUserProfiles(realm, activeSubscriptionType),
                        activeSubscriptionType.name
                    )
                }
            }
            .errorHandler { session: SyncSession, error: SyncException ->
                onSyncError.invoke(session, error)
            }
            .waitForInitialRemoteData()
            .build()

        realm = Realm.open(config)

//        // After configuration changes, realm stays the same but config changes every time
//        // This leads to the app crashing when trying to interact with Realm after a configuration change
//        Log.i(
//            TAG(),
//            "RealmSyncRepository: Realm = \"${realm}\""
//        )
//        Log.i(
//            TAG(),
//            "RealmSyncRepository: Config = \"${config}\""
//        )

        // Mutable states must be updated on the UI thread
        CoroutineScope(Dispatchers.Main).launch {
            realm.subscriptions.waitForSynchronization()
        }
    }


    /*
    ===== Functions =====
     */
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
    override fun getUserProfileList(): Flow<ResultsChange<UserProfile>> {
        return realm.query<UserProfile>()
            .sort(Pair("_id", Sort.ASCENDING))
            .asFlow()
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
        }
        realm.write {
            copyToRealm(userProfile, updatePolicy = UpdatePolicy.ALL)
        }
    }

    /**
     * Returns the current user's ID
     */
    fun getCurrentUserId(): String{
        return currentUser.id
    }

    override suspend fun updateUserProfile(firstName: String, lastName: String, biography: String, instagramHandle: String, twitterHandle: String, linktreeHandle: String, linkedinHandle: String) {
        // Queries inside write transaction are live objects
        // Queries outside would be frozen objects and require a call to the mutable realm's .findLatest()
        val frozenUserProfile = getQueryUserProfiles(
            realm = realm,
            subscriptionType = getActiveSubscriptionType(realm)
        ).find()
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
        when (getQueryUserProfiles(
            realm = realm,
            subscriptionType = getActiveSubscriptionType(realm)
        ).find().size) {
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
            SubscriptionType.MINE -> realm.query("ownerId == $0", currentUser.id)
            SubscriptionType.ALL -> realm.query()
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
        val frozenUserProfile = getQueryUserProfiles(
            realm = realm,
            subscriptionType = getActiveSubscriptionType(realm)
        ).find()
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
        when (getQueryUserProfiles(
            realm = realm,
            subscriptionType = getActiveSubscriptionType(realm)
        ).find().size) {
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
    override fun getNearbyUserProfileList(userLatitude: Double, userLongitude: Double, radiusInKilometers: Double): Flow<ResultsChange<UserProfile>>{
        val circleAroundUser = GeoCircle.create(
            center = GeoPoint.create(userLatitude, userLongitude),
            radius = Distance.fromKilometers(radiusInKilometers)
        )
        return realm.query<UserProfile>("location GEOWITHIN $circleAroundUser").query("ownerId != $0", currentUser.id).find().asFlow()
    }

    //endregion location


    override suspend fun updateUserProfileInterests(interest:String) {
        // Queries inside write transaction are live objects
        // Queries outside would be frozen objects and require a call to the mutable realm's .findLatest()
        val frozenUserProfile = getQueryUserProfiles(
            realm = realm,
            subscriptionType = getActiveSubscriptionType(realm)
        ).find()
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
                biography = "empty"
            )
            return
        }
        when (getQueryUserProfiles(
            realm = realm,
            subscriptionType = getActiveSubscriptionType(realm)
        ).find().size) {
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
        val frozenUserProfile = getQueryUserProfiles(
            realm = realm,
            subscriptionType = getActiveSubscriptionType(realm)
        ).find()
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
                biography = "empty"
            )
            return
        }
        when (getQueryUserProfiles(
            realm = realm,
            subscriptionType = getActiveSubscriptionType(realm)
        ).find().size) {
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



}

/**
 * Mock repo for generating the Compose layout preview.
 */
class MockRepository : SyncRepository {
    override fun getActiveSubscriptionType(realm: Realm?): SubscriptionType = SubscriptionType.ALL
    override fun pauseSync() = Unit
    override fun resumeSync() = Unit
    override fun close() = Unit

    override fun getTaskList(): Flow<ResultsChange<Item>> = flowOf()
    override suspend fun toggleIsComplete(task: Item) = Unit
    override suspend fun addTask(taskSummary: String) = Unit
    override suspend fun updateSubscriptionsItems(subscriptionType: SubscriptionType) = Unit
    override suspend fun deleteTask(task: Item) = Unit
    override fun isTaskMine(task: Item): Boolean = task.owner_id == MOCK_OWNER_ID_MINE


    // Contributed by Kevin Kubota
    override fun getUserProfileList(): Flow<ResultsChange<UserProfile>> = flowOf()
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

    override fun getNearbyUserProfileList(userLatitude: Double, userLongitude: Double, radiusInKilometers: Double): Flow<ResultsChange<UserProfile>> = flowOf()

    override suspend fun updateUserProfileInterests(interest:String) = Unit

    override suspend fun updateUserProfileIndustries(industry:String) = Unit

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
