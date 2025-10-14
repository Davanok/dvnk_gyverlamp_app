package com.davanok.firelamp.data.model

data class Time(
    val hour: UByte,
    val minute: UByte,
    val second: UByte
) {
    companion object {
        fun parse(value: String): Time {
            val valuesRaw = value.split(":")
            check(valuesRaw.size == 3)

            val values = valuesRaw.map { it.toUByteOrNull() }
            check(values.all { it != null })

            return Time(
                hour = values[0]!!,
                minute = values[1]!!,
                second = values[2]!!
            )
        }
    }
}
