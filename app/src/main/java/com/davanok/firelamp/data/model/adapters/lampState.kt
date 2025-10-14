package com.davanok.firelamp.data.model.adapters

import com.davanok.firelamp.data.model.LampState
import com.davanok.firelamp.data.model.Time

fun parseLampState(raw: String): LampState {
    val values = raw.split(' ')

    check(values.size == 11)

    return LampState(
        effectId = values[1].toUByte(),
        brightness = values[2].toUByte(),
        speed = values[3].toUByte(),
        scale = values[4].toUByte(),
        powerOn = values[5] == "1",
        espIsClient = values[6] == "1",
        useNTP = values[7] == "1",
        timerRunning = values[8] == "1",
        buttonEnabled = values[9] == "1",
        lampTime = Time.parse(values[10]),
    )
}