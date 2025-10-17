package com.davanok.firelamp.ui.pages.lampControl.lampsList

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davanok.firelamp.data.model.LampAddress


@Composable
fun LampsListScreen(
    viewModel: LampsListViewModel = hiltViewModel(),
    onLampChange: (LampAddress) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.availableLampAddresses) {
        if (uiState.availableLampAddresses.size == 1) {
            val lamp = uiState.availableLampAddresses.first()
            viewModel.setCurrentLamp(lamp)
            onLampChange(lamp)
        }
    }

    Content(
        currentLampAddress = uiState.currentLampAddress,
        availableLampAddresses = uiState.availableLampAddresses,
        onLampChange = {
            viewModel.setCurrentLamp(it)
            onLampChange(it)
        }
    )
}

@Composable
private fun Content(
    currentLampAddress: LampAddress,
    availableLampAddresses: List<LampAddress>,
    onLampChange: (LampAddress) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = availableLampAddresses,
            key = { it.hostname }
        ) { lampAddress ->
            HorizontalDivider(Modifier.fillMaxWidth())
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = currentLampAddress == lampAddress,
                        onClick = { onLampChange(lampAddress) }
                    ),
                headlineContent = { Text(text = lampAddress.getName()) }
            )
        }
    }
}

