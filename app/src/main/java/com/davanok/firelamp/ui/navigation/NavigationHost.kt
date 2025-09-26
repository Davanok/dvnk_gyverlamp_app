package com.davanok.firelamp.ui.navigation

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFirst
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.scene.rememberSceneSetupNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.davanok.firelamp.R
import com.davanok.firelamp.ui.pages.lampControl.LampControlScreen
import kotlinx.coroutines.launch

enum class MenuNavigation(
    @StringRes val title: Int,
    val icon: ImageVector,
    val route: NavKey
) {
    LampControl(R.string.lamp_control, Icons.Default.Home, Route.LampControl),
    Connection(R.string.connection, Icons.Default.Wifi, Route.Connection),
    AppSettings(R.string.app_settings, Icons.Default.Settings, Route.AppSettings),
    EffectsSettings(R.string.effects_settings, Icons.Default.Animation, Route.EffectsSettings),
    CycleSettings(R.string.cycle_settings, Icons.Default.Autorenew, Route.CycleSettings),
    AlarmSettings(R.string.alarm_settings, Icons.Default.Alarm, Route.AlarmSettings),
    TurnOffTimer(R.string.turn_off_timer, Icons.Default.Timer, Route.TurnOffTimer),
    Drawing(R.string.drawing, Icons.Default.Timer, Route.Drawing),
    Language(R.string.language, Icons.Default.Language, Route.Language)
}
private val TopLevelRoutes = MenuNavigation.entries.fastMap { it.route }.toSet()


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DefaultNavigationDrawerScaffold(
    currentRoute: NavKey?,
    navigate: (NavKey) -> Unit,
    title: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.app_name),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleLarge
                    )
                    HorizontalDivider()
                    Spacer(Modifier.height(16.dp))

                    MenuNavigation.entries.fastForEach { navigation ->
                        NavigationDrawerItem(
                            label = { Text(stringResource(navigation.title)) },
                            selected = currentRoute == navigation.route,
                            onClick = {
                                navigate(navigation.route)
                                scope.launch { drawerState.close() }
                                      },
                            icon = {
                                Icon(
                                    imageVector = navigation.icon,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    drawerState.apply { if (isClosed) open() else close() }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = stringResource(R.string.menu)
                            )
                        }
                    },
                    title = title
                )
            },
            content = { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    content()
                }
            }
        )
    }
}


@Composable
fun NavigationHost() {
    val backStack = rememberNavBackStack(Route.LampControl)

    DefaultNavigationDrawerScaffold(
        currentRoute = backStack.lastOrNull(),
        title = {
            backStack.fastFirstOrNull { it in TopLevelRoutes }?.let { route ->
                Text(stringResource(MenuNavigation.entries.fastFirst { it.route == route }.title))
            }
        },
        navigate = { route ->
            if (route in TopLevelRoutes) backStack.clear()

            backStack.add(route)
        }
    ) {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryDecorators = listOf(
                rememberSceneSetupNavEntryDecorator(),
                rememberSavedStateNavEntryDecorator(),
            ),
            entryProvider = entryProvider(
                fallback = { NavEntry(Route.LampControl) { LampControlScreen() } }
            ) {
                entry<Route.LampControl> { LampControlScreen() }
            }
        )
    }
}