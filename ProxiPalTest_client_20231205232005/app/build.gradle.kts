plugins {
    id("com.android.application")
    kotlin("android")
    id("io.realm.kotlin") version "1.13.0"
}

android {
    compileSdk = 34
    defaultConfig {
        applicationId = "com.mongodb.app"
        minSdk = 33
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
    kotlinOptions {
        jvmTarget = "1.8" // Set JVM target compatibility
    }
    namespace = "com.mongodb.app"
}

dependencies {
//    For compass screen connections between devices
//    implementation ("com.google.android.gms:play-services-nearby:LATEST_VERSION")
    implementation("com.google.android.gms:play-services-nearby:19.1.0")

    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("androidx.navigation:navigation-compose:2.6.0")
    implementation ("androidx.datastore:datastore-preferences:1.0.0")
    implementation ("com.google.accompanist:accompanist-permissions:0.30.0")
    implementation("androidx.compose.ui:ui:1.3.2")
    implementation("androidx.compose.ui:ui-tooling:1.3.2")
    implementation("androidx.compose.ui:ui-tooling-preview:1.3.2")
    implementation("androidx.compose.foundation:foundation:1.3.1")
    implementation("androidx.compose.runtime:runtime-livedata:1.3.2")

    implementation("androidx.compose.material3:material3:1.0.1")
    implementation("androidx.compose.material:material:1.6.0")
    implementation("androidx.activity:activity-compose:1.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")

    implementation("com.google.android.material:material:1.7.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")


    implementation("com.google.android.gms:play-services-maps:18.2.0")// DON'T FORGET TO UPDATE VERSION IN PROJECT GRADLE

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation("io.realm.kotlin:library-sync:1.13.0") // DON'T FORGET TO UPDATE VERSION IN PROJECT GRADLE
    implementation("io.realm.kotlin:gradle-plugin:1.13.0")

    implementation ("androidx.core:core-ktx:1.12.0")
    implementation ("androidx.compose.ui:ui:1.6.1")
    implementation ("androidx.compose.material:material:1.6.1")
    implementation ("com.google.android.material:material:1.11.0")
    implementation ("androidx.compose.ui:ui-tooling-preview:1.6.1")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation ("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.core:core-ktx:+")
    //implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation ("com.google.android.gms:play-services-auth:20.0.1")
    testImplementation ("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test.ext:junit:1.1.5")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation ("androidx.compose.ui:ui-test-junit4:1.6.1")
    debugImplementation ("androidx.compose.ui:ui-tooling:1.6.1")
    implementation ("com.google.accompanist:accompanist-pager:0.19.0")
}
