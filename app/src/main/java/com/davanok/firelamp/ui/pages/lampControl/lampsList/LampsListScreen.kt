package com.davanok.firelamp.ui.pages.lampControl.lampsList

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SignalWifiConnectedNoInternet4
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davanok.firelamp.R
import com.davanok.firelamp.data.model.LampAddress


@Composable
fun LampsListScreen(
    viewModel: LampsListViewModel = hiltViewModel(),
    onSingleLamp: (LampAddress) -> Unit,
    onLampChange: (LampAddress) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.availableLampAddresses) {
        if (uiState.availableLampAddresses.size == 1) {
            val lamp = uiState.availableLampAddresses.first()
            viewModel.setCurrentLamp(lamp.address)
            onSingleLamp(lamp.address)
        }
    }

    Content(
        currentLampAddress = uiState.currentLampAddress,
        availableLamps = uiState.availableLampAddresses,
        onLampChange = {
            viewModel.setCurrentLamp(it)
            onLampChange(it)
        },
        setLampPower = viewModel::setLampPower,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun Content(
    currentLampAddress: LampAddress,
    availableLamps: List<LampsListUiState.ListLampState>,
    onLampChange: (LampAddress) -> Unit,
    setLampPower: (LampAddress, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(
            items = availableLamps,
            key = { it.address.hostname }
        ) { lamp ->
            HorizontalDivider(Modifier.fillMaxWidth())
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = currentLampAddress == lamp,
                        onClick = { onLampChange(lamp.address) }
                    ),
                headlineContent = { Text(text = lamp.address.getDisplayName()) },
                trailingContent = {
                    if (!lamp.connected)
                        Icon(
                            imageVector = Icons.Default.SignalWifiConnectedNoInternet4,
                            contentDescription = stringResource(R.string.lamp_connection_failed)
                        )
                    else Switch(
                        checked = lamp.isPowerOn,
                        onCheckedChange = { setLampPower(lamp.address, it) }
                    )
                }
            )
        }
    }
}

