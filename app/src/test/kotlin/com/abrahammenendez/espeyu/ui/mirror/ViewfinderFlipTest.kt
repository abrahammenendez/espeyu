// SPDX-FileCopyrightText: 2026 Abraham Menéndez
// SPDX-License-Identifier: AGPL-3.0-or-later

package com.abrahammenendez.espeyu.ui.mirror

import com.abrahammenendez.espeyu.data.Lens
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ViewfinderFlipTest {

    @Test
    fun `front camera true view undoes the preview's own mirroring`() {
        assertTrue(isViewfinderFlipped(Lens.FRONT, isTrueView = true))
    }

    @Test
    fun `front camera mirrored view is what the preview already shows`() {
        assertFalse(isViewfinderFlipped(Lens.FRONT, isTrueView = false))
    }

    @Test
    fun `back camera true view is what the preview already shows`() {
        assertFalse(isViewfinderFlipped(Lens.BACK, isTrueView = true))
    }

    @Test
    fun `back camera mirrored view needs flipping`() {
        assertTrue(isViewfinderFlipped(Lens.BACK, isTrueView = false))
    }
}
