pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "org.jetbrains.kotlin.plugin.serialization") {
                // Supprimer ou commenter la ligne pour utiliser la version spécifiée dans le build.gradle
                // useVersion("1.9.23")
            }
            if (requested.id.id == "com.google.devtools.ksp") {
                // Supprimer ou commenter la ligne pour utiliser la version spécifiée dans le build.gradle
                // useVersion("1.9.23-1.0.18")
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "Climatport"
include(":app")