// SPDX-FileCopyrightText: 2026 Abraham Menéndez
// SPDX-License-Identifier: AGPL-3.0-or-later

package com.abrahammenendez.espeyu.ui.mirror

import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abrahammenendez.espeyu.ui.about.AboutDialog
import com.abrahammenendez.espeyu.ui.about.AboutMark

/** FloatingToolbarTokens.ContainerExternalPadding: how far a floating bar sits off the edge. */
private val ScreenEdgePadding = 16.dp

@Composable
fun MirrorScreen(
    modifier: Modifier = Modifier,
    viewModel: MirrorViewModel = viewModel(factory = MirrorViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val viewfinder = rememberViewfinderState()
    val settings = state.settings

    var isTouchingControls by remember { mutableStateOf(false) }
    var isAboutOpen by rememberSaveable { mutableStateOf(false) }
    val controls =
        rememberControlsVisibility(isPinned = state.isFrozen || isTouchingControls || isAboutOpen)

    // Cold start is only over when there is something to look at, which the startup benchmark
    // then reads back as time to full display.
    ReportDrawnWhen { viewfinder.isStreaming }

    ScreenBrightnessEffect(settings.brightness)

    val horizontalScale = if (isViewfinderFlipped(settings.lens, settings.isTrueView)) -1f else 1f
    val pinch = rememberTransformableState { _, zoomChange, _, _ ->
        viewModel.setZoomRatio(state.zoom.ratio * zoomChange)
    }

    BoxWithConstraints(
        modifier =
            modifier
                .fillMaxSize()
                .transformable(state = pinch, enabled = state.zoom.isSupported && !state.isFrozen)
                .pointerInput(Unit) {
                    detectTapGestures { controls.toggle() }
                }
    ) {
        RingLightFrame(settings.ringLight, Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize().graphicsLayer { scaleX = horizontalScale }) {
                MirrorViewfinder(
                    lens = settings.lens,
                    isActive = !state.isFrozen,
                    zoomRatio = state.zoom.ratio,
                    state = viewfinder,
                    onLensAvailabilityChange = viewModel::setLensAvailability,
                    onZoomCapabilitiesChange = viewModel::setZoomCapabilities,
                    modifier = Modifier.fillMaxSize(),
                )
                state.frozenFrame?.let { frame ->
                    // A frame captured before the device turned no longer matches the screen, so
                    // it is fitted rather than cropped, over black to hide the viewfinder behind
                    // it.
                    Image(
                        bitmap = frame,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize().background(Color.Black),
                    )
                }
            }
            AboutMark(
                onClick = { isAboutOpen = true },
                modifier = Modifier.align(Alignment.TopEnd).safeDrawingPadding(),
            )
        }

        val isLandscape = maxWidth > maxHeight
        AnimatedVisibility(
            visible = controls.isVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier =
                Modifier.align(if (isLandscape) Alignment.CenterEnd else Alignment.BottomCenter),
        ) {
            MirrorControls(
                state = state,
                canFreeze = viewfinder.isStreaming,
                onToggleView = viewModel::toggleView,
                onSwitchLens = viewModel::switchLens,
                onToggleFreeze = { toggleFreeze(state, viewfinder, viewModel) },
                onToggleBrightness = viewModel::toggleBrightnessOverride,
                onBrightnessChange = viewModel::setBrightnessLevel,
                onToggleRingLight = viewModel::toggleRingLight,
                onWarmthChange = viewModel::setRingLightWarmth,
                onTouchActiveChange = { isActive ->
                    isTouchingControls = isActive
                    if (!isActive) controls.keepAlive()
                },
                modifier = Modifier.safeDrawingPadding().padding(ScreenEdgePadding),
            )
        }

        if (isAboutOpen) AboutDialog(onDismiss = { isAboutOpen = false })
    }
}

/** A frame that cannot be read back is not worth freezing on, so the tap is dropped. */
private fun toggleFreeze(
    state: MirrorUiState,
    viewfinder: ViewfinderState,
    viewModel: MirrorViewModel,
) {
    if (state.isFrozen) viewModel.unfreeze() else viewfinder.captureFrame()?.let(viewModel::freeze)
}
