package com.davanok.firelamp.data.implementations

import com.davanok.firelamp.data.model.FavouriteConfig
import com.davanok.firelamp.data.repositories.FavouritesRepository
import com.davanok.firelamp.data.repositories.FireLampRepository
import io.ktor.network.sockets.SocketAddress

class FavouritesRepositoryImpl(
    private val lampAddress: SocketAddress,
    private val repository: FireLampRepository
): FavouritesRepository {
    private fun parseFavouritesConfig(raw: String): FavouriteConfig {
        val source = raw.split(' ')

        // skip FAV
        val enabled = source[1] == "1"
        val interval = source[2].toUShort()
        val dispersion = source[3].toUShort()
        val useFavourites = source[4] == "1"

        val favouritesList = source.subList(5, source.size).map { it == "1" }.toBooleanArray()

        return FavouriteConfig(
            enabled = enabled,
            interval = interval,
            dispersion = dispersion,
            useFavourites = useFavourites,
            favouritesList = favouritesList
        )
    }
    private fun Boolean.toIntChar() = if(this) '1' else '0'
    private fun serializeFavouritesConfig(config: FavouriteConfig) = buildString {
        append(config.enabled.toIntChar())
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


    override suspend fun getFavouritesConfig(): Result<FavouriteConfig> {
        val raw = repository.sendCommand(lampAddress, "FAV_GET").getOrThrow()
        val result = runCatching {
            parseFavouritesConfig(raw)
        }
        return result
    }

    override suspend fun setFavouritesConfig(config: FavouriteConfig): Result<Unit> =
        repository.sendCommand(
            lampAddress,
            "FAV_SET ${serializeFavouritesConfig(config)}"
        ).map { }

    override suspend fun updateConfig(block: (FavouriteConfig) -> FavouriteConfig): Result<Unit> =
        runCatching {
            val config = getFavouritesConfig().getOrThrow()
            val updated = block(config)
            setFavouritesConfig(updated)
        }
}