plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.googleServices) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.kover)
    alias(libs.plugins.sonarqube)
}

// Code style: ktlint (Android Studio style) + Compose rules. CI runs `spotlessCheck`;
// fix locally with `./gradlew spotlessApply`. Rules live here, not in .editorconfig: Spotless
// doesn't apply ktlint properties from .editorconfig reliably (doc 30).
val ktlintRules = mapOf(
    "ktlint_code_style" to "android_studio",
    "max_line_length" to 120,
    // Trailing commas, as written in the project (android_studio style forbids them by default):
    // adding a parameter or list item then changes one line, not two.
    "ij_kotlin_allow_trailing_comma" to true,
    "ij_kotlin_allow_trailing_comma_on_call_site" to true,
    // Signatures stay as written: short ones on one line, longer ones one parameter per line.
    // (android_studio style would join every signature that fits in 120 characters.)
    "ktlint_standard_function-signature" to "disabled",
    "ktlint_standard_class-signature" to "disabled",
    // Composables are PascalCase functions.
    "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
)

spotless {
    kotlin {
        target("**/src/**/*.kt")
        targetExclude("**/build/**")
        ktlint(libs.versions.ktlint.get())
            .editorConfigOverride(ktlintRules)
            .customRuleSets(listOf("io.nlopez.compose.rules:ktlint:${libs.versions.composeRules.get()}"))
    }
    kotlinGradle {
        target("*.gradle.kts", "**/*.gradle.kts")
        targetExclude("**/build/**")
        ktlint(libs.versions.ktlint.get()).editorConfigOverride(ktlintRules)
    }
}

// Coverage: Kover measures the JVM test runs (commonTest + androidHostTest) of every module and merges
// them into one report: `./gradlew koverXmlReport` (XML for SonarQube) or `koverHtmlReport` (to browse).
// iOS test runs (Kotlin/Native) can't be measured.
subprojects {
    apply(plugin = "org.jetbrains.kotlinx.kover")

    // The Sonar plugin doesn't find Kotlin Multiplatform source sets: list them (src/<name>Main, src/<name>Test).
    pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
        val sourceSetDirs = file("src").listFiles().orEmpty().filter { it.isDirectory }
        sonar {
            properties {
                property("sonar.sources", sourceSetDirs.filter { it.name.endsWith("Main") })
                property("sonar.tests", sourceSetDirs.filter { it.name.endsWith("Test") })
            }
        }
    }
}

dependencies {
    subprojects.forEach { kover(it) }
}

// Static analysis on SonarQube Cloud. CI runs `koverXmlReport sonar` with SONAR_TOKEN on pushes to main and PRs.
sonar {
    properties {
        property("sonar.projectKey", "mbahmani90_Lisbonav")
        property("sonar.organization", "mbahmani90")
        property("sonar.host.url", "https://sonarcloud.io")
        // Waits for the quality gate and fails the task (and CI) when it fails.
        property("sonar.qualitygate.wait", true)
        // Inherited by every module, so an absolute path to the merged report.
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            layout.buildDirectory.file("reports/kover/report.xml").get().asFile.absolutePath,
        )
        // iosMain is still checked for issues, but left out of coverage: Kover can't measure iOS tests,
        // so it would always count as 0%.
        property("sonar.coverage.exclusions", "**/src/iosMain/**")
    }
}
