package com.davanok.firelamp.data.di

import com.davanok.firelamp.data.implementations.FavouritesRepositoryImpl
import com.davanok.firelamp.data.implementations.FireLampRepositoryImpl
import com.davanok.firelamp.data.implementations.LampControlRepositoryImpl
import com.davanok.firelamp.data.repositories.FavouritesRepository
import com.davanok.firelamp.data.repositories.FireLampRepository
import com.davanok.firelamp.data.repositories.LampControlRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.network.sockets.InetSocketAddress
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FireLampModule {

    @Provides
    @Singleton
    fun provideFireLampRepository(): FireLampRepository = FireLampRepositoryImpl()

    @Provides
    @Singleton
    fun provideFavouritesRepository(
        repository: FireLampRepository,
    ): FavouritesRepository {
        val address = InetSocketAddress("19.168.31.83", 8888) // TODO

        return FavouritesRepositoryImpl(address, repository)
    }

    @Provides
    @Singleton
    fun provideLampControlRepository(
        repository: FireLampRepository,
        favouritesRepository: FavouritesRepository
    ): LampControlRepository {
        val address = InetSocketAddress("19.168.31.83", 8888) // TODO

        return LampControlRepositoryImpl(address, repository, favouritesRepository)
    }

}