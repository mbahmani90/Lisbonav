import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Feature: the Navegante transport card (tap to read passes and trips). Placeholder screen for now;
// card reading comes from :calypso-nfc later. Never depends on another feature.
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    iosArm64()
    iosSimulatorArm64()

    android {
        namespace = "com.majidbahmani.lisbonav.feature.transportcard"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
        androidResources {
            enable = true // Compose resources (strings)
        }
        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.navigation.compose) // TransportCardRoute + transportCardScreen() for the app's NavHost
            implementation(libs.kotlinx.serialization.json) // @Serializable route

            implementation(project(":calypso-nfc")) // card reading + Lisbon parser (data layer only)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime) // dates in the domain model
            implementation(libs.koin.core)
        }
        androidMain.dependencies {
            implementation(libs.koin.android) // androidApplication() for the NFC tag reader
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

compose.resources {
    packageOfResClass = "com.majidbahmani.lisbonav.feature.transportcard.resources"
}
