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
        xml { onCheck = false }
        html { onCheck = false }
        // Exclude pure UI and DI wiring from coverage to focus on business logic
        filters {
            excludes {
                // App Compose screens and activity
                classes(
                    "com.ugo.mhews.mealmanage.MainActivity",
                    "com.ugo.mhews.mealmanage.ui.*ScreenKt",
                    "com.ugo.mhews.mealmanage.ui.theme.*Kt"
                )
                // DI modules (wiring only)
                classes(
                    "com.ugo.mhews.mealmanage.di.*",
                    "com.ugo.mhews.mealmanage.data.di.*"
                )
                // Generated Hilt classes
                packages("hilt_aggregated_deps", "dagger.hilt.internal.generated")
            }
        }
        verify {
            rule("LineCoverage") {
                bound {
                    minValue = 100
                }
            }
        }
    }
}

// Ensure coverage XML report runs after tests and disable HTML reports to avoid FreeMarker issues
tasks.matching { it.name == "koverHtmlReport" }.configureEach { enabled = false }
subprojects {
    afterEvaluate {
        tasks.matching { it.name == "koverHtmlReport" }.configureEach { enabled = false }
    }
}
tasks.named("koverXmlReport").configure {
    dependsOn(":domain:test", ":core:test", ":data:testDebugUnitTest", ":app:testDebugUnitTest")
}
