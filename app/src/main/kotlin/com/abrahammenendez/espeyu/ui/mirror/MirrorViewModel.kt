// SPDX-FileCopyrightText: 2026 Abraham Menéndez
// SPDX-License-Identifier: AGPL-3.0-or-later

package com.abrahammenendez.espeyu.ui.mirror

import android.app.Application
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.abrahammenendez.espeyu.data.MirrorSettings
import com.abrahammenendez.espeyu.data.SettingsRepository
import com.abrahammenendez.espeyu.data.settingsDataStore
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MirrorViewModel(private val repository: SettingsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(MirrorUiState())
    val uiState: StateFlow<MirrorUiState> = _uiState.asStateFlow()

    private var hasLocalEdits = false

    init {
        viewModelScope.launch {
            val stored = repository.settings.first()
            // A tap that beat the first read off disk is a deliberate choice; the stored value is
            // not.
            if (!hasLocalEdits) _uiState.update { it.copy(settings = stored) }
            _uiState
                .map { it.settings }
                .distinctUntilChanged()
                .dropWhile { it == stored } // Already on disk.
                // collectLatest cancels the pending write when another change lands, so dragging
                // a slider writes once rather than once per pixel.
                .collectLatest { settings ->
                    delay(PERSIST_DEBOUNCE)
                    repository.save(settings)
                }
        }
    }

    fun toggleView() = editSettings { it.copy(isTrueView = !it.isTrueView) }

    /** Zoom ranges and the frozen frame both belong to the camera being left behind. */
    fun switchLens() = edit {
        it.copy(
            settings = it.settings.copy(lens = it.settings.lens.opposite()),
            zoom = ZoomState(),
            frozenFrame = null,
        )
    }

    fun toggleBrightnessOverride() = editSettings {
        it.copy(brightness = it.brightness.copy(isOverridden = !it.brightness.isOverridden))
    }

    fun setBrightnessLevel(level: Float) = editSettings {
        it.copy(brightness = it.brightness.copy(level = level.coerceIn(0f, 1f)))
    }

    fun toggleRingLight() = editSettings {
        it.copy(ringLight = it.ringLight.copy(isOn = !it.ringLight.isOn))
    }

    fun setRingLightWarmth(warmth: Float) = editSettings {
        it.copy(ringLight = it.ringLight.copy(warmth = warmth.coerceIn(0f, 1f)))
    }

    /** Falls back to whatever the device does have when the stored lens is missing. */
    fun setLensAvailability(availability: LensAvailability) {
        _uiState.update { state ->
            val lens = state.settings.lens
            val corrected =
                when {
                    lens in availability -> lens
                    lens.opposite() in availability -> lens.opposite()
                    else -> lens
                }
            state.copy(
                settings = state.settings.copy(lens = corrected),
                availableLenses = availability,
            )
        }
    }

    fun setZoomCapabilities(minRatio: Float, maxRatio: Float) {
        _uiState.update {
            val zoom = ZoomState(it.zoom.ratio, minRatio, maxRatio)
            it.copy(zoom = zoom.copy(ratio = zoom.coerce(zoom.ratio)))
        }
    }

    fun setZoomRatio(ratio: Float) {
        _uiState.update { it.copy(zoom = it.zoom.copy(ratio = it.zoom.coerce(ratio))) }
    }

    fun freeze(frame: ImageBitmap) = _uiState.update { it.copy(frozenFrame = frame) }

    fun unfreeze() = _uiState.update { it.copy(frozenFrame = null) }

    private fun editSettings(block: (MirrorSettings) -> MirrorSettings) = edit {
        it.copy(settings = block(it.settings))
    }

    /** The single place a change counts as the user's, and so beats what is on disk. */
    private fun edit(block: (MirrorUiState) -> MirrorUiState) {
        hasLocalEdits = true
        _uiState.update(block)
    }

    companion object {
        private val PERSIST_DEBOUNCE = 250.milliseconds

        val Factory = viewModelFactory {
            initializer {
                val application: Application = checkNotNull(this[APPLICATION_KEY])
                MirrorViewModel(SettingsRepository(application.settingsDataStore))
            }
        }
    }
}
