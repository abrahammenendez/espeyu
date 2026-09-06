// SPDX-FileCopyrightText: 2026 Abraham Menéndez
// SPDX-License-Identifier: AGPL-3.0-or-later

package com.abrahammenendez.espeyu.ui.mirror

import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private const val PAST_THE_TIMEOUT = CONTROLS_AUTO_HIDE_MILLIS + 100

@RunWith(RobolectricTestRunner::class)
class ControlsVisibilityTest {

    @get:Rule val compose = createComposeRule()

    @Test
    fun `controls hide themselves once left alone`() {
        val visibility = show(isPinned = false)

        compose.mainClock.advanceTimeBy(PAST_THE_TIMEOUT)

        assertFalse(visibility.isVisible)
    }

    @Test
    fun `pinned controls stay`() {
        val visibility = show(isPinned = true)

        compose.mainClock.advanceTimeBy(PAST_THE_TIMEOUT)

        assertTrue(visibility.isVisible)
    }

    @Test
    fun `interacting restarts the countdown`() {
        val visibility = show(isPinned = false)

        compose.mainClock.advanceTimeBy(CONTROLS_AUTO_HIDE_MILLIS - 100)
        visibility.keepAlive()
        compose.waitForIdle()
        compose.mainClock.advanceTimeBy(CONTROLS_AUTO_HIDE_MILLIS - 100)

        assertTrue(visibility.isVisible)
    }

    @Test
    fun `hidden controls come back and then hide again`() {
        val visibility = show(isPinned = false)
        compose.mainClock.advanceTimeBy(PAST_THE_TIMEOUT)

        visibility.toggle()
        compose.waitForIdle()
        assertTrue(visibility.isVisible)

        compose.mainClock.advanceTimeBy(PAST_THE_TIMEOUT)
        assertFalse(visibility.isVisible)
    }

    private fun show(isPinned: Boolean): ControlsVisibility {
        // Idling would otherwise run the countdown out before a test can interrupt it.
        compose.mainClock.autoAdvance = false
        lateinit var visibility: ControlsVisibility
        compose.setContent { visibility = rememberControlsVisibility(isPinned) }
        return visibility
    }
}
