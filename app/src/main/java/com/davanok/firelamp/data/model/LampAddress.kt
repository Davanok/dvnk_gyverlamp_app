package com.davanok.firelamp.data.model

import io.ktor.network.sockets.InetSocketAddress
import kotlinx.serialization.Serializable

@Serializable
data class LampAddress(
    val hostname: String,
    val port: Int
) {
    fun isValid(): Boolean {
        val parts = hostname.split('.')
        return parts.size == 4 && parts.all { it.toUByteOrNull() != null }
    }

    companion object {
        val Hotspot = LampAddress("192.168.4.1", 8888)
        val Unknown = LampAddress("0.0.0.0", 8888)
    }
}

fun LampAddress.toSocketAddress() = InetSocketAddress(hostname, port)
fun InetSocketAddress.toLampAddress() = LampAddress(hostname, port)
