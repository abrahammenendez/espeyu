// SPDX-FileCopyrightText: 2026 Abraham Menéndez
// SPDX-License-Identifier: AGPL-3.0-or-later

package com.abrahammenendez.espeyu.ui.mirror

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.abrahammenendez.espeyu.data.RingLight

private val FrameWidth = 48.dp

/**
 * Fill light drawn on the screen itself, not the camera torch: the torch points away from the user
 * and would be useless for a mirror. The viewfinder gives the border up rather than being veiled by
 * it, so the whole reflection stays visible, only smaller.
 */
@Composable
fun RingLightFrame(
    ringLight: RingLight,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val isOn = ringLight.isOn
    val border = if (isOn) FrameWidth else 0.dp
    Box(
        modifier
            .background(if (isOn) ringLightColor(ringLight.warmth) else Color.Black)
            .padding(border)
            // The border already clears whatever the cutout would have, so anything drawn inside
            // should not inset itself a second time.
            .consumeWindowInsets(PaddingValues(border))
            .clip(if (isOn) MaterialTheme.shapes.extraLarge else RectangleShape),
        content = content,
    )
}
