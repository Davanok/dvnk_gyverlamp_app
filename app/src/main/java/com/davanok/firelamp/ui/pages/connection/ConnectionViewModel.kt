package com.davanok.firelamp.ui.pages.connection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davanok.firelamp.data.model.LampAddress
import com.davanok.firelamp.data.repositories.DataStoreRepository
import com.davanok.firelamp.data.repositories.LampsFinderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConnectionViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository,
    private val lampsFinderRepository: LampsFinderRepository
) : ViewModel() {
    private val _appConfig = dataStoreRepository.subscribeToPreferences()
    private val _uiState = MutableStateFlow(ConnectionUiState())
    val uiState: StateFlow<ConnectionUiState> = combine(
        _appConfig,
        _uiState
    ) { appConfig, state ->
        state.copy(
            editLampAddress = if (state.editLampAddress == null) null else appConfig.savedLampAddresses.firstOrNull { it.hostname == state.editLampAddress.hostname },
            savedLampsList = appConfig.savedLampAddresses
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ConnectionUiState()
    )

    fun updateLampAddress(address: LampAddress) = viewModelScope.launch {
        dataStoreRepository.updatePreferences { preferences ->
            val savedLamps = preferences.savedLampAddresses.toMutableList()
            val toUpdate = savedLamps.indexOfFirst { it.hostname == address.hostname }
            if (toUpdate >= 0)
                savedLamps[toUpdate] = address
            else
                savedLamps.add(address)

            preferences.copy(savedLampAddresses = savedLamps)
        }
    }
    fun cancelEditLamp() = _uiState.update {
        it.copy(editLampAddress = null)
    }

    fun deleteLampAddress(address: LampAddress) = viewModelScope.launch {
        dataStoreRepository.updatePreferences { preferences ->
            val savedLamps = preferences.savedLampAddresses

            val filtered = savedLamps.filter { it.hostname == address.hostname }

            preferences.copy(savedLampAddresses = filtered)
        }
    }

    fun setCurrentLamp(address: LampAddress) = _uiState.update {
        it.copy(editLampAddress = address)
    }

    fun findLamps() = viewModelScope.launch {
        lampsFinderRepository.findLamps(LampAddress.Unknown.port).collect { (progress, lamps) ->
            _uiState.update {
                it.copy(
                    findLampProgress = progress,
                    foundedLampsList = lamps
                )
            }
        }
        _uiState.update {
            it.copy(findLampProgress = null)
        }
    }

    fun checkConnection(address: LampAddress) = viewModelScope.launch {
        _uiState.update { it.copy(checkConnectionInProgress = true) }

        lampsFinderRepository.checkConnection(address)
            .onFailure {
                _uiState.update {
                    it.copy(
                        checkConnectionInProgress = false,
                        lampConnected = false
                    )
                }
            }
            .onSuccess { address ->
                _uiState.update {
                    it.copy(
                        editLampAddress = address,
                        checkConnectionInProgress = false,
                        lampConnected = address != null
                    )
                }
            }
    }
}

data class ConnectionUiState(
    val editLampAddress: LampAddress? = null,
    val savedLampsList: List<LampAddress> = emptyList(),
    val foundedLampsList: List<LampAddress> = emptyList(),
    val findLampProgress: Float? = null,
    val lampConnected: Boolean? = null,
    val checkConnectionInProgress: Boolean = false
)