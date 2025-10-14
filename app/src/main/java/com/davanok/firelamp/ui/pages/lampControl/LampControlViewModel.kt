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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
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

    private fun handleLampState(lampState: LampState) {
        _uiState.update {
            val currentEffect = it.lampAvailableEffects
                .getOrElse(lampState.effectId.toInt()) { LampEffect.Default }

            val brightness = (lampState.brightness.toFloat() / 255).coerceIn(0f, 1f)
            val speed = ((lampState.speed - currentEffect.minSpeed).toFloat() / (currentEffect.maxSpeed - currentEffect.minSpeed).toFloat()).coerceIn(0f, 1f)
            val scale = ((lampState.scale - currentEffect.minScale).toFloat() / (currentEffect.maxScale - currentEffect.minScale).toFloat()).coerceIn(0f, 1f)

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
        _uiState.update {
            it.copy(
                lampCycleEnabled = config.cycleEnabled
            )
        }
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

    private fun loadLampData() = viewModelScope.launch {
        val lampAddress = uiState.value.currentLampAddress
        val timeout = defaultTimeout

        controlRepository.getEffectsList(lampAddress, timeout)
            .onSuccess { effects ->
                _uiState.update {
                    it.copy(
                        lampConnected = true,
                        lampAvailableEffects = effects
                    )
                }
            }.onFailure {
                _uiState.update {
                    it.copy(lampConnected = false)
                }
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
                _uiState.update {
                    it.copy(
                        lampConnected = false,
                    )
                }
            }
    }

    init {
        loadLampData()
    }

    fun setCurrentLamp(lampAddress: LampAddress) = viewModelScope.launch {
        dataStoreRepository.updatePreferences {
            it.copy(
                latestLampAddress = lampAddress
            )
        }

        loadLampData()
    }

    fun setLampPowerOn(powerOn: Boolean) = viewModelScope.launch {
        val lampAddress = uiState.value.currentLampAddress
        val result = if (powerOn)
            controlRepository.turnOnLamp(
                lampAddress = lampAddress,
                timeout = defaultTimeout
            )
        else
            controlRepository.turnOffLamp(
                lampAddress = lampAddress,
                timeout = defaultTimeout
            )

        result.handleLampStateResponse()
    }

    fun setCycleEnabled(enabled: Boolean) = viewModelScope.launch {
        val lampAddress = uiState.value.currentLampAddress
        favouritesRepository.updateConfig(
            lampAddress = lampAddress,
            timeout = defaultTimeout
        ) {
            it.copy(cycleEnabled = enabled)
        }.handleFavouritesConfigResponse()
    }

    fun setLampEffect(effect: LampEffect) = viewModelScope.launch {
        val lampAddress = uiState.value.currentLampAddress
        controlRepository.setEffect(
            lampAddress = lampAddress,
            timeout = defaultTimeout,
            effectId = effect.id
        ).handleLampStateResponse()
    }

    fun setLampBrightness(brightness: Float) = viewModelScope.launch {
        _uiState.update { it.copy(lampBrightness = brightness) }
        val brightnessInRange = (255 * brightness).toInt().coerceIn(0, 255).toUByte()
        val lampAddress = uiState.value.currentLampAddress
        controlRepository.setBrightness(
            lampAddress = lampAddress,
            timeout = defaultTimeout,
            value = brightnessInRange
        ).handleLampStateResponse()
    }
    fun setLampSpeed(speed: Float) = viewModelScope.launch {
        _uiState.update { it.copy(lampSpeed = speed) }
        val uiStateSnapshot = uiState.value
        val lampAddress = uiStateSnapshot.currentLampAddress
        val currentEffect = uiStateSnapshot.lampCurrentEffect

        val speedRaw = currentEffect.minSpeed.toInt() + speed * (currentEffect.maxSpeed - currentEffect.minSpeed).toInt()

        val speedInRange = speedRaw.toInt().coerceIn(0, 255).toUByte()
        controlRepository.setSpeed(
            lampAddress = lampAddress,
            timeout = defaultTimeout,
            value = speedInRange
        ).handleLampStateResponse()
    }
    fun setLampScale(scale: Float) = viewModelScope.launch {
        _uiState.update { it.copy(lampScale = scale) }
        val uiStateSnapshot = uiState.value
        val lampAddress = uiStateSnapshot.currentLampAddress
        val currentEffect = uiStateSnapshot.lampCurrentEffect

        val scaleRaw = currentEffect.minScale.toInt() + scale * (currentEffect.maxScale - currentEffect.minScale).toInt()

        val scaleInRange = scaleRaw.toInt().coerceIn(0, 255).toUByte()
        controlRepository.setScale(
            lampAddress = lampAddress,
            timeout = defaultTimeout,
            value = scaleInRange
        ).handleLampStateResponse()
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
