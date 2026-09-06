// SPDX-FileCopyrightText: 2026 Abraham Menéndez
// SPDX-License-Identifier: AGPL-3.0-or-later

package com.abrahammenendez.espeyu.ui.mirror

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

internal const val CONTROLS_AUTO_HIDE_MILLIS = 3_000L

@Stable
class ControlsVisibility {
    var isVisible by mutableStateOf(true)
        private set

    internal var interactions by mutableIntStateOf(0)
        private set

    fun toggle() {
        isVisible = !isVisible
    }

    /** Restarts the countdown without changing what is on screen. */
    fun keepAlive() {
        interactions++
    }

    internal fun hide() {
        isVisible = false
    }
}

/** @param isPinned holds the controls open, for a frozen frame or a slider mid-drag. */
@Composable
internal fun rememberControlsVisibility(isPinned: Boolean): ControlsVisibility {
    val visibility = remember { ControlsVisibility() }
    LaunchedEffect(visibility.isVisible, visibility.interactions, isPinned) {
        if (visibility.isVisible && !isPinned) {
            delay(CONTROLS_AUTO_HIDE_MILLIS)
            visibility.hide()
        }
    }
    return visibility
}
