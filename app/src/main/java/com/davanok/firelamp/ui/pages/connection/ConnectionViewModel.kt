package com.davanok.firelamp.ui.pages.connection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davanok.firelamp.data.model.LampAddress
import com.davanok.firelamp.data.repositories.DataStoreRepository
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

@HiltViewModel
class ConnectionViewModel @Inject constructor(
    private val controlRepository: LampControlRepository,
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {
    private val _appConfig = dataStoreRepository.subscribeToPreferences()
    private val _uiState = MutableStateFlow(ConnectionUiState())

    val uiState: StateFlow<ConnectionUiState> = combine(
        _appConfig,
        _uiState
    ) { appConfig, state ->
        state.copy(
            currentLampAddress = state.currentLampAddress,
            savedLampsList = appConfig.savedLampAddresses
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ConnectionUiState()
    )

    fun setLampAddress(address: LampAddress) {
        _uiState.update {
            it.copy(
                currentLampAddress = address
            )
        }
    }

    fun saveConnection() = viewModelScope.launch {
        val uiStateSnapshot = _uiState.value
        dataStoreRepository.updatePreferences {
            it.copy(
                latestLampAddress = uiStateSnapshot.currentLampAddress,
                savedLampAddresses = uiStateSnapshot.savedLampsList + uiStateSnapshot.currentLampAddress
            )
        }
    }

    fun findLamps() = viewModelScope.launch {

    }

    fun checkConnection() = viewModelScope.launch {

    }

    fun clearLampsList() = viewModelScope.launch {

    }

    init {
        viewModelScope.launch {
            _appConfig.lastOrNull()?.let { appConfig ->
                _uiState.update {
                    it.copy(
                        currentLampAddress = appConfig.latestLampAddress
                    )
                }
            }
        }
    }
}

data class ConnectionUiState(
    val currentLampAddress: LampAddress = LampAddress.Hotspot,
    val savedLampsList: List<LampAddress> = listOf(LampAddress.Hotspot),
    val findLampProgress: Float? = null,
    val lampConnected: Boolean? = null,
    val checkConnectionInProgress: Boolean = false
)