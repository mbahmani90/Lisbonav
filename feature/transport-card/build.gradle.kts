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
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.navigation.compose) // TransportCardRoute + cardScreen() for the app's NavHost
            implementation(libs.kotlinx.serialization.json) // @Serializable route
        }
    }
}

compose.resources {
    packageOfResClass = "com.majidbahmani.lisbonav.feature.transportcard.resources"
}
