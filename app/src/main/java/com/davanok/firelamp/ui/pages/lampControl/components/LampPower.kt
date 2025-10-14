package com.davanok.firelamp.ui.pages.lampControl.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.davanok.firelamp.R
import com.davanok.firelamp.ui.components.LabeledSwitch


@Composable
fun LampPowerControl(
    lampPowerOn: Boolean,
    onPowerOnChange: (Boolean) -> Unit,
    cycleEnabled: Boolean,
    onCycleEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row (
        modifier = modifier
    ) {
        LabeledSwitch(
            label = { Text(text = stringResource(R.string.lamp_power)) },
            checked = lampPowerOn,
            onCheckedChange = onPowerOnChange
        )
        LabeledSwitch(
            label = { Text(text = stringResource(R.string.cycle_state)) },
            checked = cycleEnabled,
            onCheckedChange = onCycleEnabledChange
        )
    }
}