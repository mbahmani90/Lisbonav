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
