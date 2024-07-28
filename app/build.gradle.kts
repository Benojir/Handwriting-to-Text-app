plugins {
    alias(libs.plugins.android.application)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.venomdino.aitools1"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.venomdino.aitools1"
        minSdk = 26
        //noinspection OldTargetApi
        targetSdk = 34
        versionCode = 3
        versionName = "1.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // add the dependency for the Google AI client SDK for Android
    //noinspection UseTomlInstead
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    // Required for one-shot operations (to use `ListenableFuture` from Guava Android)
    //noinspection UseTomlInstead
    implementation("com.google.guava:guava:32.1.3-android")
    // Required for streaming operations (to use `Publisher` from Reactive Streams)
    //noinspection UseTomlInstead
    implementation("org.reactivestreams:reactive-streams:1.0.4")

    //noinspection UseTomlInstead
    implementation("com.vanniktech:android-image-cropper:4.5.0")
}