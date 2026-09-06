// SPDX-FileCopyrightText: 2026 Abraham Menéndez
// SPDX-License-Identifier: AGPL-3.0-or-later

package com.abrahammenendez.espeyu.ui.mirror

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ZoomStateTest {

    @Test
    fun `a single available ratio is not zoom`() {
        assertFalse(ZoomState(minRatio = 1f, maxRatio = 1f).isSupported)
    }

    @Test
    fun `a range wider than a point is zoom`() {
        assertTrue(ZoomState(minRatio = 1f, maxRatio = 4f).isSupported)
    }

    @Test
    fun `ratios are held inside the reported range`() {
        val zoom = ZoomState(minRatio = 0.6f, maxRatio = 4f)
        assertEquals(0.6f, zoom.coerce(0.1f), 0f)
        assertEquals(4f, zoom.coerce(9f), 0f)
        assertEquals(2f, zoom.coerce(2f), 0f)
    }
}
