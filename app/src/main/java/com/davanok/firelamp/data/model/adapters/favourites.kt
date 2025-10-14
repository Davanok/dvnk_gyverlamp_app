package com.davanok.firelamp.data.model.adapters

import com.davanok.firelamp.data.model.FavouriteConfig

fun parseFavouritesConfig(raw: String): FavouriteConfig {
    val source = raw.split(' ')

    // skip FAV
    val enabled = source[1] == "1"
    val interval = source[2].toUShort()
    val dispersion = source[3].toUShort()
    val useFavourites = source[4] == "1"

    val favouritesList = source.subList(5, source.size).map { it == "1" }.toBooleanArray()

    return FavouriteConfig(
        cycleEnabled = enabled,
        interval = interval,
        dispersion = dispersion,
        useFavourites = useFavourites,
        favouritesList = favouritesList
    )
}

private fun Boolean.toIntChar() = if(this) '1' else '0'

fun serializeFavouritesConfig(config: FavouriteConfig) = buildString {
    append(config.cycleEnabled.toIntChar())
    append(' ')
    append(config.interval)
    append(' ')
    append(config.dispersion)
    append(' ')
    append(config.useFavourites.toIntChar())
    config.favouritesList.forEach {
        append(' ')
        append(it.toIntChar())
    }
}