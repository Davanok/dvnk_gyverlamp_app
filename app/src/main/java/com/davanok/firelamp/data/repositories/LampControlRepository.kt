package com.davanok.firelamp.data.repositories

import com.davanok.firelamp.data.model.LampEffect

interface LampControlRepository {
    suspend fun turnOnLamp(): Result<Unit>
    suspend fun turnOffLamp(): Result<Unit>

    suspend fun enableCycle(): Result<Unit>
    suspend fun diableCycle(): Result<Unit>

    suspend fun getEffectsList(): Result<List<LampEffect>>
    suspend fun setEffect(effectId: UByte): Result<Unit>
    
    suspend fun setBrightness(value: UByte): Result<Unit>
    suspend fun setSpeed(value: UByte): Result<Unit>
    suspend fun setScale(value: UByte): Result<Unit>
    
    suspend fun setDefault(): Result<Unit>
    suspend fun setRandom(): Result<Unit>
}