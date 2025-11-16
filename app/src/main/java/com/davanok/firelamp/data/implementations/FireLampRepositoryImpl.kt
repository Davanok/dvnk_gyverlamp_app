package com.davanok.firelamp.data.implementations

import android.util.Log
import com.davanok.firelamp.data.model.LampAddress
import com.davanok.firelamp.data.model.adapters.toSocketAddress
import com.davanok.firelamp.data.repositories.FireLampRepository
import com.davanok.firelamp.data.utils.runLogging
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.Datagram
import io.ktor.network.sockets.aSocket
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.readText
import io.ktor.utils.io.core.writeText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration

class FireLampRepositoryImpl: FireLampRepository {
    private val selector = ActorSelectorManager(Dispatchers.IO)

    override suspend fun sendCommand(
        address: LampAddress,
        command: String,
        timeout: Duration
    ): Result<String> = runLogging("sendCommand") {
        Log.d("FireLampRepositoryImpl", "address=$address; command=$command; timeout=$timeout")
        aSocket(selector).udp().bind().use { socket ->
            val packet = buildPacket { writeText(command) }
            socket.send(Datagram(packet, address.toSocketAddress()))

            withTimeout(timeout) {
                val response = socket.receive()
                response.packet.readText()
            }
        }
    }

    override fun close() {
        selector.close()
    }
}