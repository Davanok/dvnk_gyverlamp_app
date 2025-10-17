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
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.davanok.firelamp.R
import com.davanok.firelamp.ui.pages.connection.ConnectionScreen
import com.davanok.firelamp.ui.pages.lampControl.lampControl.LampControlScreen
import com.davanok.firelamp.ui.pages.lampControl.lampsList.LampsListScreen

@Composable
private fun DefaultNavigationWrapper(
    backStack: NavBackStack<NavKey>,
    navButtonType: NavButtonType = NavButtonType.Menu,
    title: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    NavigationDrawerScaffold(
        currentRoute = backStack.lastOrNull(),
        navigate = {
            backStack.clear()
            backStack.add(it)
        },
        navigateBack = backStack::removeLastOrNull,
        title = title,
        navButtonType = navButtonType,
        content = content
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationHost() {
    val backStack = rememberNavBackStack(Route.LampControl)
    NavDisplay(
        backStack = backStack,
        entryDecorators = listOf(

        ),
        entryProvider = entryProvider(
            fallback = { NavEntry(Route.LampControl) { LampControlNavModule(backStack) } }
        ) {
            entry<Route.LampControl> { LampControlNavModule(backStack) }
            entry<Route.Connection> {
                DefaultNavigationWrapper(
                    backStack = backStack,
                    title = { Text(text = stringResource(R.string.connection)) },
                ) { ConnectionScreen(onBack = backStack::removeLastOrNull) }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun LampControlNavModule(
    parentBackStack: NavBackStack<NavKey>
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
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
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
                DefaultNavigationWrapper(
                    backStack = parentBackStack,
                    title = { Text(text = stringResource(R.string.lamp_control)) },
                ) {
                    LampsListScreen(
                        onLampChange = { backStack.add(Route.LampControl.Control(it.getName())) }
                    )
                }
            }
            entry<Route.LampControl.Control>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) { key ->
                DefaultNavigationWrapper(
                    backStack = parentBackStack,
                    title = { Text(text = key.lampName) },
                ) { LampControlScreen() }
            }
        }
    )
}