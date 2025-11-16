package com.davanok.firelamp.data.repositories

import com.davanok.firelamp.data.model.LampAddress
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface LampsFinderRepository {
    fun findLamps(port: Int, timeout: Duration = 1.seconds): Flow<Pair<Float, List<LampAddress>>>
    suspend fun checkConnection(address: LampAddress, timeout: Duration = 1.seconds): Result<LampAddress?>
}