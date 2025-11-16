package com.davanok.firelamp.data.model

data class DiscoverResponse(
    val ipAddress: String,
    val port: Int,
    val apName: String?
) {
    fun toLampAddress() = LampAddress(
        hostname = ipAddress,
        port = port,
        name = apName ?: ""
    )
}
