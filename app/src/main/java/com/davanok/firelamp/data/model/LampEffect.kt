package com.davanok.firelamp.data.model

enum class ColorSelectType {
    NO,
    YES,
    COMBINED
}

data class LampEffect(
    val id: UByte,
    val name: String,
    val minSpeed: UByte,
    val maxSpeed: UByte,
    val minScale: UByte,
    val maxScale: UByte,
    val colorSelect: ColorSelectType
)
