package com.davanok.firelamp.data.repositories

import io.ktor.network.sockets.SocketAddress
import io.ktor.utils.io.core.Closeable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface FireLampRepository: Closeable {
    suspend fun sendCommand(
        address: SocketAddress,
        command: String,
        timeout: Duration = 1.seconds
    ): Result<String>
}