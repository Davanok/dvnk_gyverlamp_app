package com.davanok.firelamp.data.implementations

import com.davanok.firelamp.data.repositories.FireLampRepository
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

class FireLampRepositoryImpl(
    private val localAddress: InetSocketAddress = InetSocketAddress("127.0.0.1", 9002)
): FireLampRepository {
    override suspend fun sendCommand(
        hostname: String,
        port: Int,
        command: String,
        timeout: Duration
    ): Result<String?> = runCatching {
        val address = InetSocketAddress(hostname, port)

        val selector = ActorSelectorManager(Dispatchers.IO)
        val socket = aSocket(selector).udp().bind(localAddress)

        val packet = buildPacket { writeText(command) }
        socket.send(Datagram(packet, address))

        withTimeoutOrNull(timeout) {
            val response = socket.receive()
            response.packet.readText()
        }
    }
}