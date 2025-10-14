package com.davanok.firelamp.data.model

data class LampState(
    val effectId: UByte,
    val brightness: UByte,
    val speed: UByte,
    val scale: UByte,
    val powerOn: Boolean,
    val espIsClient: Boolean,
    val useNTP: Boolean,
    val timerRunning: Boolean,
    val buttonEnabled: Boolean,
    val lampTime: Time
)
