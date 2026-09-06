// SPDX-FileCopyrightText: 2026 Abraham Menéndez
// SPDX-License-Identifier: AGPL-3.0-or-later

package com.abrahammenendez.espeyu.ui.mirror

import android.view.Window
import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import com.abrahammenendez.espeyu.data.ScreenBrightness

/**
 * Overrides brightness for this window only. The system setting is left alone, and Android drops
 * the override as soon as the window stops being the one on screen.
 */
@Composable
fun ScreenBrightnessEffect(brightness: ScreenBrightness) {
    val window = LocalActivity.current?.window ?: return
    val target =
        if (brightness.isOverridden) {
            brightness.level.coerceIn(0f, 1f)
        } else {
            WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        }

    LaunchedEffect(window, target) { window.setScreenBrightness(target) }

    DisposableEffect(window) {
        onDispose {
            window.setScreenBrightness(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE)
        }
    }
}

private fun Window.setScreenBrightness(value: Float) {
    attributes = attributes.apply { screenBrightness = value }
}
