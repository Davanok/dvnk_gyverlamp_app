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
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class ConnectionViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository,
    private val lampsFinderRepository: LampsFinderRepository
) : ViewModel() {
    private val _appConfig = dataStoreRepository.subscribeToPreferences()
    private val _uiState = MutableStateFlow(ConnectionUiState())

    private var findTimeout: Duration = 5.seconds

    val uiState: StateFlow<ConnectionUiState> = combine(
        _appConfig,
        _uiState
    ) { appConfig, state ->
        findTimeout = appConfig.findTimeout
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

            preferences.copy(savedLampAddresses = savedLamps.distinctBy { it.hostname })
        }
    }
    fun cancelEditLamp() = _uiState.update {
        it.copy(editLampAddress = null)
    }

    fun deleteLampAddress(address: LampAddress) = viewModelScope.launch {
        dataStoreRepository.updatePreferences { preferences ->
            val savedLamps = preferences.savedLampAddresses

            val filtered = savedLamps.filter { it.hostname != address.hostname }

            preferences.copy(savedLampAddresses = filtered)
        }
    }

    fun setCurrentLamp(address: LampAddress) = _uiState.update {
        it.copy(editLampAddress = address)
    }
    fun saveAndSetCurrentLamp(address: LampAddress) {
        updateLampAddress(address).invokeOnCompletion {
            setCurrentLamp(address)
        }
    }

    fun setSearchPort(port: Int) {
        if (port >= 0)
            _uiState.update { it.copy(searchLampsPort = port) }
    }

    fun findLamps() {
        if (uiState.value.findLampProgress != null) return
        _uiState.update { it.copy(findLampProgress = 0f) }

        viewModelScope.launch {
            lampsFinderRepository.findLamps(uiState.value.searchLampsPort, timeout = findTimeout)
                .collect { (progress, lamps) ->
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
    }

    fun checkConnection(address: LampAddress) = viewModelScope.launch {
        _uiState.update { it.copy(checkConnectionInProgress = true) }

        lampsFinderRepository.checkConnection(address, findTimeout)
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
    val checkConnectionInProgress: Boolean = false,
    val searchLampsPort: Int = LampAddress.Unknown.port
)