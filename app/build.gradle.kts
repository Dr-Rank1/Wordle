plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

fun loadLocalProperties(): Map<String, String> {
    val file = rootProject.file("local.properties")
    if (!file.exists()) return emptyMap()
    return file.readLines()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") && it.contains("=") }
        .associate { line ->
            val idx = line.indexOf('=')
            line.substring(0, idx).trim() to line.substring(idx + 1).trim()
        }
}

val localProperties = loadLocalProperties()

android {
    compileSdk = 36
    namespace = "com.rank.lexi"

    defaultConfig {
        applicationId = "com.rank.lexi"
        minSdk = 24
        targetSdk = 36
        versionCode = 4
        versionName = "1.0.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val releaseStorePath = localProperties["RELEASE_STORE_FILE"]
    val releaseStore = if (releaseStorePath != null) {
        rootProject.file(releaseStorePath)
    } else {
        rootProject.file("release.keystore")
    }

    signingConfigs {
        if (releaseStore.exists()) {
            create("release") {
                storeFile = releaseStore
                storePassword = localProperties["RELEASE_STORE_PASSWORD"] ?: "lexiguess2026"
                keyAlias = localProperties["RELEASE_KEY_ALIAS"] ?: "lexiguess"
                keyPassword = localProperties["RELEASE_KEY_PASSWORD"] ?: "lexiguess2026"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfigs.findByName("release")?.let { signingConfig = it }
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    // composeOptions block intentionally omitted:
    // org.jetbrains.kotlin.plugin.compose (Kotlin 2.0) manages the Compose
    // compiler automatically — no kotlinCompilerExtensionVersion needed.

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    packaging {
        resources {
            excludes += setOf("META-INF/AL2.0", "META-INF/LGPL2.1")
        }
    }
}

dependencies {
    // Compose BOM — aligns ALL androidx.compose.* versions automatically.
    // Using 2024.06.00 (Compose UI 1.6.8, Material3 1.2.1), compatible with
    // Kotlin 2.0 + org.jetbrains.kotlin.plugin.compose.
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Core & Lifecycle
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
    implementation("androidx.activity:activity-compose:1.9.0")

    // Jetpack Compose — versions managed by BOM
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.animation:animation")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-android-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Networking — Retrofit + OkHttp (scalars converter for plain-text word list)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    // Start.io (formerly StartApp) — secondary ad network
    // Pinned to 5.2.6: 5.3.x requires compileSdk 37 which AGP 8.5 does not support yet.
    implementation("com.startapp:inapp-sdk:5.2.6")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
