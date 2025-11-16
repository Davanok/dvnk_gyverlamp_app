package com.davanok.firelamp.data.model.adapters

import com.davanok.firelamp.data.model.LampAddress
import io.ktor.network.sockets.InetSocketAddress


fun LampAddress.toSocketAddress() = InetSocketAddress(hostname, port)
fun InetSocketAddress.toLampAddress() = LampAddress(hostname, port)