package com.davanok.firelamp.ui.pages.lampControl.lampControl

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davanok.firelamp.R
import com.davanok.firelamp.data.model.LampEffect
import com.davanok.firelamp.ui.pages.lampControl.components.EffectSelector
import com.davanok.firelamp.ui.pages.lampControl.components.LampControlComponent
import com.davanok.firelamp.ui.pages.lampControl.components.LampPowerControl

@Composable
fun LampControlScreen(
    viewModel: LampControlViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Content(
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
        onScaleChange = viewModel::setLampScale,
        onRandomEffectValues = viewModel::setRandomValues,
        onDefaultEffectValues = viewModel::setDefaultValues
    )
}

@Composable
private fun Content(
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
    onRandomEffectValues: () -> Unit,
    onDefaultEffectValues: () -> Unit
) {
    Column {
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            LampControlComponent(
                modifier = Modifier
                    .fillMaxWidth(),
                brightness = brightness,
                speed = speed,
                scale = scale,
                scaleIsColor = scaleIsColor,
                onBrightnessChange = onBrightnessChange,
                onSpeedChange = onSpeedChange,
                onScaleChange = onScaleChange
            )
        }
        Row {
            Button(
                modifier = Modifier.weight(1f),
                onClick = onRandomEffectValues
            ) {
                Text(text = stringResource(R.string.random_effect_values))
            }
            Spacer(Modifier.width(8.dp))
            Button(
                modifier = Modifier.weight(1f),
                onClick = onDefaultEffectValues
            ) {
                Text(text = stringResource(R.string.default_effect_values))
            }
        }
    }
}