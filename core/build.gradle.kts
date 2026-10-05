import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Shared non-UI code for all features: the HTTP client and its platform engines.
// No Compose, no business logic, no routes (see the module diagram in the README).
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinSerialization) // @Serializable test models
}

kotlin {
    iosArm64()
    iosSimulatorArm64()

    android {
        namespace = "com.majidbahmani.lisbonav.core"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            // `api`: createHttpClient() returns HttpClient and the Koin modules are public types.
            api(libs.ktor.client.core)
            api(libs.koin.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.serialization.kotlinxJson)
            implementation(libs.kotlinx.serialization.json)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }
    }
}
