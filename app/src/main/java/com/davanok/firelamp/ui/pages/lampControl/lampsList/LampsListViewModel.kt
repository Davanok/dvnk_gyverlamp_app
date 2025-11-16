package com.davanok.firelamp.ui.pages.lampControl.lampsList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davanok.firelamp.data.model.LampAddress
import com.davanok.firelamp.data.model.LampState
import com.davanok.firelamp.data.repositories.DataStoreRepository
import com.davanok.firelamp.data.repositories.LampControlRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class LampsListViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository,
    private val controlRepository: LampControlRepository
) : ViewModel() {
    private val _appConfig = dataStoreRepository.subscribeToPreferences()
    private var defaultTimeout: Duration = 1.seconds
    private val _uiState = MutableStateFlow(LampsListUiState())
    private val _lampStates = MutableStateFlow(emptyMap<LampAddress, LampState?>())

    private fun updateLampState(lampAddress: LampAddress, state: LampState?) =
        _lampStates.update { it + (lampAddress to state) }

    val uiState: StateFlow<LampsListUiState> = combine(
        _appConfig,
        _uiState,
        _lampStates
    ) { appConfig, uiState, lampStates ->
        defaultTimeout = appConfig.defaultTimeout
        uiState.copy(
            currentLampAddress = appConfig.latestLampAddress,
            availableLampAddresses = appConfig.savedLampAddresses.map { address ->
                val state = lampStates[address]
                LampsListUiState.ListLampState(
                    address = address,
                    connected = state != null,
                    isPowerOn = state?.powerOn == true
                )
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LampsListUiState()
    )

    fun setCurrentLamp(lampAddress: LampAddress) = viewModelScope.launch {
        dataStoreRepository.updatePreferences {
            it.copy(latestLampAddress = lampAddress)
        }
    }

    fun setLampPower(lampAddress: LampAddress, setPowerOn: Boolean) = viewModelScope.launch {
        val state = controlRepository.setLampPowerOn(lampAddress, defaultTimeout, setPowerOn).getOrNull()
        updateLampState(lampAddress, state)
    }

    init {
        viewModelScope.launch {
            _appConfig
                .map { it.savedLampAddresses }
                .distinctUntilChanged()
                .collect { addresses ->
                    addresses.forEach { address ->
                        launch {
                            val state = controlRepository
                                .getLampState(address, defaultTimeout)
                                .getOrNull()
                            updateLampState(address, state)
                        }
                    }
                }
        }
    }
}

data class LampsListUiState(
    val currentLampAddress: LampAddress = LampAddress.Unknown,
    val availableLampAddresses: List<ListLampState> = emptyList()
) {
    data class ListLampState(
        val address: LampAddress,
        val connected: Boolean,
        val isPowerOn: Boolean
    )
}
