// SPDX-FileCopyrightText: 2026 Abraham Menéndez
// SPDX-License-Identifier: AGPL-3.0-or-later

package com.abrahammenendez.espeyu.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SettingsRepositoryTest {

    @get:Rule val folder = TemporaryFolder()

    @Test
    fun `an empty store reads back the defaults`() = runTest {
        assertEquals(MirrorSettings(), repository().settings.first())
    }

    @Test
    fun `settings survive a round trip`() = runTest {
        val repository = repository()
        val settings =
            MirrorSettings(
                lens = Lens.BACK,
                isTrueView = false,
                brightness = ScreenBrightness(isOverridden = true, level = 0.25f),
                ringLight = RingLight(isOn = true, warmth = 0.75f),
            )

        repository.save(settings)

        assertEquals(settings, repository.settings.first())
    }

    @Test
    fun `an unrecognised lens falls back to the default`() = runTest {
        val store = store()
        store.edit { it[stringPreferencesKey("lens")] = "SIDE" }

        assertEquals(MirrorSettings().lens, SettingsRepository(store).settings.first().lens)
    }

    private fun TestScope.repository() = SettingsRepository(store())

    private fun TestScope.store(): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(scope = backgroundScope) {
            File(folder.newFolder(), "settings.preferences_pb")
        }
}
