package com.davanok.firelamp.data.repositories

import com.davanok.firelamp.data.model.LampAddress
import com.davanok.firelamp.data.model.LampEffect
import kotlin.time.Duration

interface LampControlRepository {
    suspend fun turnOnLamp(lampAddress: LampAddress, timeout: Duration): Result<Unit>
    suspend fun turnOffLamp(lampAddress: LampAddress, timeout: Duration): Result<Unit>

    suspend fun enableCycle(lampAddress: LampAddress, timeout: Duration): Result<Unit>
    suspend fun diableCycle(lampAddress: LampAddress, timeout: Duration): Result<Unit>

    suspend fun getEffectsList(lampAddress: LampAddress, timeout: Duration): Result<List<LampEffect>>
    suspend fun setEffect(lampAddress: LampAddress, timeout: Duration, effectId: UByte): Result<Unit>
    
    suspend fun setBrightness(lampAddress: LampAddress, timeout: Duration, value: UByte): Result<Unit>
    suspend fun setSpeed(lampAddress: LampAddress, timeout: Duration, value: UByte): Result<Unit>
    suspend fun setScale(lampAddress: LampAddress, timeout: Duration, value: UByte): Result<Unit>
    
    suspend fun setDefault(lampAddress: LampAddress, timeout: Duration): Result<Unit>
    suspend fun setRandom(lampAddress: LampAddress, timeout: Duration): Result<Unit>
}