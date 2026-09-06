// SPDX-FileCopyrightText: 2026 Abraham Menéndez
// SPDX-License-Identifier: AGPL-3.0-or-later

package com.abrahammenendez.espeyu.ui.mirror

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private const val DELTA = 0.01f

class ColorTemperatureTest {

    @Test
    fun `6600 kelvin is white`() {
        val color = kelvinToColor(6600f)
        assertEquals(1f, color.red, DELTA)
        assertEquals(1f, color.green, DELTA)
        assertEquals(1f, color.blue, DELTA)
    }

    @Test
    fun `1000 kelvin is deep amber with no blue`() {
        val color = kelvinToColor(1000f)
        assertEquals(1f, color.red, DELTA)
        assertEquals(68f / 255f, color.green, DELTA)
        assertEquals(0f, color.blue, DELTA)
    }

    @Test
    fun `temperatures outside the approximation are clamped`() {
        assertEquals(kelvinToColor(1000f), kelvinToColor(0f))
        assertEquals(kelvinToColor(40000f), kelvinToColor(100000f))
    }

    @Test
    fun `warmth adds red and removes blue`() {
        val neutral = ringLightColor(0f)
        val warm = ringLightColor(1f)
        assertTrue(warm.blue < neutral.blue)
        assertTrue(warm.red >= neutral.red)
    }

    @Test
    fun `warmth endpoints match their kelvin`() {
        assertEquals(kelvinToColor(NEUTRAL_KELVIN), ringLightColor(0f))
        assertEquals(kelvinToColor(WARM_KELVIN), ringLightColor(1f))
    }

    @Test
    fun `warmth is monotonic in blue`() {
        val samples = (0..10).map { ringLightColor(it / 10f).blue }
        assertEquals(samples.sortedDescending(), samples)
    }

    @Test
    fun `warmth outside the slider range is clamped`() {
        assertEquals(ringLightColor(0f), ringLightColor(-1f))
        assertEquals(ringLightColor(1f), ringLightColor(2f))
    }
}
