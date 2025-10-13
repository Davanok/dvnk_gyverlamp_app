package com.davanok.firelamp.data.model

import io.ktor.network.sockets.InetSocketAddress
import kotlinx.serialization.Serializable

@Serializable
data class LampAddress(
    val hostname: String,
    val port: Int
)

fun LampAddress.toSocketAddress() = InetSocketAddress(hostname, port)
fun InetSocketAddress.toLampAddress() = LampAddress(hostname, port)
