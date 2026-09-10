# espeyu

An Android mirror app: full-screen front camera, unmirrored by default, so you
see yourself the way other people do. Public repo, read as a work sample, so the
bar is the smallest change a senior reviewer would call obvious. README.md has
the architecture and the reasoning behind it; do not duplicate that here.

## Commands

```sh
./gradlew spotlessApply # ktfmt kotlinlang style, writes fixes
./gradlew lint          # Android Lint, warningsAsErrors
./gradlew test          # every test, all on the JVM
./gradlew recordRoborazziDebug  # rewrite the golden screenshots
./gradlew assembleDebug
./gradlew bundleRelease     # the bundle Play receives: R8, resource shrinking, lintVital
```

Run `spotlessApply`, `lint` and `test` before calling a change done. CI runs
`spotlessCheck`, `lint`, `test`, `bundleRelease`, and `reuse lint` in a
separate job.

## Gotchas

- **`PreviewView.ImplementationMode.COMPATIBLE` is not a preference.**
  `PERFORMANCE` uses a `SurfaceView`, and CameraX documents two limits that both
  land here: the mode is not for a `PreviewView` that "needs to be animated", and
  its `STREAMING` state "might happen prematurely". The mirror flip and the ring
  light's rounded window are `graphicsLayer` transforms on that view, and freeze
  reads the frame back the moment `STREAMING` arrives.
- **The controls bar is a floating toolbar built by hand.**
  `HorizontalFloatingToolbar` needs material3 1.5.0-alpha and is experimental, so
  `MirrorControls` matches `FloatingToolbarTokens` from 1.4.0 instead. The bar
  takes its width from the button row through `IntrinsicSize.Min`. `CircleShape`
  looks right on the one-row bar and turns into an ellipse the moment a slider
  stacks on top, which is why the shape is `shapes.extraLarge`.
- **The front camera preview arrives mirrored and the back camera does not.**
  `isViewfinderFlipped` is the only place that knows this. Anything that changes
  how the viewfinder is transformed goes through it, and `ViewfinderFlipTest`
  covers all four combinations.
- **Dependencies can add permissions.** CameraX pulls in `media3-common`, which
  declares `ACCESS_NETWORK_STATE`; the manifest removes it with
  `tools:node="remove"`. `ManifestPermissionsTest` reads the merged manifest and
  fails if anything else appears. Never silence that test; remove the permission
  or drop the dependency.
- **Never set `Preview.targetRotation`.** `PreviewView` overrides the transform with the live
  display rotation only while the use case has none, which is what lets `MainActivity` handle
  `orientation` itself and keep the camera bound across a rotation.
- **`kotlin { compilerOptions { allWarningsAsErrors } }` is on** in both modules.
  A deprecated Compose overload fails the build rather than warning.
- **Kotlin needs no plugin.** AGP 9 compiles Kotlin itself, so
  `org.jetbrains.kotlin.android` is absent on purpose. Only the Compose compiler
  plugin is applied.
- **`androidx.baselineprofile` is pinned to a release candidate.** 1.4.x does not
  understand AGP 9's DSL and fails with "Module `:app` is not a supported android
  module". Move it back to stable once 1.5.0 ships.
- **Robolectric is pinned to `sdk=36`** in
  `app/src/test/resources/robolectric.properties`, one below `targetSdk`, which
  is as far as 4.16.1 goes.
- **`Dispatchers.setMain` does not take effect under Robolectric**, so
  `MirrorViewModelTest` runs without it, against a real preferences store on a
  temporary file, and waits for results instead of advancing virtual time. Adding
  `@RunWith(RobolectricTestRunner::class)` to it would make it hang, not fail.
- **`versionCode` and `versionName` have to stay literals.** F-Droid parses them
  out of the tagged revision with a regex and resolves nothing, so deriving
  either from the other in Gradle would stop the updater seeing new releases.
- **`ScreenshotTest` compares against golden images** in
  `app/src/test/screenshots/`, so any change to how a screen looks fails it with
  a bare `AssertionError`. `app/build/outputs/roborazzi/` holds the diff. Record
  again once the new look is the intended one, never to clear the failure.
- **Compose tests use `androidx.compose.ui.test.junit4.v2.createComposeRule`.**
  The v1 rule is deprecated, and with the v2 rule a state change made from test
  code needs a `waitForIdle()` before the effect keyed on it restarts.
  `ControlsVisibilityTest` also turns `mainClock.autoAdvance` off, because idling
  would otherwise run the auto-hide countdown out before the test can interrupt
  it.

## Conventions

- Camera and window side effects live in composables keyed on what they depend
  on, never in `MainActivity`, so they unwind on their own. `MainActivity` only
  sets up the window itself.
- `MirrorViewModel` owns everything persisted plus everything that survives a
  rotation. Purely ephemeral UI state, which slider panel is open and whether the
  controls are on screen, stays in the composables.
- Settings persist through one debounced collector in `MirrorViewModel.init`, not
  through each setter. Adding a setting means adding a field and a key, nothing
  else.
- Screens paint their own background with a `Surface` rather than a bare
  `Column`, which is what puts the theme's content colour on their text.
- Icons are Material Symbols converted to vector drawables and committed, rather
  than `material-icons-extended`. Add new ones the same way and keep their
  Apache-2.0 header.
- Every file carries SPDX headers, or is annotated in `REUSE.toml`. `reuse lint`
  has to pass.
- The non-goals are binding: no photo capture, no video, no saving, no sharing,
  no filters, no accounts, no ads, no analytics. A feature that would need the
  `INTERNET` permission is a no.
- `main` takes pull requests only. Branch, open a PR, let CI go green, merge.

## Writing style

The repo is a work sample, so anything that reads as machine-generated is a
defect. These rules apply to code, comments, tests, docs and commit messages.

- **No em dashes.** Use a comma, a colon, or two sentences.
- Comments explain **why**, never what. If the code already says it, delete the
  comment rather than rewording it.
- State a fact in one place only and cross-reference it.
- No comments that narrate history, argue a case to a reviewer, or park a
  roadmap note.
- Test names state behaviour plainly, with no "since ..." or "so that ..."
  justification clauses.
- **Never invent specifics in documentation.** Verify every number, path, file
  name and API name against the code before writing it down.

## Commits

Conventional Commits, enforced by commitlint in CI.

- The type must be `feat`, `fix`, `revert` or `chore`. **`docs`, `refactor`,
  `test`, `style`, `perf` and `ci` are rejected**, so use `chore` for those.
- Lowercase type, scope and subject. No trailing period.
- New code should try follow same conventions and style of existing code.
