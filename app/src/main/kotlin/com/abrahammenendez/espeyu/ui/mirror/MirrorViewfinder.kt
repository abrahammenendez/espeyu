// SPDX-FileCopyrightText: 2026 Abraham Menéndez
// SPDX-License-Identifier: AGPL-3.0-or-later

package com.abrahammenendez.espeyu.ui.mirror

import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.abrahammenendez.espeyu.data.Lens
import kotlinx.coroutines.awaitCancellation

/** A phone missing one of the two lenses is unusual but the switch button must not lie. */
@Immutable
data class LensAvailability(val hasFront: Boolean = true, val hasBack: Boolean = true) {
    val canSwitch: Boolean
        get() = hasFront && hasBack

    operator fun contains(lens: Lens): Boolean = if (lens == Lens.FRONT) hasFront else hasBack
}

/**
 * Handle on the live preview. [captureFrame] is the freeze feature: it reads back exactly what is
 * on screen, already rotated and mirrored by the preview itself.
 */
@Stable
class ViewfinderState {
    internal var previewView by mutableStateOf<PreviewView?>(null)

    var isStreaming by mutableStateOf(false)
        internal set

    fun captureFrame(): ImageBitmap? {
        if (!isStreaming) return null
        val bitmap = previewView?.bitmap ?: return null
        return if (bitmap.width == 0 || bitmap.height == 0) null else bitmap.asImageBitmap()
    }
}

@Composable fun rememberViewfinderState(): ViewfinderState = remember { ViewfinderState() }

/**
 * PreviewView mirrors the front camera and leaves the back camera alone, so the viewfinder only
 * needs flipping when the wanted view disagrees with that default.
 */
internal fun isViewfinderFlipped(lens: Lens, isTrueView: Boolean): Boolean {
    val previewIsMirrored = lens == Lens.FRONT
    val wantsMirrored = !isTrueView
    return previewIsMirrored != wantsMirrored
}

/**
 * @param isActive binds the camera when true and releases it when false, which is how freezing
 *   stops the sensor rather than merely stopping the pixels.
 */
@Composable
fun MirrorViewfinder(
    lens: Lens,
    isActive: Boolean,
    zoomRatio: Float,
    state: ViewfinderState,
    onLensAvailabilityChange: (LensAvailability) -> Unit,
    onZoomCapabilitiesChange: (minRatio: Float, maxRatio: Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = state.previewView
    var camera by remember { mutableStateOf<Camera?>(null) }

    AndroidView(
        factory = { viewContext ->
            PreviewView(viewContext).also { view ->
                // PERFORMANCE is documented as not for a view that needs to be animated, and
                // as reaching STREAMING prematurely. This view is transformed, and captureFrame
                // reads it back the moment STREAMING arrives.
                view.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                view.scaleType = PreviewView.ScaleType.FILL_CENTER
                state.previewView = view
            }
        },
        modifier = modifier,
        onRelease = { state.previewView = null },
    )

    LaunchedEffect(previewView, lens, isActive) {
        val view = previewView ?: return@LaunchedEffect
        val provider = ProcessCameraProvider.awaitInstance(context)
        val availability = provider.lensAvailability()
        onLensAvailabilityChange(availability)
        provider.unbindAll()
        if (!isActive || lens !in availability) return@LaunchedEffect

        val preview = Preview.Builder().build().also { it.surfaceProvider = view.surfaceProvider }
        camera = provider.bindToLifecycle(lifecycleOwner, lens.selector, preview)
        try {
            awaitCancellation()
        } finally {
            camera = null
            provider.unbindAll()
        }
    }

    DisposableEffect(previewView) {
        val streamState = previewView?.previewStreamState
        val observer =
            Observer<PreviewView.StreamState> {
                state.isStreaming = it == PreviewView.StreamState.STREAMING
            }
        streamState?.observeForever(observer)
        onDispose {
            streamState?.removeObserver(observer)
            state.isStreaming = false
        }
    }

    DisposableEffect(camera) {
        val zoomState = camera?.cameraInfo?.zoomState
        val observer =
            Observer<androidx.camera.core.ZoomState> {
                onZoomCapabilitiesChange(it.minZoomRatio, it.maxZoomRatio)
            }
        zoomState?.observeForever(observer)
        onDispose { zoomState?.removeObserver(observer) }
    }

    LaunchedEffect(camera, zoomRatio) { camera?.cameraControl?.setZoomRatio(zoomRatio) }
}

private val Lens.selector: CameraSelector
    get() =
        when (this) {
            Lens.FRONT -> CameraSelector.DEFAULT_FRONT_CAMERA
            Lens.BACK -> CameraSelector.DEFAULT_BACK_CAMERA
        }

private fun ProcessCameraProvider.lensAvailability(): LensAvailability =
    LensAvailability(
        hasFront = hasCameraOrFalse(CameraSelector.DEFAULT_FRONT_CAMERA),
        hasBack = hasCameraOrFalse(CameraSelector.DEFAULT_BACK_CAMERA),
    )

private fun ProcessCameraProvider.hasCameraOrFalse(selector: CameraSelector): Boolean =
    runCatching {
        hasCamera(selector)
    }
    .getOrDefault(false)
