package com.davanok.firelamp.ui.pages.lampControl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davanok.firelamp.data.model.LampAddress
import com.davanok.firelamp.data.model.LampEffect
import com.davanok.firelamp.ui.pages.lampControl.components.EffectSelector
import com.davanok.firelamp.ui.pages.lampControl.components.LampControlComponent
import com.davanok.firelamp.ui.pages.lampControl.components.LampPowerControl
import com.davanok.firelamp.ui.pages.lampControl.components.LampSelector


@Composable
fun LampControlScreen(
    viewModel: LampControlViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Content(
        currentLampAddress = uiState.currentLampAddress,
        availableLampAddresses = uiState.availableLampAddresses,
        onLampChange = viewModel::setCurrentLamp,
        lampPowerOn = uiState.lampPowerOn,
        onPowerOnChange = viewModel::setLampPowerOn,
        cycleEnabled = uiState.lampCycleEnabled,
        onCycleEnabledChange = viewModel::setCycleEnabled,
        currentEffect = uiState.lampCurrentEffect,
        availableEffects = uiState.lampAvailableEffects,
        onEffectChange = viewModel::setLampEffect,
        brightness = uiState.lampBrightness,
        speed = uiState.lampSpeed,
        scale = uiState.lampScale,
        scaleIsColor = uiState.scaleIsColor,
        onBrightnessChange = viewModel::setLampBrightness,
        onSpeedChange = viewModel::setLampSpeed,
        onScaleChange = viewModel::setLampScale
    )
}

@Composable
private fun Content(
    currentLampAddress: LampAddress,
    availableLampAddresses: List<LampAddress>,
    onLampChange: (LampAddress) -> Unit,
    lampPowerOn: Boolean,
    onPowerOnChange: (Boolean) -> Unit,
    cycleEnabled: Boolean,
    onCycleEnabledChange: (Boolean) -> Unit,
    currentEffect: LampEffect,
    availableEffects: List<LampEffect>,
    onEffectChange: (LampEffect) -> Unit,
    brightness: Float,
    speed: Float,
    scale: Float,
    scaleIsColor: Boolean,
    onBrightnessChange: (Float) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onScaleChange: (Float) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LampSelector(
            currentLampAddress = currentLampAddress,
            availableLampAddresses = availableLampAddresses,
            onLampChange = onLampChange
        )
        LampPowerControl(
            lampPowerOn = lampPowerOn,
            onPowerOnChange = onPowerOnChange,
            cycleEnabled = cycleEnabled,
            onCycleEnabledChange = onCycleEnabledChange
        )

        EffectSelector(
            currentEffect = currentEffect,
            availableEffects = availableEffects,
            onEffectChange = onEffectChange
        )

        LampControlComponent(
            brightness = brightness,
            speed = speed,
            scale = scale,
            scaleIsColor = scaleIsColor,
            onBrightnessChange = onBrightnessChange,
            onSpeedChange = onSpeedChange,
            onScaleChange = onScaleChange,
            modifier = Modifier.fillMaxWidth().weight(1f)
        )
    }
}