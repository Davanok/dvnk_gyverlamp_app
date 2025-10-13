package com.davanok.firelamp.data.implementations

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.davanok.firelamp.data.model.FireLampPreferences
import com.davanok.firelamp.data.model.PreferencesKeys
import com.davanok.firelamp.data.repositories.DataStoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class DataStoreRepositoryImpl(
    defaultPreferencesKey: String,
    private val context: Context
): DataStoreRepository {
    private val Context.dataStore by preferencesDataStore(defaultPreferencesKey)
    private val dataStoreFlow = context.dataStore.data

    override suspend fun getPreferences(): FireLampPreferences {
        val preferences = dataStoreFlow.lastOrNull() ?: return FireLampPreferences()
        val raw = preferences[PreferencesKeys.APP_SETTINGS] ?: return FireLampPreferences()
        return Json.decodeFromString(FireLampPreferences.serializer(), raw)
    }

    override suspend fun setPreferences(preferences: FireLampPreferences) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.APP_SETTINGS] = Json.encodeToString(
                FireLampPreferences.serializer(),
                preferences
            )
        }
    }

    override suspend fun updatePreferences(block: (FireLampPreferences) -> FireLampPreferences) {
        context.dataStore.edit { prefs ->
            val currentRaw = prefs[PreferencesKeys.APP_SETTINGS]
            val current =
                if (currentRaw == null) FireLampPreferences()
                else Json.decodeFromString(FireLampPreferences.serializer(), currentRaw)

            val updated = block(current)

            prefs[PreferencesKeys.APP_SETTINGS] = Json.encodeToString(
                FireLampPreferences.serializer(),
                updated
            )
        }
    }

    override fun subscribeToPreferences(): Flow<FireLampPreferences> =
        dataStoreFlow.map { preferences ->
            val raw = preferences[PreferencesKeys.APP_SETTINGS]
            if (raw == null) FireLampPreferences()
            else Json.decodeFromString(FireLampPreferences.serializer(), raw)
        }
}