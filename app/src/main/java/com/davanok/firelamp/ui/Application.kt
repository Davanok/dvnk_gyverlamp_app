package com.davanok.firelamp.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.davanok.firelamp.ui.navigation.NavigationHost

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Application(
    colorScheme: ColorScheme
) {
    MaterialExpressiveTheme(
        colorScheme = colorScheme
    ) {
        Surface {
            NavigationHost()
        }
    }
}