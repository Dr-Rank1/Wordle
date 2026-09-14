pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        id("com.android.application") version "8.5.0"
        kotlin("android") version "2.0.0"
        id("com.google.dagger.hilt.android") version "2.51.1"
        id("com.google.devtools.ksp") version "2.0.0-1.0.22"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "LexiGuess"
include(":app")
