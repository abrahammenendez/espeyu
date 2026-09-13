# espeyu

:bulb: A modern, privacy-first, fast mirror for your phone. Free, open-source, no ads.

[![Release](https://github.com/abrahammenendez/espeyu/actions/workflows/release.yaml/badge.svg)](https://github.com/abrahammenendez/espeyu/actions/workflows/release.yaml)
[![REUSE status](https://api.reuse.software/badge/github.com/abrahammenendez/espeyu)](https://api.reuse.software/info/github.com/abrahammenendez/espeyu)

Your front camera shows you a mirrored view. That's not what other people see when they look at you.
Espeyu shows you the true view instead, the one other people see. You can still switch to the
mirrored view any time.

In addition to true view by default, it also has freeze, zoom, screen brightness and an on-screen
ring light.

Espeyu takes no photos, records no video, writes nothing to disk beyond your own
preferences, and has no `INTERNET` permission. It's free, open source and
distributed through Google Play, with no ads and no in-app purchases.

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
`spotlessCheck` in place of `spotlessApply`, plus `bundleRelease` and a REUSE
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
edge. `HorizontalFloatingToolbar` is the component for this, and it is not
usable here, since it lands in material3 1.5.0-alpha and is still opt-in
experimental, while the tokens it would apply ship in the stable release. The
bar takes its width from the button row through `IntrinsicSize.Min`, so a
slider opening above the buttons is exactly as wide as they are and the bar
never carries dead space.

Each button plays a haptic when it acts, since your eyes are on your own face
and not on the bar. The toggles use `ToggleOn` and `ToggleOff`, the lens switch
uses `SegmentTick`, so the type matches the gesture rather than being one buzz
for everything.

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

The corner mark opens the about dialog and signs a screenshot, so it has to hold
up over a live preview of any colour without pulling the eye off the reflection.
It is a cropped copy of the launcher glyph, not the adaptive icon, which builds
in a margin a mark this small cannot spare. `ScreenshotTest` renders it over
white and black, since a mark that has washed out still passes every semantics
assertion.
It sits inside the ring light's window rather than over the lamp.

## Privacy

`CAMERA` is the only permission the app asks the platform for, and
`ManifestPermissionsTest` asserts that against the merged manifest rather than
against the source. The one entry it filters out is the signature permission
`androidx.core` defines on the app itself to keep its own receivers unexported. That test earns its keep: CameraX pulls in `camera-video`
and through it `media3-common`, which declares `ACCESS_NETWORK_STATE`. The
manifest removes it.

Nothing else is written to disk. There is no analytics, no crash reporting, no
Play Services and no network code, and without `INTERNET` there could not be.

The privacy policy itself is [`PRIVACY.md`](PRIVACY.md).

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

Merging a pull request to `main` is the release. semantic-release reads the
squash-merged titles and tags the version, and the same workflow signs the
bundle with the upload key and uploads it, with the store listing, to Play's
internal track. `feat` makes a minor release and the other three types a patch.

`versionName` is that version and `versionCode` follows from it, so neither is
written down anywhere. Moving a build to the closed test or to production is the
`Promote` workflow, run by hand.

The listing text and images live in [`app/src/main/play/`](app/src/main/play),
which is their only source: an edit made in the Play Console is overwritten by
the next release. [`docs/releases.md`](docs/releases.md) is the runbook, and
covers the robot account behind all this.

## Licence

AGPL-3.0-or-later, and [REUSE](https://reuse.software) compliant. The control
icons under `app/src/main/res/drawable/` are Material Symbols, Apache-2.0. The
mirror glyph in `ic_launcher_foreground.xml` and `ic_mark.xml` is Lucide's
[`mirror-round`](https://lucide.dev/icons/mirror-round), ISC. The bundled fonts
are [Inter](https://rsms.me/inter/) and
[Buenard](https://fonts.google.com/specimen/Buenard), both OFL-1.1.
