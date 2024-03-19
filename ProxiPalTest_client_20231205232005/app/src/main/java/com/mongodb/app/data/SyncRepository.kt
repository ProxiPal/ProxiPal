package com.mongodb.app.data

import android.util.Log
import com.mongodb.app.TAG
import com.mongodb.app.app
import com.mongodb.app.data.messages.IConversationsRealm
import com.mongodb.app.data.messages.IMessagesRealm
import com.mongodb.app.data.messages.SHOULD_PRINT_REALM_CONFIG_INFO
import com.mongodb.app.data.userprofiles.SHOULD_USE_TASKS_ITEMS
import com.mongodb.app.domain.FriendConversation
import com.mongodb.app.domain.FriendMessage
import com.mongodb.app.domain.Item
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
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.mongodb.kbson.ObjectId
import java.util.SortedSet
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
     * Returns a flow with the user profiles for the current subscription.
     */
    fun getUserProfileList(): Flow<ResultsChange<UserProfile>>

    /**
     * Adds a user profile that belongs to the current user using the specified parameters
     */
    suspend fun addUserProfile(firstName: String, lastName: String, biography: String)

    /**
     * Updates a possible existing user profile for the current user in the database using the specified parameters
     */
    suspend fun updateUserProfile(firstName: String, lastName: String, biography: String)

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

    // endregion location


    // endregion Functions
}


/**
 * Repo implementation used in runtime.
 */
class RealmSyncRepository(
    onSyncError: (session: SyncSession, error: SyncException) -> Unit
) : SyncRepository, IMessagesRealm, IConversationsRealm {

    private val realm: Realm
    private val config: SyncConfiguration
    private val currentUser: User
        get() = app.currentUser!!
    private val _messagesSubscriptionName: String = "MyFriendMessages"
    private val _conversationsSubscriptionName: String = "MyFriendConversations"

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
        val schemaSet = if (SHOULD_USE_TASKS_ITEMS) setOf(Item::class)
        else setOf(
            UserProfile::class,
            CustomGeoPoint::class,
            FriendMessage::class,
            FriendConversation::class)
        config = SyncConfiguration.Builder(currentUser, schemaSet)
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
                    add(
                        getQueryMessages(realm),
                        _messagesSubscriptionName
                    )
                    add(
                        getQueryConversations(realm),
                        _conversationsSubscriptionName
                    )
                }
            }
            .errorHandler { session: SyncSession, error: SyncException ->
                onSyncError.invoke(session, error)
            }
            .waitForInitialRemoteData()
            .build()

        realm = Realm.open(config)

        if (SHOULD_PRINT_REALM_CONFIG_INFO) {
            // After configuration changes, realm stays the same but config changes every time
            // This leads to the app crashing when trying to interact with Realm after a configuration change
            Log.i(
                TAG(),
                "RealmSyncRepository: Realm = \"${realm}\""
            )
            Log.i(
                TAG(),
                "RealmSyncRepository: Config = \"${config}\""
            )
            Log.i(
                TAG(),
                "RealmSyncRepository: Subscription amount = \"${realm.subscriptions.size}\""
            )
            for (subscription in realm.subscriptions){
                Log.i(
                    TAG(),
                    "RealmSyncRepository: Subscription = \"${subscription}\" ;; " +
                            "Subscription name = \"${subscription.name}\" ;; " +
                            "Subscription description = \"${subscription.queryDescription}\""
                )
            }
        }

        // Mutable states must be updated on the UI thread
        CoroutineScope(Dispatchers.Main).launch {
            Log.i(
                TAG(),
                "RealmSyncRepository: Start of subscription synchronization"
            )
            realm.subscriptions.waitForSynchronization()
            Log.i(
                TAG(),
                "RealmSyncRepository: End of subscription synchronization"
            )
        }
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

    override fun getCurrentUserId(): String{
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

    override suspend fun addUserProfile(firstName: String, lastName: String, biography: String) {
        // The "owner ID" added is associated with the user currently logged into the app
        val userProfile = UserProfile().apply {
            ownerId = currentUser.id
            this.firstName = firstName
            this.lastName = lastName
            this.biography = biography

            // Added by Marco Pacini, to make sure there is an initial location
            // it will be updated by the connect screen
            this.location = CustomGeoPoint(0.0,0.0)
        }
        realm.write {
            copyToRealm(userProfile, updatePolicy = UpdatePolicy.ALL)
        }
    }

    override suspend fun updateUserProfile(firstName: String, lastName: String, biography: String) {
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
                biography = biography
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


    // region Messages
    override suspend fun updateSubscriptionsMessages() {
        // Add the messages query to the realm subscriptions
        realm.subscriptions.update {
            add(getQueryMessages(realm), _messagesSubscriptionName)
        }
        if (SHOULD_PRINT_REALM_CONFIG_INFO){
            for (subscription in realm.subscriptions){
                Log.i(
                    TAG(),
                    "RealmSyncRepository: Message subscription = \"${subscription}\" ;; " +
                            "Subscription name = \"${subscription.name}\" ;; " +
                            "Subscription description = \"${subscription.queryDescription}\""
                )
            }
        }
    }

    override fun getQueryMessages(realm: Realm): RealmQuery<FriendMessage>{
        // Find all messages sent by the current user
        return realm.query("ownerId == $0", currentUser.id)
    }

    override suspend fun createMessage(newMessage: FriendMessage){
        realm.write {
            copyToRealm(newMessage, updatePolicy = UpdatePolicy.ALL)
        }
    }

    override fun readMessage(id: String): Flow<ResultsChange<FriendMessage>> {
        // Comparing "object ids"
        return realm.query<FriendMessage>("_id == $0", ObjectId(id))
            .sort(Pair("_id", Sort.ASCENDING))
            .asFlow()
    }
    // endregion Messages


    // region Conversations
    override suspend fun updateSubscriptionsConversations() {
        // Add the conversations query to the realm subscriptions
        realm.subscriptions.update {
            add(getQueryConversations(realm), _conversationsSubscriptionName)
        }
        if (SHOULD_PRINT_REALM_CONFIG_INFO){
            for (subscription in realm.subscriptions){
                Log.i(
                    TAG(),
                    "RealmSyncRepository: Conversation subscription = \"${subscription}\" ;; " +
                            "Subscription name = \"${subscription.name}\" ;; " +
                            "Subscription description = \"${subscription.queryDescription}\""
                )
            }
        }
    }

    override fun getQueryConversations(realm: Realm): RealmQuery<FriendConversation> {
        // Find all conversations where the current user is involved
        return realm.query("$0 IN usersInvolved", currentUser.id)
    }

    override suspend fun createConversation(usersInvolved: SortedSet<String>) {
        val usersInvolvedRealmList: RealmList<String> = usersInvolved.toRealmList()
        Log.i(
            TAG(),
            "RealmSyncRepository: Conversation users involved = \"" +
                    "${usersInvolvedRealmList}\""
        )
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

    private fun getRealmQuerySpecificConversation(usersInvolved: SortedSet<String>): RealmQuery<FriendConversation>{
        return realm.query<FriendConversation>("$0 == usersInvolved", usersInvolved)
    }

    override fun readConversation(usersInvolved: SortedSet<String>): Flow<ResultsChange<FriendConversation>> {
        return getRealmQuerySpecificConversation(usersInvolved)
            .sort(Pair("_id", Sort.ASCENDING))
            .asFlow()
    }

    override suspend fun updateConversation(
        usersInvolved: SortedSet<String>,
        messageId: String,
        shouldAddMessage: Boolean
    ) {
        // Queries inside write transaction are live objects
        // Queries outside would be frozen objects and require a call to the mutable realm's .findLatest()
        val frozenObjects = getRealmQuerySpecificConversation(usersInvolved)
            .find()
        // In case the query result list is empty, check first before calling ".first()"
        val frozenObject = if (frozenObjects.size > 0) frozenObjects.first() else null
        if (frozenObject == null) {
            Log.i(
                TAG(),
                "RealmSyncRepository: Creating a new conversation object; " +
                        "Users involved = \"${usersInvolved}\""
            )
            // Create a new conversation object
            createConversation(
                usersInvolved = usersInvolved
            )
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

    override suspend fun readReferencedMessages(friendConversation: FriendConversation): MutableList<FriendMessage> {
        val messages: MutableList<FriendMessage> = mutableListOf()
        for (messageId in friendConversation.messagesSent){
            val localJob = CoroutineScope(Dispatchers.Main).async{
                val messageFlow: Flow<ResultsChange<FriendMessage>> = readMessage(messageId)
                // Use .first instead of .collect
                // Otherwise only the 1st message will be retrieved
                // ... since .collect does not terminate automatically (?)
                messageFlow.first{
                    messages.addAll(it.list)
                }
            }
            Log.i(
                TAG(),
                "RealmSyncRepository: (before) Referenced message amount = \"${messages.size}\""
            )
            localJob.await()
            Log.i(
                TAG(),
                "RealmSyncRepository: (after) Referenced message amount = \"${messages.size}\""
            )
        }
        return messages
    }

    // Extension function
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
    // endregion
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
        TODO("Not yet implemented")
    }

    override fun close() = Unit

    override fun getTaskList(): Flow<ResultsChange<Item>> = flowOf()
    override suspend fun toggleIsComplete(task: Item) = Unit
    override suspend fun addTask(taskSummary: String) = Unit
    override suspend fun updateSubscriptionsItems(subscriptionType: SubscriptionType) = Unit
    override suspend fun deleteTask(task: Item) = Unit
    override fun isTaskMine(task: Item): Boolean = task.owner_id == MOCK_OWNER_ID_MINE


    // Contributed by Kevin Kubota
    override fun getUserProfileList(): Flow<ResultsChange<UserProfile>> = flowOf()
    override suspend fun addUserProfile(firstName: String, lastName: String, biography: String) =
        Unit
    override suspend fun updateUserProfile(firstName: String, lastName: String, biography: String) =
        Unit
    override suspend fun updateSubscriptionsUserProfiles(subscriptionType: SubscriptionType) = Unit
    override suspend fun deleteUserProfile(userProfile: UserProfile) = Unit
    override fun isUserProfileMine(userProfile: UserProfile): Boolean =
        userProfile.ownerId == MOCK_OWNER_ID_MINE

    override suspend fun updateUserProfileLocation(latitude: Double, longitude: Double) =
        Unit

    override fun getNearbyUserProfileList(userLatitude: Double, userLongitude: Double, radiusInKilometers: Double): Flow<ResultsChange<UserProfile>> = flowOf()
    // endregion Functions


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
