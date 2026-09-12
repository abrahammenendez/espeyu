// SPDX-FileCopyrightText: 2026 Abraham Menéndez
// SPDX-License-Identifier: AGPL-3.0-or-later

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.baselineprofile)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.play.publisher)
    alias(libs.plugins.roborazzi)
}

// semantic-release passes the version it has just tagged. Anything else is not a release,
// and falls back to the lowest version there is.
val releaseVersion = providers.gradleProperty("releaseVersion").getOrElse("0.0.1")

val (major, minor, patch) = releaseVersion.split(".").map(String::toInt)

val uploadKeystore = providers.environmentVariable("UPLOAD_KEYSTORE")
val uploadKeystorePassword = providers.environmentVariable("UPLOAD_KEYSTORE_PASSWORD")

android {
    namespace = "com.abrahammenendez.espeyu"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.abrahammenendez.espeyu"
        minSdk = 26
        targetSdk = 37
        versionCode = major * 10000 + minor * 100 + patch
        versionName = releaseVersion
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        // The release workflow is the only place that holds the upload key. Everywhere else
        // a release build comes out unsigned, which is as far as it should get.
        uploadKeystore.orNull?.let { keystore ->
            create("upload") {
                storeFile = file(keystore)
                storePassword = uploadKeystorePassword.get()
                keyAlias = "espeyu-upload"
                // PKCS12 holds one password, for the store and the key alike.
                keyPassword = uploadKeystorePassword.get()
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("upload")

            optimization {
                enable = true
            }
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Play-only metadata that would otherwise be an opaque blob in the APK.
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    lint {
        warningsAsErrors = true
        checkDependencies = true
        // Reported but never fatal: these fire the day something upstream publishes, so as
        // errors they break a build no one touched. Dependabot owns dependency freshness.
        informational +=
            listOf("AndroidGradlePluginVersion", "GradleDependency", "NewerVersionAvailable")
    }
}

// The robot account signs in through Workload Identity Federation, so no key file exists
// to leak. Every build lands on the internal track, and moving one on is deliberate. The
// listing goes up with it, which is what keeps Play from disagreeing with the repository.
play {
    useApplicationDefaultCredentials = true
    defaultToAppBundles = true
    track = "internal"
}

kotlin {
    compilerOptions {
        allWarningsAsErrors = true
    }
}

dependencies {
    implementation(platform(libs.compose.bom))

    baselineProfile(project(":benchmark"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.androidx.lifecycle.runtime.testing)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.compose.ui.test.junit4)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.junit.rule)
}
