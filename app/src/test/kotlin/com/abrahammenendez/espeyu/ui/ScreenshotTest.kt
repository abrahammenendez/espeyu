// SPDX-FileCopyrightText: 2026 Abraham Menéndez
// SPDX-License-Identifier: AGPL-3.0-or-later

package com.abrahammenendez.espeyu.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.abrahammenendez.espeyu.data.MirrorSettings
import com.abrahammenendez.espeyu.data.RingLight
import com.abrahammenendez.espeyu.data.ScreenBrightness
import com.abrahammenendez.espeyu.ui.about.AboutDialog
import com.abrahammenendez.espeyu.ui.about.AboutMark
import com.abrahammenendez.espeyu.ui.mirror.MirrorControls
import com.abrahammenendez.espeyu.ui.mirror.MirrorUiState
import com.abrahammenendez.espeyu.ui.mirror.RingLightFrame
import com.abrahammenendez.espeyu.ui.permission.CameraPermissionGate
import com.abrahammenendez.espeyu.ui.theme.EspeyuTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val GOLDEN_DIR = "src/test/screenshots"

/** The mirror screen needs a camera to draw anything, which leaves these two surfaces. */
@RunWith(RobolectricTestRunner::class)
class ScreenshotTest {

    @get:Rule val compose = createComposeRule()

    @Test fun `the camera prompt`() = capture("permission-prompt") { CameraPermissionGate {} }

    /** A line of body text as wide as a landscape phone is one nobody can follow. */
    @Test
    @Config(qualifiers = "w800dp-h360dp-land")
    fun `the camera prompt in landscape`() =
        capture("permission-prompt-landscape") { CameraPermissionGate {} }

    @Test fun `the controls at rest`() = capture("controls") { Controls(MirrorUiState()) }

    @Test
    fun `the controls with the setting sliders showing`() =
        capture("controls-sliders") {
            Controls(
                MirrorUiState(
                    settings =
                        MirrorSettings(
                            brightness = ScreenBrightness(isOverridden = true, level = 0.4f),
                            ringLight = RingLight(isOn = true, warmth = 0.7f),
                        )
                )
            )
        }

    /** The lamp is the border the viewfinder gives up, so it has to read as a frame. */
    @Test
    @Config(qualifiers = "w360dp-h780dp")
    fun `the ring light framing the viewfinder`() =
        capture("ring-light") {
            RingLightFrame(RingLight(isOn = true, warmth = 1f), Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxSize().background(Color.DarkGray))
            }
        }

    /** A preview can be any colour, and only a golden shows the mark survives both ends. */
    @Test
    fun `the mark over a light and a dark preview`() =
        capture("mark") {
            Row {
                Box(Modifier.size(80.dp, 64.dp).background(Color.White)) { AboutMark({}) }
                Box(Modifier.size(80.dp, 64.dp).background(Color.Black)) { AboutMark({}) }
            }
        }

    /** The dialog is its own window, so it is matched rather than read off the root. */
    @Test
    fun `the about box`() {
        compose.setContent { EspeyuTheme { AboutDialog(onDismiss = {}) } }

        compose.onNode(isDialog()).captureRoboImage("$GOLDEN_DIR/about.png")
    }

    private fun capture(name: String, content: @Composable () -> Unit) {
        compose.setContent { EspeyuTheme(content) }
        compose.onRoot().captureRoboImage("$GOLDEN_DIR/$name.png")
    }
}

@Composable
private fun Controls(state: MirrorUiState) {
    MirrorControls(
        state = state,
        canFreeze = true,
        onToggleView = {},
        onSwitchLens = {},
        onToggleFreeze = {},
        onToggleBrightness = {},
        onBrightnessChange = {},
        onToggleRingLight = {},
        onWarmthChange = {},
        onTouchActiveChange = {},
    )
}
