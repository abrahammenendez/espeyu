# espeyu

:bulb: A fast, modern, privacy-first, feature-rich mirror for your phone.

[![Main](https://github.com/abrahammenendez/espeyu/actions/workflows/main.yaml/badge.svg)](https://github.com/abrahammenendez/espeyu/actions/workflows/main.yaml)
[![REUSE status](https://api.reuse.software/badge/github.com/abrahammenendez/espeyu)](https://api.reuse.software/info/github.com/abrahammenendez/espeyu)

You open it and you see yourself, full screen, straight away.

Every phone mirrors its front camera preview, and no setting turns that off.
The "mirror front camera" option both Android and iOS expose only decides how a
saved photo is written; the live view stays flipped. So you can never see
yourself live the way other people see you. Espeyu shows you the true view by
default and keeps the mirrored one a tap away.

Espeyu takes no photos, records no video, writes nothing to disk beyond your own
preferences, and has no `INTERNET` permission. It is distributed through
F-Droid only.

## Getting started

Requires the Android SDK.
[`gradle/gradle-daemon-jvm.properties`](gradle/gradle-daemon-jvm.properties) pins
the build to JDK 25, which Gradle provisions itself when the machine has none.

```sh
./gradlew spotlessApply # ktfmt, writes fixes
./gradlew lint          # Android Lint, warnings are errors
./gradlew test          # JVM unit tests, including the Compose ones
./gradlew assembleDebug
```

Run those before calling a change done. CI runs the same set, with
`spotlessCheck` in place of `spotlessApply`, plus `assembleRelease` and a REUSE
licence check.

The SDK components are pinned in [`app/build.gradle.kts`](app/build.gradle.kts)
and the Gradle and dependency versions in
[`gradle/libs.versions.toml`](gradle/libs.versions.toml).

## How it works

One activity, one screen, no dependency injection and no navigation library.
State lives in `MirrorViewModel` as a single immutable `MirrorUiState`; the
persisted slice of it round-trips through DataStore.

### The two views

`PreviewView` mirrors the front camera and leaves the back camera alone, so
"true view" and "mirrored view" are not a fixed transform. `isViewfinderFlipped`
in [`MirrorViewfinder.kt`](app/src/main/kotlin/com/abrahammenendez/espeyu/ui/mirror/MirrorViewfinder.kt)
decides when the viewfinder disagrees with the preview's own default, and the
screen sets `scaleX` to `1f` or `-1f` accordingly. The flip is a
`graphicsLayer` transform, so it costs nothing per frame and never rebinds the
camera.

It is not animated. Sweeping `scaleX` from one to the other passes through
zero, which squashes the image flat on the way and reads as a glitch rather
than as a mirror turning round.

`Preview.Builder.setMirrorMode` looks like the direct route and is not one. Its
own documentation says it does nothing below API 33, and espeyu supports API 26.
It is experimental and opt-in besides, and being a builder option, every toggle
would rebuild the use case and rebind the camera.

`androidx.camera:camera-compose` would replace the `AndroidView` holding the
`PreviewView`. Freeze is what stops it: `CameraXViewfinder` is the whole of that
artifact's API, and it cannot read the displayed frame back.

### The controls

One floating toolbar, matched by hand to `FloatingToolbarTokens` in Material 3
1.4.0: 64dp tall, 8dp of padding, 4dp between buttons and 16dp off the screen
edge. `HorizontalFloatingToolbar` is the component for this and it is not
usable here, since it lands in material3 1.5.0-alpha and is still opt-in
experimental, while the tokens it would apply ship in the stable release. The
bar takes its width from the button row through `IntrinsicSize.Min`, so a
slider opening above the buttons is exactly as wide as they are and the bar
never carries dead space.

### Freeze

Freezing reads the displayed frame back with `PreviewView.getBitmap()`, which
captures after the preview's own rotation and mirroring, and then unbinds the
camera. The sensor genuinely stops rather than the pixels merely holding still.
The button stays disabled until the preview reports `STREAMING`, because before
that the read back returns null or an empty bitmap.

The alternative, binding an `ImageCapture` use case and taking a real photo,
costs shutter latency, so the frozen image would not be the frame you were
looking at, and a real capture is subject to mandatory regional shutter sounds.

### Ring light

Light cast by the screen itself, not the camera torch, which points the wrong
way. The viewfinder gives up a 48dp border and the exposed frame becomes
the lamp, its corners rounded so it reads as a window rather than as a crop. A
gradient veiling the reflection was the other option and it is the worse one:
this way the whole of you stays visible, only smaller, and the lit area is at
full strength rather than fading out across the frame.

Auto white balance is left on. Pinning it to `CONTROL_AWB_MODE_DAYLIGHT` puts
the light's colour on your face rather than only on the frame, and turns a
warm-lit room entirely orange, because the camera then stops correcting the room
either. Screen brightness is left alone for the same reason, and its own control
sits in the same toolbar.

The warmth slider interpolates in mireds rather than kelvin, so the middle of
the slider looks like the middle of the range, and the colour comes from Tanner
Helland's approximation of the Planckian locus rather than from hand-picked hex
values. See
[`ColorTemperature.kt`](app/src/main/kotlin/com/abrahammenendez/espeyu/ui/mirror/ColorTemperature.kt).

### Screen brightness

`WindowManager.LayoutParams.screenBrightness` is a per-window override. It does
not touch the system setting, and Android drops it when the window stops being
the one on screen.

### About

The mark in the corner opens the about dialog. It is the launcher glyph cropped
to its ink, because an adaptive icon carries 36dp of padding on each side that a
mark this small cannot afford. It draws twice, a dark copy a pixel below a pale
one, since the preview behind it can be any colour: `ScreenshotTest` captures it
over white and black, which is the only way to catch a mark that has gone
invisible. It sits inside the ring light's window rather than over the lamp.

## Privacy

`CAMERA` is the only permission the app asks the platform for, and
`ManifestPermissionsTest` asserts that against the merged manifest rather than
against the source. The one entry it filters out is the signature permission
`androidx.core` defines on the app itself to keep its own receivers unexported. That test earns its keep: CameraX pulls in `camera-video`
and through it `media3-common`, which declares `ACCESS_NETWORK_STATE`. The
manifest removes it.

Nothing else is written to disk. There is no analytics, no crash reporting, no
Play Services and no network code, and without `INTERNET` there could not be.

## Testing

Everything runs on the JVM. Compose UI tests and anything needing a `Context`
run under Robolectric, so there is no instrumented test suite and no emulator in
CI.

`ScreenshotTest` captures what is drawn, which a semantics assertion cannot
check: text rendered black on a black window still reports itself as displayed.
The golden images live in `app/src/test/screenshots/` and `./gradlew test`
compares against them. After an intended change to the way a screen looks,
re-record them:

```sh
./gradlew recordRoborazziDebug
```

Skia rasterises differently across platforms, so a golden recorded on one
machine can fail on another. There is no comparison threshold on purpose: a
tolerance loose enough to absorb that is loose enough to hide a real change. If
CI disagrees with a golden recorded locally, take the images CI uploads as the
new goldens rather than widening anything.

`benchmark/` is a Macrobenchmark module and needs a real device:

```sh
./gradlew :benchmark:connectedBenchmarkReleaseAndroidTest        # startup timing
./gradlew :app:generateReleaseBaselineProfile                    # baseline profile
```

The app calls `ReportDrawnWhen` the moment the first camera frame reaches the
screen, so `StartupTimingMetric`'s time to full display is cold start to first
frame, which is the number that makes "fast" true.

## Releasing

F-Droid builds and signs from a git tag, which is why no signing key and no APK
live here.

1. Raise `versionCode` and `versionName` in
   [`app/build.gradle.kts`](app/build.gradle.kts).
2. Add `fastlane/metadata/android/en-US/changelogs/<versionCode>.txt`, which is
   what the F-Droid listing shows as the release notes.
3. Tag the commit `v<versionName>` and push it. The release workflow refuses a
   tag that disagrees with `versionName`, or that arrives without the changelog
   for its `versionCode`, and otherwise publishes the GitHub release.

F-Droid reads the listing text and images from `fastlane/metadata/`. See
[`docs/f-droid.md`](docs/f-droid.md) for the submission itself.

## Licence

AGPL-3.0-or-later, and [REUSE](https://reuse.software) compliant. The icons
under `app/src/main/res/drawable/` are Material Symbols, Apache-2.0.
