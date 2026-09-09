// SPDX-FileCopyrightText: 2026 Abraham Menéndez
// SPDX-License-Identifier: AGPL-3.0-or-later

package com.abrahammenendez.espeyu.ui.mirror

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.abrahammenendez.espeyu.data.MirrorSettings
import com.abrahammenendez.espeyu.data.RingLight
import com.abrahammenendez.espeyu.data.ScreenBrightness
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MirrorControlsTest {

    @get:Rule val compose = createComposeRule()

    @Test
    fun `freezing waits for the preview to have a frame`() {
        show(MirrorUiState(), canFreeze = false)

        compose.onNodeWithContentDescription("Freeze the frame").assertIsNotEnabled()
    }

    @Test
    fun `freezing is offered once the preview is streaming`() {
        show(MirrorUiState(), canFreeze = true)

        compose.onNodeWithContentDescription("Freeze the frame").assertIsEnabled()
    }

    @Test
    fun `a frozen frame can always be released`() {
        show(MirrorUiState(frozenFrame = FakeFrame), canFreeze = false)

        compose.onNodeWithContentDescription("Unfreeze the frame").assertIsEnabled()
    }

    @Test
    fun `the view mode button offers the mode you are not in`() {
        show(MirrorUiState(settings = MirrorSettings(isTrueView = true)))

        compose.onNodeWithContentDescription("Show mirrored view").assertIsDisplayed()
    }

    @Test
    fun `a device with one lens does not offer a switch`() {
        show(MirrorUiState(availableLenses = LensAvailability(hasFront = true, hasBack = false)))

        compose.onNodeWithContentDescription("Switch camera").assertDoesNotExist()
    }

    @Test
    fun `overriding brightness reveals its slider`() {
        show(
            MirrorUiState(
                settings =
                    MirrorSettings(brightness = ScreenBrightness(isOverridden = true, level = 0.5f))
            )
        )

        compose.onNodeWithContentDescription("Screen brightness").assertIsDisplayed()
    }

    @Test
    fun `the ring light reveals its warmth slider`() {
        show(MirrorUiState(settings = MirrorSettings(ringLight = RingLight(isOn = true))))

        compose.onNodeWithContentDescription("Ring light warmth").assertIsDisplayed()
    }

    @Test
    fun `tapping the view mode button reports it`() {
        var toggled = false
        show(MirrorUiState(), onToggleView = { toggled = true })

        compose.onNodeWithContentDescription("Show mirrored view").performClick()

        assertTrue(toggled)
    }

    @Test
    fun `turning a control on plays the on feedback`() {
        val haptics = RecordingHapticFeedback()
        show(MirrorUiState(), haptics = haptics)

        compose.onNodeWithContentDescription("Freeze the frame").performClick()

        assertEquals(listOf(HapticFeedbackType.ToggleOn), haptics.events)
    }

    @Test
    fun `turning a control off plays the off feedback`() {
        val haptics = RecordingHapticFeedback()
        show(MirrorUiState(frozenFrame = FakeFrame), canFreeze = false, haptics = haptics)

        compose.onNodeWithContentDescription("Unfreeze the frame").performClick()

        assertEquals(listOf(HapticFeedbackType.ToggleOff), haptics.events)
    }

    @Test
    fun `switching lens plays the discrete tick`() {
        val haptics = RecordingHapticFeedback()
        show(MirrorUiState(), haptics = haptics)

        compose.onNodeWithContentDescription("Switch camera").performClick()

        assertEquals(listOf(HapticFeedbackType.SegmentTick), haptics.events)
    }

    private fun show(
        state: MirrorUiState,
        canFreeze: Boolean = true,
        onToggleView: () -> Unit = {},
        haptics: HapticFeedback = RecordingHapticFeedback(),
    ) {
        compose.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptics) {
                MirrorControls(
                    state = state,
                    canFreeze = canFreeze,
                    onToggleView = onToggleView,
                    onSwitchLens = {},
                    onToggleFreeze = {},
                    onToggleBrightness = {},
                    onBrightnessChange = {},
                    onToggleRingLight = {},
                    onWarmthChange = {},
                    onTouchActiveChange = {},
                )
            }
        }
    }
}

private class RecordingHapticFeedback : HapticFeedback {
    val events = mutableListOf<HapticFeedbackType>()

    override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
        events += hapticFeedbackType
    }
}
