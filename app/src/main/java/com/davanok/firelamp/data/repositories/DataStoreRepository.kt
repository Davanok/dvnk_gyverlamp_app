package com.davanok.firelamp.data.repositories

import com.davanok.firelamp.data.model.FireLampPreferences
import kotlinx.coroutines.flow.Flow

interface DataStoreRepository {
    suspend fun getPreferences(): FireLampPreferences
    suspend fun setPreferences(preferences: FireLampPreferences)
    suspend fun updatePreferences(block: (FireLampPreferences) -> FireLampPreferences)
    fun subscribeToPreferences(): Flow<FireLampPreferences>
}