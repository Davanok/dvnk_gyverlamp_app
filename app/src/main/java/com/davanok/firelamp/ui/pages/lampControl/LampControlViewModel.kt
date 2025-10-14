package com.davanok.firelamp.ui.pages.lampControl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davanok.firelamp.data.model.ColorSelectType
import com.davanok.firelamp.data.model.FavouriteConfig
import com.davanok.firelamp.data.model.LampAddress
import com.davanok.firelamp.data.model.LampEffect
import com.davanok.firelamp.data.model.LampState
import com.davanok.firelamp.data.repositories.DataStoreRepository
import com.davanok.firelamp.data.repositories.FavouritesRepository
import com.davanok.firelamp.data.repositories.LampControlRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class LampControlViewModel @Inject constructor(
    private val controlRepository: LampControlRepository,
    private val favouritesRepository: FavouritesRepository,
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    private val _appConfig = dataStoreRepository.subscribeToPreferences()
    private var defaultTimeout: Duration = 1.seconds
    private val _uiState = MutableStateFlow(LampControlUiState())

    private var loadJob: Job? = null

    val uiState: StateFlow<LampControlUiState> = combine(
        _appConfig,
        _uiState
    ) { appConfig, state ->
        defaultTimeout = appConfig.defaultTimeout
        state.copy(
            currentLampAddress = appConfig.latestLampAddress,
            availableLampAddresses = appConfig.savedLampAddresses
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LampControlUiState()
    )

    private fun safeNormalize(value: UByte, min: UByte, max: UByte): Float {
        val span = (max - min).toInt()
        return if (span == 0) 0f
        else ((value - min).toFloat() / span).coerceIn(0f, 1f)
    }

    private fun normalizedToUByte(normalized: Float, min: UByte, max: UByte): UByte {
        val min = min.toInt()
        val max = max.toInt()

        val span = (max - min)
        val raw = if (span == 0) min
        else (min + (normalized.coerceIn(0f, 1f) * span)).roundToInt()
        return raw.coerceIn(min, max).toUByte()
    }


    private fun handleLampState(lampState: LampState) {
        _uiState.update {
            val currentEffect = it.lampAvailableEffects
                .getOrElse(lampState.effectId.toInt()) { LampEffect.Default }

            val brightness = safeNormalize(lampState.brightness, 0.toUByte(), 255.toUByte())
            val speed = safeNormalize(lampState.speed, currentEffect.minSpeed, currentEffect.maxSpeed)
            val scale = safeNormalize(lampState.scale, currentEffect.minScale, currentEffect.maxScale)

            it.copy(
                lampPowerOn = lampState.powerOn,
                lampCurrentEffect = currentEffect,
                lampBrightness = brightness,
                lampSpeed = speed,
                lampScale = scale,
                scaleIsColor = currentEffect.colorSelect == ColorSelectType.YES
            )
        }
    }

    private fun handleFavouriteConfig(config: FavouriteConfig) {
        _uiState.update { it.copy(lampCycleEnabled = config.cycleEnabled) }
    }

    private fun Result<LampState>.handleLampStateResponse() =
        onSuccess { lampState ->
            _uiState.update { it.copy(lampConnected = true) }
            handleLampState(lampState)
        }.onFailure {
            _uiState.update { it.copy(lampConnected = false) }
        }

    private fun Result<FavouriteConfig>.handleFavouritesConfigResponse() =
        onSuccess { favouriteConfig ->
            _uiState.update { it.copy(lampConnected = true) }
            handleFavouriteConfig(favouriteConfig)
        }.onFailure {
            _uiState.update { it.copy(lampConnected = false) }
        }

    private suspend fun loadLampData(lampAddress: LampAddress) {
        val timeout = defaultTimeout

        controlRepository.getEffectsList(lampAddress, timeout)
            .onSuccess { effects ->
                _uiState.update { it.copy(lampConnected = true, lampAvailableEffects = effects) }
            }.onFailure {
                _uiState.update { it.copy(lampConnected = false) }
                return
            }

        controlRepository
            .getLampState(lampAddress, timeout)
            .mapCatching {
                it to favouritesRepository.getFavouritesConfig(lampAddress, timeout).getOrThrow()
            }
            .onSuccess { (lampState, favouritesState) ->
                _uiState.update {
                    it.copy(
                        lampConnected = true,
                        lampCycleEnabled = favouritesState.cycleEnabled
                    )
                }
                handleLampState(lampState)
            }
            .onFailure {
                _uiState.update { it.copy(lampConnected = false) }
            }
    }

    private fun safeLoadLampData(lampAddress: LampAddress) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch { loadLampData(lampAddress) }
    }

    init {
        viewModelScope.launch {
            val config = _appConfig.first()
            safeLoadLampData(config.latestLampAddress)
        }
    }

    /* ---------- Public API ---------- */

    fun setCurrentLamp(lampAddress: LampAddress) = viewModelScope.launch {
        dataStoreRepository.updatePreferences {
            it.copy(latestLampAddress = lampAddress)
        }
        safeLoadLampData(lampAddress)
    }

    fun setLampPowerOn(powerOn: Boolean) = viewModelScope.launch {
        val lampAddress = uiState.value.currentLampAddress
        val result = if (powerOn)
            controlRepository.turnOnLamp(lampAddress = lampAddress, timeout = defaultTimeout)
        else
            controlRepository.turnOffLamp(lampAddress = lampAddress, timeout = defaultTimeout)

        result.handleLampStateResponse()
    }

    fun setCycleEnabled(enabled: Boolean) = viewModelScope.launch {
        val lampAddress = uiState.value.currentLampAddress
        favouritesRepository.updateConfig(lampAddress = lampAddress, timeout = defaultTimeout) {
            it.copy(cycleEnabled = enabled)
        }.handleFavouritesConfigResponse()
    }

    fun setLampEffect(effect: LampEffect) = viewModelScope.launch {
        val lampAddress = uiState.value.currentLampAddress
        controlRepository.setEffect(lampAddress = lampAddress, timeout = defaultTimeout, effectId = effect.id)
            .handleLampStateResponse()
    }

    fun setLampBrightness(brightness: Float) = viewModelScope.launch {
        val ui = uiState.value
        _uiState.update { it.copy(lampBrightness = brightness.coerceIn(0f, 1f)) }

        val lampAddress = ui.currentLampAddress

        val brightnessInRange = normalizedToUByte(brightness, 0.toUByte(), 255.toUByte())
        controlRepository.setBrightness(lampAddress = lampAddress, timeout = defaultTimeout, value = brightnessInRange)
            .handleLampStateResponse()
    }

    fun setLampSpeed(speed: Float) = viewModelScope.launch {
        val ui = uiState.value
        _uiState.update { it.copy(lampSpeed = speed.coerceIn(0f, 1f)) }

        val lampAddress = ui.currentLampAddress
        val currentEffect = ui.lampCurrentEffect

        val speedInRange = normalizedToUByte(speed, currentEffect.minSpeed, currentEffect.maxSpeed)
        controlRepository.setSpeed(lampAddress = lampAddress, timeout = defaultTimeout, value = speedInRange)
            .handleLampStateResponse()
    }

    fun setLampScale(scale: Float) = viewModelScope.launch {
        val ui = uiState.value
        _uiState.update { it.copy(lampScale = scale.coerceIn(0f, 1f)) }

        val lampAddress = ui.currentLampAddress
        val currentEffect = ui.lampCurrentEffect

        val scaleInRange = normalizedToUByte(scale, currentEffect.minScale, currentEffect.maxScale)
        controlRepository.setScale(lampAddress = lampAddress, timeout = defaultTimeout, value = scaleInRange)
            .handleLampStateResponse()
    }
}

data class LampControlUiState(
    val lampConnected: Boolean = false,
    val currentLampAddress: LampAddress = LampAddress.Hotspot,
    val availableLampAddresses: List<LampAddress> = listOf(LampAddress.Hotspot),
    val lampPowerOn: Boolean = false,
    val lampCycleEnabled: Boolean = false,
    val lampCurrentEffect: LampEffect = LampEffect.Default,
    val lampAvailableEffects: List<LampEffect> = listOf(LampEffect.Default),
    val lampBrightness: Float = 0f,
    val lampSpeed: Float = 0f,
    val lampScale: Float = 0f,
    val scaleIsColor: Boolean = false
)
