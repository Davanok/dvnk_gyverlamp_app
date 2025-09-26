package com.davanok.firelamp.data.repositories

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface FireLampRepository {
    suspend fun sendCommand(
        hostname: String,
        port: Int,
        command: String,
        timeout: Duration = 1.seconds
    ): Result<String?>
}