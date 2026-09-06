// SPDX-FileCopyrightText: 2026 Abraham Menéndez
// SPDX-License-Identifier: AGPL-3.0-or-later

package com.abrahammenendez.espeyu.data

import androidx.compose.runtime.Immutable

enum class Lens {
    FRONT,
    BACK;

    fun opposite(): Lens = if (this == FRONT) BACK else FRONT
}

/**
 * Starts at daylight, which is the tone grooming and makeup are judged under. Off keeps the last
 * [warmth] so toggling the light back on restores the chosen tone.
 */
@Immutable data class RingLight(val isOn: Boolean = false, val warmth: Float = 0f)

/** [level] applies only while [isOverridden]; otherwise the window follows system brightness. */
@Immutable data class ScreenBrightness(val isOverridden: Boolean = false, val level: Float = 1f)

@Immutable
data class MirrorSettings(
    val lens: Lens = Lens.FRONT,
    val isTrueView: Boolean = true,
    val brightness: ScreenBrightness = ScreenBrightness(),
    val ringLight: RingLight = RingLight(),
)
