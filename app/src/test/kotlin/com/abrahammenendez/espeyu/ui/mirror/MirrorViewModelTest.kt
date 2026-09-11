// SPDX-FileCopyrightText: 2026 Abraham Menéndez
// SPDX-License-Identifier: AGPL-3.0-or-later

package com.abrahammenendez.espeyu.ui.mirror

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModelStore
import com.abrahammenendez.espeyu.data.Lens
import com.abrahammenendez.espeyu.data.MirrorSettings
import com.abrahammenendez.espeyu.data.SettingsRepository
import java.io.File
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

private val AwaitTimeout = 10.seconds

/**
 * The view model is driven against a real preferences store on a temporary file. Reads and writes
 * are genuine IO, so every assertion that depends on one waits for it instead of assuming.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MirrorViewModelTest {

    @get:Rule val folder = TemporaryFolder()

    private val storeScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Every MirrorViewModel the test creates is tracked here so tearDown can clear it. Without
    // that, viewModelScope's persistence collector outlives the test and can resume on Main after
    // resetMain(), which fails because no real Main dispatcher exists in a plain JVM test.
    private val viewModelStore = ViewModelStore()
    private var nextViewModelKey = 0

    @Before fun setUp() = Dispatchers.setMain(Dispatchers.Unconfined)

    @After
    fun tearDown() {
        viewModelStore.clear()
        storeScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun `view mode starts unmirrored`() {
        assertTrue(viewModel().uiState.value.settings.isTrueView)
    }

    @Test
    fun `stored settings are restored`() = await {
        val store = store()
        SettingsRepository(store).save(MirrorSettings(lens = Lens.BACK))

        val viewModel = track(MirrorViewModel(SettingsRepository(store)))

        assertEquals(
            Lens.BACK,
            viewModel.uiState.first { it.settings.lens == Lens.BACK }.settings.lens,
        )
    }

    @Test
    fun `changing a setting reaches the store`() = await {
        val store = store()
        val viewModel = track(MirrorViewModel(SettingsRepository(store)))

        viewModel.toggleView()

        assertFalse(SettingsRepository(store).settings.first { !it.isTrueView }.isTrueView)
    }

    @Test
    fun `a change made before the stored settings arrive is kept`() = await {
        val store = store()
        SettingsRepository(store).save(MirrorSettings(isTrueView = true))

        val viewModel = track(MirrorViewModel(SettingsRepository(store)))
        viewModel.toggleView()

        assertFalse(viewModel.uiState.value.settings.isTrueView)
    }

    @Test
    fun `switching lens drops the frozen frame and the old zoom range`() {
        val viewModel = viewModel()
        viewModel.setZoomCapabilities(minRatio = 1f, maxRatio = 4f)
        viewModel.setZoomRatio(3f)
        viewModel.freeze(FakeFrame)

        viewModel.switchLens()

        val state = viewModel.uiState.value
        assertEquals(Lens.BACK, state.settings.lens)
        assertNull(state.frozenFrame)
        assertEquals(ZoomState(), state.zoom)
    }

    @Test
    fun `a lens the device does not have is swapped for one it does`() {
        val viewModel = viewModel()

        viewModel.setLensAvailability(LensAvailability(hasFront = false, hasBack = true))

        assertEquals(Lens.BACK, viewModel.uiState.value.settings.lens)
    }

    @Test
    fun `a device reporting neither lens is left alone rather than flipped forever`() {
        val viewModel = viewModel()

        viewModel.setLensAvailability(LensAvailability(hasFront = false, hasBack = false))

        assertEquals(Lens.FRONT, viewModel.uiState.value.settings.lens)
    }

    @Test
    fun `a narrowed zoom range pulls the current ratio back in`() {
        val viewModel = viewModel()
        viewModel.setZoomCapabilities(minRatio = 1f, maxRatio = 8f)
        viewModel.setZoomRatio(8f)

        viewModel.setZoomCapabilities(minRatio = 1f, maxRatio = 2f)

        assertEquals(2f, viewModel.uiState.value.zoom.ratio, 0f)
    }

    @Test
    fun `unfreezing clears the frame`() {
        val viewModel = viewModel()
        viewModel.freeze(FakeFrame)

        viewModel.unfreeze()

        assertFalse(viewModel.uiState.value.isFrozen)
    }

    @Test
    fun `levels outside the slider range are rejected`() {
        val viewModel = viewModel()

        viewModel.setBrightnessLevel(2f)
        viewModel.setRingLightWarmth(-1f)

        val settings = viewModel.uiState.value.settings
        assertEquals(1f, settings.brightness.level, 0f)
        assertEquals(0f, settings.ringLight.warmth, 0f)
    }

    private fun await(body: suspend CoroutineScope.() -> Unit) = runBlocking {
        withTimeout(AwaitTimeout) { body() }
    }

    private fun track(viewModel: MirrorViewModel): MirrorViewModel {
        viewModelStore.put((nextViewModelKey++).toString(), viewModel)
        return viewModel
    }

    private fun viewModel() = track(MirrorViewModel(SettingsRepository(store())))

    private fun store(): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(scope = storeScope) {
            File(folder.newFolder(), "settings.preferences_pb")
        }
}
