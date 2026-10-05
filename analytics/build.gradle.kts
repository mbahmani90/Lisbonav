import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Analytics API for shared code: an interface, its events and a no-op. No other module, no
// Firebase: the platform apps implement Analytics (Kotlin on Android, Swift on iOS) and pass it
// to initKoin (see the module diagram in the README).
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
}

kotlin {
    iosArm64()
    iosSimulatorArm64()

    android {
        namespace = "com.majidbahmani.lisbonav.analytics"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
        withHostTest {}
    }

    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
