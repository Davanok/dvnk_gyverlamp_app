package com.davanok.firelamp.data.model.adapters

import com.davanok.firelamp.data.model.DiscoverResponse

fun parseDiscoverResponse(raw: String): DiscoverResponse {
    val parts = raw.split(':')

    val ipAddress = parts[0]
    val port = parts[1].toInt()
    val apName = parts.getOrNull(2)

    return DiscoverResponse(
        ipAddress = ipAddress,
        port = port,
        apName = apName
    )
}