# Realm Kotlin SDK Template App

## Configuration

Ensure `app/src/main/res/values/atlasConfig.xml` exists and contains the following properties:

- **appId:** your Atlas App Services App ID.
- **baseUrl:** the App Services backend URL. This should be https://realm.mongodb.com in most cases.
- **dataExplorerLink:** the link to the Atlas cluster's collections.

### Using the Atlas App Services UI

The easiest way to use this template app is to log on to [Atlas App Services](https://realm.mongodb.com/) and click the **Create App From Template** button. Choose 
**Real Time Sync**, and then follow the prompts. While the backend app is being 
created, you can download this Kotlin template app pre-configured for your new 
app.

### Cloning from GitHub

If you have cloned this repository from the GitHub
[mongodb/template-app-kotlin-todo](https://github.com/mongodb/template-app-kotlin-todo.git)
repository, you must create a separate App Services App with Device Sync
enabled to use this client. You can find information about how to do this
in the Atlas App Services documentation page:
[Template Apps -> Create a Template App](https://www.mongodb.com/docs/atlas/app-services/reference/template-apps/)

Once you have created the App Services App, replace any value in this client's
`realm_app_id` field with your App Services App ID. For help finding this ID, refer
to: [Find Your Project or App Id](https://www.mongodb.com/docs/atlas/app-services/reference/find-your-project-or-app-id/)

### Download the Client as a Zip File

If you have downloaded this client as a .zip file from the Atlas App Services
UI, it does not contain the App Services App ID. You must replace any value
in this client's `realm_app_id` field in `app/src/main/res/values/atlasConfig.xml`
with your App Services App ID. For help finding this ID, refer to:
[Find Your Project or App Id](https://www.mongodb.com/docs/atlas/app-services/reference/find-your-project-or-app-id/)

## Run the app

- Open this directory in Android Studio.
- Wait for gradle to sync dependencies.
- Build & run the app.

## Issues

Please report issues with the template at https://github.com/mongodb-university/realm-template-apps/issues/new

### User profile related issues and notes (for future reference)

This is a documentation of the errors encountered while trying to implement user profile adding/getting/updating to/from the Mongo database. Some of the errors below were discovered using the logcat tab, either by debugging or running the app normally. All steps taken and what resolved these errors are described below. Note, these solutions may not work in the future if the errors were to re-occur. These are simply what worked during the first error encounter.

## Cannot switch screens to another screen

Add the desired screen to be navigated to in the project's "AndroidManifest.xml" file.

## class {className} not part of configuration domain

Had to update the "SyncConfiguration.Builder()" so that the set parameter was of the same class type as the class listed in the error.
Example:
    "val config = SyncConfiguration.Builder(user, setOf(Toad::class))" would correctly query for classes of type "Toad"
See this link:
    https://www.mongodb.com/docs/realm/sdk/kotlin/sync/open-a-synced-realm/ 

## Unable to add {className} to database; "RLM_ERR_NO_SUBSCRIPTION_FOR_WRITE"; "Cannot write to class {className} when no flexible sync subscription has been created"

Uninstall the ProxiPal app from the phone or emulated phone. The "Important" section listed in this link:
    https://www.mongodb.com/docs/realm/sdk/kotlin/app-services/connect-to-app-services-backend/#std-label-kotlin-connect-to-backend 
was what prompted trying to uninstall and re-install the app due to stating configurations are cached in the app internally and cannot be changed by closing and re-opening the app. 

On a related note, this link:
    https://www.mongodb.com/docs/realm/sdk/kotlin/sync/subscribe/ 
may be helpful since it revolves around properly setting up Sync subscriptions.

Need to update read and write rules in "Rules" under "Data Access" under "App Services" tab on Mongo Atlas website.

## Log message shows successful adding {className} to database but the object is not actually added

A new database collection was first created with a dummy object for user profiles. (Unsure if this was needed but the collection was manually created anyway.) Logged into the Atlas website and went to the "Rules" sub-section of the database's "Data Access" section (on the far left). Then, a rule was added to the collection that holds objects of type {className} in the database. The rule was (1) applicable always, (2) had insert, delete, and search document permissions, and (3) had read all, write all field permissions.

## "RLM_ERR_WRONG_TRANSACTION_STATE"; "Trying to modify database while in read transaction"

In the update function to update data in the database, if queries are in the realm's write(), consider putting them outside. Use the "frozen objects" approach instead of the "live objects" approach using this link:
    https://www.mongodb.com/docs/realm/sdk/kotlin/realm-database/crud/update/ 


## App crashing when trying to interact with Realm instance after a configuration change occurs

For classes that do not update after a configuration change (ex: UserProfileViewModel), their SyncRepository/RealmSyncRepository instance variable must be reset. In classes that have their onCreate() or other functions called after a configuration change (ex: UserProfileScreen), they should directly update the SyncRepository/RealmSyncRepository instance variables in the classes that cannot be updated, within their own onCreate() or other functions that get called after a configuration change.
Example:
    UserProfileScreen has its onCreate() called after a configuration change, but UserProfileViewModel only has its init{} called once. (Note, some classes' init{} do get called after a configuration change, such as RealmSyncRepository.) UserProfileScreen has its own instance variable of type SyncRepository/RealmSyncRepository that gets updated when its onCreate() gets called, or after a configuration change. UserProfileViewModel also has a variable of type SyncRepository/RealmSyncRepository, but it does not get updated after a configuration change like UserProfileScreen does. To update UserProfileViewModel's instance variable of type SyncRepository/RealmSyncRepository, UserProfileScreen calls a function in the other class and passes its own instance variable as an argument so the UserProfileViewModel's instance variable can get updated.

## Cannot login to Realm at user sign in screen

Deleting the app did not work first try. What did work was restarting Android Studio and wiping
emulator device data on restart.

### Issues encountered when working on messages screen

## "RLM_ERR_NO_SUBSCRIPTION_FOR_WRITE"; no flexible subscription error

Needed to manually update the realm instance's subscriptions with 
"realm.subscriptions.update { add(\\subscriptionHere\\) }"

## App crashing when running regularly, then runs without working when debugging

This was mainly caused whenever the realm.subscriptions.update() was called.
What steps I took to fix it was:
(1) Uninstall app
(2) Re-run app and sign in
(3) Revert realm subscriptions to only use user profile and geopoint subscriptions
(4) Run app to check user profile functionality to see if it saves to database
(5) Run app to check if messages screen can show without crashing
(6) Re-instate messages subscriptions
(7) Run app to check if messages can be sent and saved to database

EDIT: It might be better to just have 1 class have a realm instance open, as multiple
classes opening multiple realms causes the subscription-related error in the header
