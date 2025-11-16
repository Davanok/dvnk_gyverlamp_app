package com.davanok.firelamp.ui.pages.connection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davanok.firelamp.R
import com.davanok.firelamp.data.model.LampAddress

@Composable
fun ConnectionScreen(
    viewModel: ConnectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Content(
        onFindLamps = viewModel::findLamps,
        findLampProgress = uiState.findLampProgress,
        onCheckConnection = viewModel::checkConnection,
        lampConnected = uiState.lampConnected,
        checkConnectionInProgress = uiState.checkConnectionInProgress,
        foundedLampsList = uiState.foundedLampsList,
        savedLampsList = uiState.savedLampsList,
        onDeleteLamp = viewModel::deleteLampAddress,
        onUpdateLamp = viewModel::updateLampAddress,
        onCancel = viewModel::cancelEditLamp,
        currentLampAddress = uiState.editLampAddress,
        onSavedLampClick = viewModel::setCurrentLamp
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
    currentLampAddress: LampAddress?,
    onSavedLampClick: (LampAddress) -> Unit,
    onFindLamps: () -> Unit,
    findLampProgress: Float?,
    onCheckConnection: (LampAddress) -> Unit,
    lampConnected: Boolean?,
    checkConnectionInProgress: Boolean,
    foundedLampsList: List<LampAddress>,
    savedLampsList: List<LampAddress>,
    onDeleteLamp: (LampAddress) -> Unit,
    onUpdateLamp: (LampAddress) -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (currentLampAddress == null)
            NewAddressInputField(
                lampConnected = lampConnected,
                checkConnectionInProgress = checkConnectionInProgress,
                checkConnection = onCheckConnection,
                addWithoutCheck = onUpdateLamp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        else
            EditLampAddress(
                currentLampAddress = currentLampAddress,
                onUpdate = onUpdateLamp,
                onCancel = onCancel,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

        Spacer(Modifier.height(12.dp))

        LampsListContent(
            onFindLamps = onFindLamps,
            findLampProgress = findLampProgress,
            foundedLampsList = foundedLampsList,
            savedLampsList = savedLampsList,
            onDeleteLamp = onDeleteLamp,
            onClickSavedLamp = onSavedLampClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun NewAddressInputField(
    lampConnected: Boolean?,
    checkConnectionInProgress: Boolean,
    checkConnection: (LampAddress) -> Unit,
    addWithoutCheck: (LampAddress) -> Unit,
    modifier: Modifier = Modifier
) {
    var editLampHost by remember { mutableStateOf(LampAddress.Unknown.hostname) }
    var editLampPort by remember { mutableIntStateOf(LampAddress.Unknown.port) }

    val hostValid by remember(editLampHost) {
        derivedStateOf {
            editLampHost.split('.').let { parts ->
                parts.size == 4 && parts.all { part ->
                    part.toIntOrNull().let { it != null && it in 0..255 }
                }
            }
        }
    }

    val builtLampAddress by remember {
        derivedStateOf {
            val address = LampAddress(
                hostname = editLampHost,
                port = editLampPort
            )
            if (address.isValid()) address else null
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = editLampHost,
            onValueChange = { editLampHost = it },
            keyboardActions = KeyboardActions(onDone = { builtLampAddress?.let { checkConnection(it) } }),
            isError = !hostValid,
            supportingText = if (hostValid) null else { { Text(text = stringResource(R.string.hostname_invalid)) } },
            label = { Text(text = stringResource(R.string.hostname)) },
            singleLine = true
        )

        OutlinedTextField(
            modifier = Modifier.weight(0.5f),
            value = editLampPort.toString(),
            onValueChange = { new -> new.toIntOrNull()?.takeIf { it >= 0 }?.let { editLampPort = it } },
            label = { Text(text = stringResource(R.string.port)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
        ) {
            FilledIconButton(
                onClick = { builtLampAddress?.let { checkConnection(it) } },
                enabled = builtLampAddress != null,
                shape = ButtonGroupDefaults.connectedLeadingButtonShape
            ) {
                if (checkConnectionInProgress) CircularWavyProgressIndicator(modifier = Modifier.size(24.dp))
                else when(lampConnected) {
                    true -> Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.lamp_connected_icon)
                    )
                    false -> Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = stringResource(R.string.lamp_connection_failed_icon)
                    )
                    null -> Icon(
                        imageVector = Icons.Default.QuestionMark,
                        contentDescription = stringResource(R.string.lamp_connection_unknown_icon)
                    )
                }
            }

            FilledIconButton(
                onClick = { builtLampAddress?.let { addWithoutCheck(it) } },
                enabled = builtLampAddress != null,
                shape = ButtonGroupDefaults.connectedTrailingButtonShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
private fun EditLampAddress(
    currentLampAddress: LampAddress,
    onUpdate: (LampAddress) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var editLampName by remember(currentLampAddress) {
        mutableStateOf(currentLampAddress.name)
    }
    Column(modifier = modifier) {
        Text(
            text = "${currentLampAddress.hostname}:${currentLampAddress.port}",
            style = MaterialTheme.typography.titleMedium
        )

        val focusManager = LocalFocusManager.current

        val onDone = {
            focusManager.clearFocus()
            onUpdate(currentLampAddress.copy(name = editLampName))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = editLampName,
                onValueChange = { editLampName = it },
                label = { Text(text = stringResource(R.string.lamp_name)) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onDone() }),
                singleLine = true
            )

            IconButton(
                onClick = onCancel
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.cancel_edit_lamp)
                )
            }

            AnimatedVisibility(
                visible = currentLampAddress.name != editLampName
            ) {
                IconButton(
                    onClick = onDone
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.update_name)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LampsListContent(
    onFindLamps: () -> Unit,
    findLampProgress: Float?,
    foundedLampsList: List<LampAddress>,
    savedLampsList: List<LampAddress>,
    onDeleteLamp: (LampAddress) -> Unit,
    onClickSavedLamp: (LampAddress) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
    ) {
        stickyHeader {
            FoundedLampsHeader(
                onFindLamps = onFindLamps,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            if (findLampProgress != null)
                LinearWavyProgressIndicator(
                    progress = { findLampProgress },
                    modifier = Modifier.fillMaxWidth()
                )
        }
        if (foundedLampsList.isEmpty())
            item {
                ListItem(
                    headlineContent = {
                        Text(text = stringResource(R.string.no_lamps_found),)
                    }
                )
            }
        else
            items(
                items = foundedLampsList,
                key = { "founded: ${it.hostname}" }
            ) { address ->
                ListItem(
                    headlineContent = {
                        Text(text = address.getDisplayName())
                    }
                )
                HorizontalDivider(Modifier.fillMaxWidth())
            }

        stickyHeader {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = stringResource(R.string.saved_lamps),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                )
            }
        }

        items(
            items = savedLampsList,
            key = { "saved: ${it.hostname}" }
        ) { address ->
            SavedLampListItem(
                address = address,
                onDeleteLamp = onDeleteLamp,
                onClick = onClickSavedLamp,
                modifier = Modifier.animateItem()
            )
            HorizontalDivider(Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun FoundedLampsHeader(
    onFindLamps: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.founded_lamps),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onFindLamps
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.find_lamps)
                )
            }
        }
    }
}

@Composable
private fun SavedLampListItem(
    address: LampAddress,
    onDeleteLamp: (LampAddress) -> Unit,
    onClick: (LampAddress) -> Unit,
    modifier: Modifier = Modifier
) {
    SwipeToDismissBox(
        modifier = modifier,
        state = rememberSwipeToDismissBoxState(),
        backgroundContent = {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(2) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete_lamp)
                    )
                }
            }
        },
        onDismiss = { onDeleteLamp(address) }
    ) {
        ListItem(
            modifier = Modifier.clickable { onClick(address) },
            headlineContent = {
                Text(text = address.getDisplayName())
            },
            trailingContent = {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.edit_lamp)
                )
            }
        )
    }
}