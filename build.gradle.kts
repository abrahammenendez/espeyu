// SPDX-FileCopyrightText: 2026 Abraham Menéndez
// SPDX-License-Identifier: AGPL-3.0-or-later

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.baselineprofile) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.roborazzi) apply false
    alias(libs.plugins.spotless)
}

// The targets are anchored rather than "**/*.kt", which would walk every module's build
// directory and race the Android resource tasks a parallel build runs alongside Spotless.
spotless {
    kotlin {
        target("*/src/**/*.kt")
        ktfmt(libs.versions.ktfmt.get()).kotlinlangStyle()
        licenseHeaderFile(rootProject.file("gradle/licence-header.txt"))
    }
    kotlinGradle {
        target("*.gradle.kts", "*/*.gradle.kts")
        ktfmt(libs.versions.ktfmt.get()).kotlinlangStyle()
    }
}
