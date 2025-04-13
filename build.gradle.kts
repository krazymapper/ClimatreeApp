plugins {
    id("com.android.application") version "8.9.1" apply false // Dernière version stable au 13 avril 2025
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false // Dernière version stable au 13 avril 2025
    id("com.google.gms.google-services") version "4.4.0" apply false
}

buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:8.9.1") // Correspond à la version du plugin Android
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22") // Correspond à la version du plugin Kotlin
        classpath("com.google.gms:google-services:4.4.0")
    }
}