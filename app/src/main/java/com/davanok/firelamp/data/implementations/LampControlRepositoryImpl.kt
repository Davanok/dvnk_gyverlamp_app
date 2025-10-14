package com.davanok.firelamp.data.implementations

import com.davanok.firelamp.data.model.LampAddress
import com.davanok.firelamp.data.model.LampEffect
import com.davanok.firelamp.data.model.LampState
import com.davanok.firelamp.data.model.adapters.parseEffectsList
import com.davanok.firelamp.data.model.adapters.parseLampState
import com.davanok.firelamp.data.repositories.FireLampRepository
import com.davanok.firelamp.data.repositories.LampControlRepository
import kotlin.time.Duration

class LampControlRepositoryImpl(
    private val repository: FireLampRepository
) : LampControlRepository {
    override suspend fun getLampState(
        lampAddress: LampAddress,
        timeout: Duration
    ): Result<LampState> =
        repository.sendCommand(lampAddress, "GET", timeout)
            .mapCatching { parseLampState(it) }

    override suspend fun turnOnLamp(lampAddress: LampAddress, timeout: Duration): Result<LampState> =
        repository.sendCommand(
            lampAddress,
            "P_ON", timeout
        ).mapCatching { parseLampState(it) }

    override suspend fun turnOffLamp(lampAddress: LampAddress, timeout: Duration): Result<LampState> =
        repository.sendCommand(
            lampAddress,
            "P_OFF", timeout
        ).mapCatching { parseLampState(it) }

    override suspend fun getEffectsList(
        lampAddress: LampAddress,
        timeout: Duration
    ): Result<List<LampEffect>> = runCatching {
        val result = mutableListOf<LampEffect>()

        repeat(10) {
            val raw = repository
                .sendCommand(lampAddress, "LIST$it", timeout)
                .getOrThrow()

            val parsed = parseEffectsList(raw)
            if (parsed.isEmpty()) return@repeat

            result.addAll(parsed)
        }

        result
    }

    override suspend fun setEffect(
        lampAddress: LampAddress,
        timeout: Duration,
        effectId: UByte
    ): Result<LampState> = repository.sendCommand(
        lampAddress,
        "EFF$effectId",
        timeout
    ).mapCatching { parseLampState(it) }

    override suspend fun setBrightness(
        lampAddress: LampAddress,
        timeout: Duration,
        value: UByte
    ): Result<LampState> = repository.sendCommand(
        lampAddress,
        "BRI$value",
        timeout
    ).mapCatching { parseLampState(it) }

    override suspend fun setSpeed(
        lampAddress: LampAddress,
        timeout: Duration,
        value: UByte
    ): Result<LampState> = repository.sendCommand(
        lampAddress,
        "SPD$value",
        timeout
    ).mapCatching { parseLampState(it) }

    override suspend fun setScale(
        lampAddress: LampAddress,
        timeout: Duration,
        value: UByte
    ): Result<LampState> = repository.sendCommand(
        lampAddress,
        "SCA$value",
        timeout
    ).mapCatching { parseLampState(it) }

    override suspend fun setDefault(lampAddress: LampAddress, timeout: Duration): Result<LampState> =
        repository.sendCommand(
            lampAddress,
            "RND_0",
            timeout
        ).mapCatching { parseLampState(it) }

    override suspend fun setRandom(lampAddress: LampAddress, timeout: Duration): Result<Unit> =
        repository.sendCommand(
            lampAddress,
            "RND_1",
            timeout
        ).map { }
}