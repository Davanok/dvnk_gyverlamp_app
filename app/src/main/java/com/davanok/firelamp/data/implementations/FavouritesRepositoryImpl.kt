package com.davanok.firelamp.data.implementations

import com.davanok.firelamp.data.model.FavouriteConfig
import com.davanok.firelamp.data.model.LampAddress
import com.davanok.firelamp.data.model.adapters.parseFavouritesConfig
import com.davanok.firelamp.data.model.adapters.serializeFavouritesConfig
import com.davanok.firelamp.data.repositories.FavouritesRepository
import com.davanok.firelamp.data.repositories.FireLampRepository
import kotlin.time.Duration

class FavouritesRepositoryImpl(
    private val repository: FireLampRepository
): FavouritesRepository {
    override suspend fun getFavouritesConfig(
        lampAddress: LampAddress,
        timeout: Duration
    ): Result<FavouriteConfig> =
        repository.sendCommand(lampAddress, "FAV_GET", timeout)
            .mapCatching { parseFavouritesConfig(it) }

    override suspend fun setFavouritesConfig(
        lampAddress: LampAddress,
        timeout: Duration,
        config: FavouriteConfig
    ): Result<FavouriteConfig> =
        repository.sendCommand(
            lampAddress,
            "FAV_SET ${serializeFavouritesConfig(config)}",
            timeout
        ).mapCatching { parseFavouritesConfig(it) }

    override suspend fun updateConfig(
        lampAddress: LampAddress,
        timeout: Duration,
        transform: (FavouriteConfig) -> FavouriteConfig
    ): Result<FavouriteConfig> =
        getFavouritesConfig(lampAddress, timeout)
            .map(transform)
            .mapCatching { updated ->
                setFavouritesConfig(lampAddress, timeout, updated).getOrThrow()
            }
}