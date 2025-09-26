package com.davanok.firelamp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.davanok.firelamp.ui.pages.lampControl.LampControlScreen

@Composable
fun NavigationHost() {
    val backStack = rememberNavBackStack(Route.LampControl)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Route.LampControl> { LampControlScreen() }
        }
    )
}