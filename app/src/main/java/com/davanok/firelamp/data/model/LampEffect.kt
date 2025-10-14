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
) {
    companion object {
        val Default = LampEffect(
            0.toUByte(),
            "None",
            UByte.MIN_VALUE,
            UByte.MAX_VALUE,
            UByte.MIN_VALUE,
            UByte.MAX_VALUE,
            ColorSelectType.NO
        )
    }
}
