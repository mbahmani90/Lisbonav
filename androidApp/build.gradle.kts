import com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.googleServices)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}
// Google Maps key: local.properties (gitignored) or a CI environment variable.
// Blank when missing, so a fresh clone still builds; the map tiles then stay grey.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
val mapsApiKey: String = localProperties.getProperty("MAPS_API_KEY")
    ?: System.getenv("MAPS_API_KEY")
    ?: ""

// Firebase: google-services.json (gitignored, from the Firebase console) next to this file.
// Missing → warning instead of a failed build, so a fresh clone still builds; the app then logs
// nothing (LisbonavApp falls back to NoOpAnalytics).
googleServices {
    missingGoogleServicesStrategy = MissingGoogleServicesStrategy.WARN
}

// Version from the release tag (doc 31): `-Plisbonav.versionName=1.2.3 -Plisbonav.versionCode=10203`.
// Local builds without them are 1.0 / 1.
val releaseVersionName = findProperty("lisbonav.versionName") as String?
val releaseVersionCode = (findProperty("lisbonav.versionCode") as String?)?.toInt()

// Release signing (the Play upload key) only from environment variables, never from files in the
// repo. Without LISBONAV_KEYSTORE_FILE the release build is unsigned, so no one needs the key to
// build locally.
val releaseKeystore: String? = System.getenv("LISBONAV_KEYSTORE_FILE")?.takeIf { it.isNotBlank() }

dependencies {
    implementation(project(":app"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android)

    // The BoM picks matching Firebase versions: no version on the artifacts.
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "com.majidbahmani.lisbonav"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.majidbahmani.lisbonav"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = releaseVersionCode ?: 1
        versionName = releaseVersionName ?: "1.0"
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        if (releaseKeystore != null) {
            create("release") {
                storeFile = file(releaseKeystore)
                storePassword = System.getenv("LISBONAV_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("LISBONAV_KEY_ALIAS")
                keyPassword = System.getenv("LISBONAV_KEY_PASSWORD")
            }
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}
