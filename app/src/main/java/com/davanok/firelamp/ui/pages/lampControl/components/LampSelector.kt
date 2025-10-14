package com.davanok.firelamp.ui.pages.lampControl.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastForEach
import com.davanok.firelamp.data.model.LampAddress


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LampSelector(
    currentLampAddress: LampAddress,
    availableLampAddresses: List<LampAddress>,
    onLampChange: (LampAddress) -> Unit,
    modifier: Modifier = Modifier
) {
    var dropdownMenuExpanded by remember { mutableStateOf(false) }

    Box {
        AssistChip(
            modifier = modifier,
            onClick = { dropdownMenuExpanded = !dropdownMenuExpanded },
            label = { Text(text = currentLampAddress.hostname) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(dropdownMenuExpanded)
            }
        )

        DropdownMenu(
            expanded = dropdownMenuExpanded,
            onDismissRequest = { dropdownMenuExpanded = false }
        ) {
            availableLampAddresses.fastForEach { item ->
                DropdownMenuItem(
                    text = { Text(text = item.hostname) },
                    onClick = {
                        onLampChange(item)
                        dropdownMenuExpanded = false
                    }
                )
            }
        }
    }
}