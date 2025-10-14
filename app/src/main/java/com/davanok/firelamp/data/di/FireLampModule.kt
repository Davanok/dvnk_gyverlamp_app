package com.davanok.firelamp.data.di

import android.content.Context
import com.davanok.firelamp.BuildConfig
import com.davanok.firelamp.data.implementations.DataStoreRepositoryImpl
import com.davanok.firelamp.data.implementations.FavouritesRepositoryImpl
import com.davanok.firelamp.data.implementations.FireLampRepositoryImpl
import com.davanok.firelamp.data.implementations.LampControlRepositoryImpl
import com.davanok.firelamp.data.repositories.DataStoreRepository
import com.davanok.firelamp.data.repositories.FavouritesRepository
import com.davanok.firelamp.data.repositories.FireLampRepository
import com.davanok.firelamp.data.repositories.LampControlRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FireLampModule {

    @Provides
    @Singleton
    fun provideDatastoreRepository(
        @ApplicationContext context: Context
    ): DataStoreRepository =
        DataStoreRepositoryImpl(BuildConfig.DATASTORE_PREFERENCES_KEY, context)

    @Provides
    @Singleton
    fun provideFireLampRepository(): FireLampRepository = FireLampRepositoryImpl()

    @Provides
    @Singleton
    fun provideFavouritesRepository(
        repository: FireLampRepository
    ): FavouritesRepository {
        return FavouritesRepositoryImpl(repository)
    }

    @Provides
    @Singleton
    fun provideLampControlRepository(
        repository: FireLampRepository
    ): LampControlRepository {
        return LampControlRepositoryImpl(repository)
    }

}