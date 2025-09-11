// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.kover)
}

// Configure Kover aggregated reports and basic verification.
// You can tighten thresholds as coverage grows.
koverReport {
    defaults {
        xml { onCheck.set(false) }
        html { onCheck.set(false) }
    }
}
