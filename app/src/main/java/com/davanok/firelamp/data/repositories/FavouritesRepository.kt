package com.davanok.firelamp.data.repositories

import com.davanok.firelamp.data.model.FavouriteConfig

interface FavouritesRepository {
    suspend fun getFavouritesConfig(): Result<FavouriteConfig>

    suspend fun setFavouritesConfig(config: FavouriteConfig): Result<Unit>

    suspend fun updateConfig(block: (FavouriteConfig) -> FavouriteConfig): Result<Unit>
}