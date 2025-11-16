package com.davanok.firelamp.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.davanok.firelamp.R
import com.davanok.firelamp.ui.pages.connection.ConnectionScreen
import com.davanok.firelamp.ui.pages.lampControl.lampControl.LampControlScreen
import com.davanok.firelamp.ui.pages.lampControl.lampsList.LampsListScreen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationHost() {
    val backStack = rememberNavBackStack(Route.LampControl)

    val topLevelNavigate: (NavKey) -> Unit = {
        backStack.clear()
        backStack.addAll(listOf(Route.LampControl, it))
    }
    val navigateBack: () -> Unit = { backStack.removeLastOrNull() }

    NavDisplay(
        backStack = backStack,
        entryProvider = entryProvider(
            fallback = {
                NavEntry(Route.LampControl) {
                    LampControlNavModule(
                        topLevelNavigate,
                        navigateBack
                    )
                }
            }
        ) {
            entry<Route.LampControl> { LampControlNavModule(topLevelNavigate, navigateBack) }
            entry<Route.Connection> {
                NavigationDrawerScaffold(
                    currentRoute = it,
                    navigate = topLevelNavigate,
                    navigateBack = navigateBack,
                    title = { Text(text = stringResource(R.string.connection)) }
                ) { ConnectionScreen() }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun LampControlNavModule(
    navigate: (NavKey) -> Unit,
    navigateBack: () -> Unit
) {
    val backStack = rememberNavBackStack(Route.LampControl.LampsList)
    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    val directive = remember(currentWindowAdaptiveInfo()) {
        calculatePaneScaffoldDirective(windowAdaptiveInfo)
            .copy(horizontalPartitionSpacerSize = 0.dp)
    }
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(directive = directive)

    NavDisplay(
        backStack = backStack,
        sceneStrategy = listDetailStrategy,
        entryProvider = entryProvider {
            entry<Route.LampControl.LampsList>(
                metadata = ListDetailSceneStrategy.listPane(
                    detailPlaceholder = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                modifier = Modifier.fillMaxSize(2f / 3)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = stringResource(R.string.select_lamp))
                                }
                            }
                        }
                    }
                )
            ) {
                NavigationDrawerScaffold(
                    currentRoute = it,
                    navigate = navigate,
                    navigateBack = navigateBack,
                    title = { Text(text = stringResource(R.string.lamp_control)) },
                ) {
                    LampsListScreen(
                        onLampChange = { backStack.add(Route.LampControl.Control(it.getDisplayName())) }
                    )
                }
            }
            entry<Route.LampControl.Control>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) { key ->
                NavigationDrawerScaffold(
                    currentRoute = key,
                    navigate = navigate,
                    navigateBack = navigateBack,
                    title = { Text(text = key.lampName) },
                ) { LampControlScreen() }
            }
        }
    )
}