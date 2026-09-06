// SPDX-FileCopyrightText: 2026 Abraham Menéndez
// SPDX-License-Identifier: AGPL-3.0-or-later

package com.abrahammenendez.espeyu.ui.mirror

import androidx.compose.ui.graphics.Color
import kotlin.math.ln
import kotlin.math.pow

private const val MIN_KELVIN = 1000f
private const val MAX_KELVIN = 40000f

/** Candlelight-warm end of the ring light's warmth slider. */
const val WARM_KELVIN = 2700f

/** Daylight end of the ring light's warmth slider, near the D65 daylight standard. */
const val NEUTRAL_KELVIN = 6500f

/**
 * Tanner Helland's approximation of the Planckian locus, accurate enough for a light source the
 * user tunes by eye. Valid from 1000 K to 40000 K; anything outside is clamped.
 *
 * https://tannerhelland.com/2012/09/18/convert-temperature-rgb-algorithm-code.html
 */
fun kelvinToColor(kelvin: Float): Color {
    val scaled = kelvin.coerceIn(MIN_KELVIN, MAX_KELVIN) / 100f
    val red = if (scaled <= 66f) 255f else 329.698727446f * (scaled - 60f).pow(-0.1332047592f)
    val green =
        if (scaled <= 66f) {
            99.4708025861f * ln(scaled) - 161.1195681661f
        } else {
            288.1221695283f * (scaled - 60f).pow(-0.0755148492f)
        }
    val blue =
        when {
            scaled >= 66f -> 255f
            scaled <= 19f -> 0f
            else -> 138.5177312231f * ln(scaled - 10f) - 305.0447927307f
        }
    return Color(red = channel(red), green = channel(green), blue = channel(blue))
}

/**
 * Warmth runs 0 (neutral) to 1 (warm). Interpolating in mireds rather than kelvin keeps the middle
 * of the slider looking like the middle of the range, which is why photographic tools use them.
 */
fun ringLightColor(warmth: Float): Color {
    val neutralMired = miredOf(NEUTRAL_KELVIN)
    val warmMired = miredOf(WARM_KELVIN)
    val mired = neutralMired + (warmMired - neutralMired) * warmth.coerceIn(0f, 1f)
    return kelvinToColor(1_000_000f / mired)
}

private fun miredOf(kelvin: Float) = 1_000_000f / kelvin

private fun channel(value: Float) = (value / 255f).coerceIn(0f, 1f)
