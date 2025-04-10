plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id("com.google.ar.sceneform.plugin")
}

android {
    namespace = "com.example.touuri"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.touuri"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    
    // OpenStreetMap
    implementation("org.osmdroid:osmdroid-android:6.1.17")
    
    // Location Services
    implementation("com.google.android.gms:play-services-location:21.1.0")
    
    // CSV Handling
    implementation("com.opencsv:opencsv:5.9")
    
    // OAuth for OpenStreetMap
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.2"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    
    // ML Kit pour la reconnaissance d'images
    implementation("com.google.mlkit:image-labeling:17.0.7")
    implementation("com.google.mlkit:image-labeling-custom:17.0.1")
    
    // CameraX
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
    
    // Coil pour le chargement d'images
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // ARCore
    implementation("com.google.ar:core:1.40.0")
    implementation("com.google.ar.sceneform:core:1.17.1")
    implementation("com.google.ar.sceneform:assets:1.17.1")
    implementation("com.google.ar.sceneform.ux:sceneform-ux:1.17.1")
    
    // 3D Models
    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation("com.google.android.filament:filament-android:1.9.25")
}