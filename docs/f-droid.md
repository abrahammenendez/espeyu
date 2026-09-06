<!--
SPDX-FileCopyrightText: 2026 Abraham Menéndez
SPDX-License-Identifier: AGPL-3.0-or-later
-->

# Publishing on F-Droid

F-Droid is the only distribution channel. There is no Play Console, no App
Bundle and no signing key in this repository: F-Droid builds from source and
signs with its own key.

## Submitting

1. Push a tag, as described in the README. F-Droid's updater only picks up
   tagged commits, and takes the highest `versionCode` it finds among them.
2. Fork [fdroiddata](https://gitlab.com/fdroid/fdroiddata) and add
   `metadata/com.abrahammenendez.espeyu.yml`. Tags here carry a `v`, so
   `AutoUpdateMode` has to be `Version v%v` or the updater matches nothing.
3. Open a merge request against `fdroiddata` and answer the new-app template.
   It asks whether upstream ships Fastlane metadata; this repository does, at
   `fastlane/metadata/android/en-US/`.

## What the reviewers look at

- **Licensing.** AGPL-3.0-or-later, with SPDX headers on every file and a
  `REUSE.toml` covering the rest. `reuse lint` runs in CI.
- **Permissions.** `CAMERA` only. The listing shows the permission list, which
  is why `ManifestPermissionsTest` guards it.
- **Dependencies.** Everything resolves from Google's Maven repository and
  Maven Central. No Play Services, no Firebase, no proprietary SDKs.
- **Build tooling.** Confirm at submission time that the buildserver carries a
  Gradle new enough for the Android Gradle plugin pinned in
  `gradle/libs.versions.toml`, and a JDK matching
  `gradle/gradle-daemon-jvm.properties`. Gradle downloads that JDK when it
  cannot find one, which a buildserver should not be relied on to allow. The pin
  is a preference rather than a requirement, and the build passes on 21, so
  lowering it is the fix if their image is older. Whether it needs lowering is
  the one thing about the submission that cannot be checked from here.

## Screenshots

`fastlane/metadata/android/en-US/images/` holds the launcher icon. Screenshots
belong in `images/phoneScreenshots/`, numbered `1.png` upwards, and have to be
taken on a real device: a mirror app screenshotted on an emulator shows an
emulator's test pattern.

## Reproducibility

F-Droid's reproducible-build verification compares a developer-signed APK
against its own rebuild. Espeyu ships no developer-signed APK, so that check
does not apply. What the build does do is stay deterministic: every version is
pinned in the version catalog, the Gradle wrapper carries a distribution
checksum, and `dependenciesInfo` is off so no Play metadata blob is embedded.
