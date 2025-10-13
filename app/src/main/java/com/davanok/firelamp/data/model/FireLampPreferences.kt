package com.davanok.firelamp.data.model

import kotlinx.serialization.Serializable


@Serializable
data class FireLampPreferences(
    val lampAddresses: List<LampAddress> = listOf(LampAddress("192.168.4.1", 8888))
)