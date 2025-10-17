package com.davanok.firelamp.ui.pages.connection

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davanok.firelamp.R
import com.davanok.firelamp.data.model.LampAddress

@Composable
fun ConnectionScreen(
    onBack: () -> Unit,
    viewModel: ConnectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Content(
        currentLampAddress = uiState.currentLampAddress,
        onLampAddressChange = viewModel::setLampAddress,
        onFindLamps = viewModel::findLamps,
        findLampProgress = uiState.findLampProgress,
        onCheckConnection = viewModel::checkConnection,
        lampConnected = uiState.lampConnected,
        checkConnectionInProgress = uiState.checkConnectionInProgress,
        onSave = viewModel::saveConnection,
        savedLampsList = uiState.savedLampsList,
        onClearLampsList = viewModel::clearLampsList,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
    currentLampAddress: LampAddress,
    onLampAddressChange: (LampAddress) -> Unit,
    onFindLamps: () -> Unit,
    findLampProgress: Float?,
    onCheckConnection: () -> Unit,
    lampConnected: Boolean?,
    checkConnectionInProgress: Boolean,
    onSave: () -> Unit,
    savedLampsList: List<LampAddress>,
    onClearLampsList: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onSave
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.apply_changes)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues)
        ) {
            AddressInputField(
                currentLampAddress = currentLampAddress,
                onLampAddressChange = onLampAddressChange,
                onDone = onSave,
                lampConnected = lampConnected,
                checkConnectionInProgress = checkConnectionInProgress,
                onCheckConnection = onCheckConnection
            )
        }
    }
}

@Composable
private fun AddressInputField(
    currentLampAddress: LampAddress,
    onLampAddressChange: (LampAddress) -> Unit,
    onDone: () -> Unit,
    lampConnected: Boolean?,
    checkConnectionInProgress: Boolean,
    onCheckConnection: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        modifier = modifier,
        value = currentLampAddress.hostname,
        onValueChange = { onLampAddressChange(LampAddress.Unknown.copy(hostname = it)) },
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        trailingIcon = {
            if (checkConnectionInProgress) CircularProgressIndicator()
            else IconButton(
                onClick = onCheckConnection
            ) {
                when(lampConnected) {
                    true -> Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.lamp_connected)
                    )
                    false -> Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = stringResource(R.string.lamp_connection_failed)
                    )
                    null -> Icon(
                        imageVector = Icons.Default.QuestionMark,
                        contentDescription = stringResource(R.string.lamp_connection_unknown)
                    )
                }
            }
        }
    )
}