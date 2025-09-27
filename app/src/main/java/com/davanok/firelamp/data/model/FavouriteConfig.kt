package com.davanok.firelamp.data.model

data class FavouriteConfig(
    val enabled: Boolean,
    val interval: UShort,
    val dispersion: UShort,
    val useFavourites: Boolean,
    val favouritesList: BooleanArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FavouriteConfig

        if (enabled != other.enabled) return false
        if (useFavourites != other.useFavourites) return false
        if (interval != other.interval) return false
        if (dispersion != other.dispersion) return false
        if (!favouritesList.contentEquals(other.favouritesList)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = enabled.hashCode()
        result = 31 * result + useFavourites.hashCode()
        result = 31 * result + interval.hashCode()
        result = 31 * result + dispersion.hashCode()
        result = 31 * result + favouritesList.contentHashCode()
        return result
    }
}