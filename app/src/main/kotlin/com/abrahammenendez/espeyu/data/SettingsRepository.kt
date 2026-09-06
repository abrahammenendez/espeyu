// SPDX-FileCopyrightText: 2026 Abraham Menéndez
// SPDX-License-Identifier: AGPL-3.0-or-later

package com.abrahammenendez.espeyu.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/** The only thing Espeyu writes to disk. */
class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    val settings: Flow<MirrorSettings> =
        dataStore.data
            .catch { cause -> if (cause is IOException) emit(emptyPreferences()) else throw cause }
            .map(::read)

    suspend fun save(settings: MirrorSettings) {
        dataStore.edit { preferences ->
            preferences[Keys.LENS] = settings.lens.name
            preferences[Keys.TRUE_VIEW] = settings.isTrueView
            preferences[Keys.BRIGHTNESS_OVERRIDDEN] = settings.brightness.isOverridden
            preferences[Keys.BRIGHTNESS_LEVEL] = settings.brightness.level
            preferences[Keys.RING_LIGHT_ON] = settings.ringLight.isOn
            preferences[Keys.RING_LIGHT_WARMTH] = settings.ringLight.warmth
        }
    }

    private fun read(preferences: Preferences): MirrorSettings {
        val defaults = MirrorSettings()
        return MirrorSettings(
            lens = preferences[Keys.LENS]?.let(::lensOrNull) ?: defaults.lens,
            isTrueView = preferences[Keys.TRUE_VIEW] ?: defaults.isTrueView,
            brightness =
                ScreenBrightness(
                    isOverridden =
                        preferences[Keys.BRIGHTNESS_OVERRIDDEN] ?: defaults.brightness.isOverridden,
                    level = preferences[Keys.BRIGHTNESS_LEVEL] ?: defaults.brightness.level,
                ),
            ringLight =
                RingLight(
                    isOn = preferences[Keys.RING_LIGHT_ON] ?: defaults.ringLight.isOn,
                    warmth = preferences[Keys.RING_LIGHT_WARMTH] ?: defaults.ringLight.warmth,
                ),
        )
    }

    private fun lensOrNull(name: String): Lens? = Lens.entries.firstOrNull { it.name == name }

    private object Keys {
        val LENS = stringPreferencesKey("lens")
        val TRUE_VIEW = booleanPreferencesKey("true_view")
        val BRIGHTNESS_OVERRIDDEN = booleanPreferencesKey("brightness_overridden")
        val BRIGHTNESS_LEVEL = floatPreferencesKey("brightness_level")
        val RING_LIGHT_ON = booleanPreferencesKey("ring_light_on")
        val RING_LIGHT_WARMTH = floatPreferencesKey("ring_light_warmth")
    }
}
