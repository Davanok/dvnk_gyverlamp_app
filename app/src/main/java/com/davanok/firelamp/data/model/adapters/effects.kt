package com.davanok.firelamp.data.model.adapters

import androidx.compose.ui.util.fastMapNotNull
import com.davanok.firelamp.data.model.ColorSelectType
import com.davanok.firelamp.data.model.LampEffect

fun parseEffectsList(raw: String): List<LampEffect> {
    val entitiesRaw = raw.split(';').drop(1) // drop LIST<n>

    val result = entitiesRaw.fastMapNotNull { entity ->
        val list = entity.split(',')
        if (list.size != 6) return@fastMapNotNull null // skip invalid entities

        val idName = list[0].split(". ")
        if (idName.size != 2) return@fastMapNotNull null

        runCatching {
            val colorTypeOrdinal = list[5].toInt()
            val colorType = ColorSelectType.entries[colorTypeOrdinal]

            LampEffect(
                id = idName.first().toUByte(),
                name = idName.last(),
                minSpeed = list[1].toUByte(),
                maxSpeed = list[2].toUByte(),
                minScale = list[3].toUByte(),
                maxScale = list[4].toUByte(),
                colorSelect = colorType,
            )
        }.getOrNull()
    }

    return result
}