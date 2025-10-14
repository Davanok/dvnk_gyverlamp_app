package com.davanok.firelamp.data.repositories

import com.davanok.firelamp.data.model.FavouriteConfig
import com.davanok.firelamp.data.model.LampAddress
import kotlin.time.Duration

interface FavouritesRepository {
    suspend fun getFavouritesConfig(lampAddress: LampAddress, timeout: Duration): Result<FavouriteConfig>

    suspend fun setFavouritesConfig(lampAddress: LampAddress, timeout: Duration, config: FavouriteConfig): Result<FavouriteConfig>

    suspend fun updateConfig(lampAddress: LampAddress, timeout: Duration, transform: (FavouriteConfig) -> FavouriteConfig): Result<FavouriteConfig>
}