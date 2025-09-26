package com.davanok.firelamp.data.di

import com.davanok.firelamp.data.implementations.FireLampRepositoryImpl
import com.davanok.firelamp.data.repositories.FireLampRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FireLampModule {

    @Provides
    @Singleton
    fun provideFireLampRepository(): FireLampRepository = FireLampRepositoryImpl()

}