package com.davanok.firelamp.ui.pages.lampControl.lampsList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davanok.firelamp.data.model.LampAddress
import com.davanok.firelamp.data.repositories.DataStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LampsListViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {
    private val _appConfig = dataStoreRepository.subscribeToPreferences()
    private val _uiState = MutableStateFlow(LampsListUiState())

    val uiState: StateFlow<LampsListUiState> = combine(
        _appConfig,
        _uiState
    ) { appConfig, state ->
        state.copy(
            currentLampAddress = appConfig.latestLampAddress,
            availableLampAddresses = appConfig.savedLampAddresses
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
}

data class LampsListUiState(
    val currentLampAddress: LampAddress = LampAddress.Hotspot,
    val availableLampAddresses: List<LampAddress> = listOf(LampAddress.Hotspot)
)
