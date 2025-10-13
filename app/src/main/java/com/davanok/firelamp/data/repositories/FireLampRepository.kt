package com.davanok.firelamp.data.repositories

import com.davanok.firelamp.data.model.LampAddress
import io.ktor.utils.io.core.Closeable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface FireLampRepository: Closeable {
    suspend fun sendCommand(
        address: LampAddress,
        command: String,
        timeout: Duration = 1.seconds
    ): Result<String>
}