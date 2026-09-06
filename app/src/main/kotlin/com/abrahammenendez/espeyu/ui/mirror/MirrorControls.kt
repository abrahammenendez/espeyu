// SPDX-FileCopyrightText: 2026 Abraham Menéndez
// SPDX-License-Identifier: AGPL-3.0-or-later

package com.abrahammenendez.espeyu.ui.mirror

import androidx.annotation.DrawableRes
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.abrahammenendez.espeyu.R

private val ControlSpacing = 4.dp

private val ControlPadding = 8.dp

/** Brings the track's ends level with the button containers either side of the row below. */
private val SliderInset = 2.dp

private val SliderIconSize = 20.dp

/**
 * @param canFreeze false until the preview has a frame worth keeping.
 * @param onTouchActiveChange keeps the controls on screen for the whole of a slider drag.
 */
@Composable
fun MirrorControls(
    state: MirrorUiState,
    canFreeze: Boolean,
    onToggleView: () -> Unit,
    onSwitchLens: () -> Unit,
    onToggleFreeze: () -> Unit,
    onToggleBrightness: () -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onToggleRingLight: () -> Unit,
    onWarmthChange: (Float) -> Unit,
    onTouchActiveChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings = state.settings

    Surface(
        modifier = modifier.trackTouchActivity(onTouchActiveChange),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.width(IntrinsicSize.Min).padding(ControlPadding),
            verticalArrangement = Arrangement.spacedBy(ControlSpacing),
        ) {
            if (settings.brightness.isOverridden) {
                ControlSlider(
                    icon = R.drawable.ic_brightness,
                    label = stringResource(R.string.slider_brightness),
                    value = settings.brightness.level,
                    onValueChange = onBrightnessChange,
                )
            }
            if (settings.ringLight.isOn) {
                ControlSlider(
                    icon = R.drawable.ic_ring_light,
                    label = stringResource(R.string.slider_warmth),
                    value = settings.ringLight.warmth,
                    onValueChange = onWarmthChange,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(ControlSpacing, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (state.availableLenses.canSwitch) {
                    ActionControl(
                        onClick = onSwitchLens,
                        icon = R.drawable.ic_lens_switch,
                        label = stringResource(R.string.action_switch_lens),
                    )
                }
                ToggleControl(
                    checked = !settings.isTrueView,
                    onCheckedChange = { onToggleView() },
                    icon = R.drawable.ic_view_mode,
                    label =
                        stringResource(
                            if (settings.isTrueView) R.string.action_show_mirrored_view
                            else R.string.action_show_true_view
                        ),
                )
                ToggleControl(
                    checked = state.isFrozen,
                    onCheckedChange = { onToggleFreeze() },
                    icon = if (state.isFrozen) R.drawable.ic_unfreeze else R.drawable.ic_freeze,
                    label =
                        stringResource(
                            if (state.isFrozen) R.string.action_unfreeze else R.string.action_freeze
                        ),
                    enabled = state.isFrozen || canFreeze,
                )
                ToggleControl(
                    checked = settings.brightness.isOverridden,
                    onCheckedChange = { onToggleBrightness() },
                    icon = R.drawable.ic_brightness,
                    label =
                        stringResource(
                            if (settings.brightness.isOverridden)
                                R.string.action_turn_brightness_off
                            else R.string.action_turn_brightness_on
                        ),
                )
                ToggleControl(
                    checked = settings.ringLight.isOn,
                    onCheckedChange = { onToggleRingLight() },
                    icon = R.drawable.ic_ring_light,
                    label =
                        stringResource(
                            if (settings.ringLight.isOn) R.string.action_turn_ring_light_off
                            else R.string.action_turn_ring_light_on
                        ),
                )
            }
        }
    }
}

@Composable
private fun ToggleControl(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    @DrawableRes icon: Int,
    label: String,
    enabled: Boolean = true,
) {
    FilledIconToggleButton(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
    ) {
        Icon(painter = painterResource(icon), contentDescription = label)
    }
}

/** Borrows the unchecked [ToggleControl] colours, for the one button with no state to show. */
@Composable
private fun ActionControl(onClick: () -> Unit, @DrawableRes icon: Int, label: String) {
    val unchecked = IconButtonDefaults.filledIconToggleButtonColors()
    FilledIconButton(
        onClick = onClick,
        colors =
            IconButtonDefaults.filledIconButtonColors(
                containerColor = unchecked.containerColor,
                contentColor = unchecked.contentColor,
            ),
    ) {
        Icon(painter = painterResource(icon), contentDescription = label)
    }
}

@Composable
private fun ControlSlider(
    @DrawableRes icon: Int,
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
) {
    Row(
        modifier = Modifier.padding(horizontal = SliderInset),
        horizontalArrangement = Arrangement.spacedBy(ControlPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(SliderIconSize),
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.semantics { contentDescription = label },
        )
    }
}

/** Reports touches without consuming them, so the children still get their gestures. */
private fun Modifier.trackTouchActivity(onActiveChange: (Boolean) -> Unit): Modifier =
    pointerInput(onActiveChange) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            onActiveChange(true)
            try {
                waitForUpOrCancellation(PointerEventPass.Initial)
            } finally {
                onActiveChange(false)
            }
        }
    }
