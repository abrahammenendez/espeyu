// SPDX-FileCopyrightText: 2026 Abraham Menéndez
// SPDX-License-Identifier: AGPL-3.0-or-later

package com.abrahammenendez.espeyu.ui.mirror

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap
import com.abrahammenendez.espeyu.data.MirrorSettings

/** Front cameras usually report a single ratio, which is why [isSupported] exists. */
@Immutable
data class ZoomState(val ratio: Float = 1f, val minRatio: Float = 1f, val maxRatio: Float = 1f) {
    val isSupported: Boolean
        get() = maxRatio > minRatio

    fun coerce(value: Float): Float = value.coerceIn(minRatio, maxRatio)
}

@Immutable
data class MirrorUiState(
    val settings: MirrorSettings = MirrorSettings(),
    val zoom: ZoomState = ZoomState(),
    val availableLenses: LensAvailability = LensAvailability(),
    val frozenFrame: ImageBitmap? = null,
) {
    val isFrozen: Boolean
        get() = frozenFrame != null
}
