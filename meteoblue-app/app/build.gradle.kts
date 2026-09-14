plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "it.meteoapp.clone"
    compileSdk = 35

    defaultConfig {
        applicationId = "it.meteoapp.clone"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        // API key MeteoBlue — letta da env (CI: GitHub Secrets), fallback per build locali
        buildConfigField(
            "String", "METEOBLUE_API_KEY",
            "\"${System.getenv("METEOBLUE_API_KEY") ?: "DEMOKEY"}\""
        )
        buildConfigField("String", "METEOBLUE_BASE_URL", "\"https://my.meteoblue.com/\"")
        // API key Google Weather (Google Maps Platform) — letta da env (CI: GitHub Secrets)
        buildConfigField(
            "String", "GOOGLE_WEATHER_API_KEY",
            "\"${System.getenv("GOOGLE_WEATHER_API_KEY") ?: "YOUR_GOOGLE_MAPS_API_KEY"}\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Compose BOM
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.foundation)
    implementation(libs.compose.animation)
    debugImplementation(libs.compose.ui.tooling)

    // Activity
    implementation(libs.activity.compose)

    // Navigation
    implementation(libs.navigation.compose)
    implementation(libs.hilt.navigation.compose)

    // Lifecycle
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Coroutines
    implementation(libs.coroutines.android)
    implementation(libs.coroutines.play.services)

    // Location
    implementation(libs.play.services.location)

    // Coil
    implementation(libs.coil.compose)

    // OSMDroid
    implementation(libs.osmdroid)

    // Vico charts
    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m3)
    implementation(libs.vico.core)

    // DataStore
    implementation(libs.datastore.preferences)

    // Splash screen
    implementation(libs.splashscreen)
}
