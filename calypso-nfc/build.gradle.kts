import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Calypso contactless card reader SDK: NFC transport, ISO 7816-4 APDUs and Calypso file reading.
// No UI, no DI, no app types: any app can use it.
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
}

kotlin {
    iosArm64()
    iosSimulatorArm64()

    android {
        namespace = "com.majidbahmani.calypso.nfc"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.datetime) // LocalDate / LocalDateTime are part of the public model
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
