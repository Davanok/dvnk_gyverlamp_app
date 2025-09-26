package com.davanok.firelamp.data.implementations

import com.davanok.firelamp.data.repositories.FireLampRepository
import com.davanok.firelamp.data.utils.runLogging
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.Datagram
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.readText
import io.ktor.utils.io.core.writeText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration

class FireLampRepositoryImpl(): FireLampRepository {
    override suspend fun sendCommand(
        hostname: String,
        port: Int,
        command: String,
        timeout: Duration
    ): Result<String?> = runLogging("sendCommand") {
        ActorSelectorManager(Dispatchers.IO).use { selector ->
            aSocket(selector).udp().bind().use { socket ->
                val packet = buildPacket { writeText(command) }
                socket.send(Datagram(packet, InetSocketAddress(hostname, port)))

                withTimeoutOrNull(timeout) {
                    val response = socket.receive()
                    response.packet.readText()
                }
            }
        }
    }
}