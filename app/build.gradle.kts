import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
            // Swift implements the Analytics interface: export it with its plain name.
            export(project(":analytics"))
        }
    }

    android {
        namespace = "com.majidbahmani.lisbonav.app"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            // Screens take a ViewModel parameter: callers need the ViewModel type.
            implementation(libs.androidx.lifecycle.viewmodelCompose)

            // The app shell: theme, shared non-UI code and the features it puts together.
            implementation(project(":systemdesign"))
            implementation(project(":core"))
            implementation(project(":feature:map"))
            implementation(project(":feature:transport-card"))
            implementation(project(":feature:consent"))
            implementation(libs.navigation.compose) // NavHost + bottom bar

            // DI: `api` because initKoin() exposes Koin types to the apps
            api(libs.koin.core)
            implementation(libs.koin.compose) // koinInject() for Analytics in App()
            // `api`: initKoin() takes an Analytics, which the platform apps implement.
            api(project(":analytics"))
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}

compose.resources {
    packageOfResClass = "com.majidbahmani.lisbonav.resources"
}
