package com.davanok.firelamp.data.model

import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


@Serializable
data class FireLampPreferences(
    val latestLampAddress: LampAddress = LampAddress.Unknown,
    val savedLampAddresses: List<LampAddress> = listOf(LampAddress.Unknown),
    val defaultTimeout: Duration = 1.seconds,
    val findTimeout: Duration = 3.seconds
)