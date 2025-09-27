package com.davanok.firelamp.data.implementations

import androidx.compose.ui.util.fastMapNotNull
import com.davanok.firelamp.data.model.ColorSelectType
import com.davanok.firelamp.data.model.LampEffect
import com.davanok.firelamp.data.repositories.FavouritesRepository
import com.davanok.firelamp.data.repositories.FireLampRepository
import com.davanok.firelamp.data.repositories.LampControlRepository
import io.ktor.network.sockets.SocketAddress

class LampControlRepositoryImpl(
    private val lampAddress: SocketAddress,
    private val repository: FireLampRepository,
    private val favouritesRepository: FavouritesRepository
): LampControlRepository {
    override suspend fun turnOnLamp(): Result<Unit> = repository.sendCommand(
        lampAddress,
        "P_ON"
    ).map { }

    override suspend fun turnOffLamp(): Result<Unit> = repository.sendCommand(
        lampAddress,
        "P_OFF"
    ).map { }

    override suspend fun enableCycle(): Result<Unit> = favouritesRepository.updateConfig {
        it.copy(enabled = true)
    }

    override suspend fun diableCycle(): Result<Unit> = favouritesRepository.updateConfig {
        it.copy(enabled = false)
    }

    private fun parseEffectsList(raw: String): List<LampEffect> {
        val entitiesRaw = raw.split(';').drop(1) // drop LIST<n>

        val result = entitiesRaw.fastMapNotNull { entity ->
            val list = entity.split(',')
            if (list.size != 6) return@fastMapNotNull null // skip invalid entities

            val idName = list[0].split(". ")
            if (idName.size != 2) return@fastMapNotNull null

            runCatching {
                val colorTypeOrdinal = list[5].toInt()
                val colorType = ColorSelectType.entries[colorTypeOrdinal]

                LampEffect(
                    id = idName.first().toUByte(),
                    name = idName.last(),
                    minSpeed = list[1].toUByte(),
                    maxSpeed = list[2].toUByte(),
                    minScale = list[3].toUByte(),
                    maxScale = list[4].toUByte(),
                    colorSelect = colorType,
                )
            }.getOrNull()
        }

        return result
    }

    override suspend fun getEffectsList(): Result<List<LampEffect>> = runCatching {
        val result = mutableListOf<LampEffect>()

        repeat(10) {
            val raw = repository
                .sendCommand(lampAddress, "LIST$it")
                .getOrThrow()

            val parsed = parseEffectsList(raw)
            if (parsed.isEmpty()) return@repeat

            result.addAll(parsed)
        }

        result
    }

    override suspend fun setEffect(effectId: UByte): Result<Unit> = repository.sendCommand(
        lampAddress,
        "EFF$effectId"
    ).map { }

    override suspend fun setBrightness(value: UByte): Result<Unit> = repository.sendCommand(
        lampAddress,
        "BRI$value"
    ).map { }

    override suspend fun setSpeed(value: UByte): Result<Unit> = repository.sendCommand(
        lampAddress,
        "SPD$value"
    ).map { }

    override suspend fun setScale(value: UByte): Result<Unit> = repository.sendCommand(
        lampAddress,
        "SCA$value"
    ).map { }

    override suspend fun setDefault(): Result<Unit> = repository.sendCommand(
        lampAddress,
        "RND_0"
    ).map { }

    override suspend fun setRandom(): Result<Unit> = repository.sendCommand(
        lampAddress,
        "RND_1"
    ).map { }
}