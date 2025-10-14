package com.davanok.firelamp.ui.pages.lampControl.components

import androidx.annotation.FloatRange
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.davanok.firelamp.R

@Composable
fun LampControlComponent(
    @FloatRange(0.0, 1.0) brightness: Float,
    @FloatRange(0.0, 1.0) speed: Float,
    @FloatRange(0.0, 1.0) scale: Float,
    scaleIsColor: Boolean,
    onBrightnessChange: (Float) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onScaleChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        LampControlItem(
            title = { Text(text = stringResource(R.string.brightness)) },
            value = brightness,
            onValueChange = onBrightnessChange,
            modifier = Modifier.fillMaxWidth()
        )
        LampControlItem(
            title = { Text(text = stringResource(R.string.speed)) },
            value = speed,
            onValueChange = onSpeedChange,
            modifier = Modifier.fillMaxWidth()
        )
        LampControlItem(
            title = {
                Text(
                    text = if (scaleIsColor)
                        stringResource(R.string.color)
                    else
                        stringResource(R.string.scale)
                )
            },
            value = scale,
            onValueChange = onScaleChange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LampControlItem(
    title: @Composable () -> Unit,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        title()

        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}