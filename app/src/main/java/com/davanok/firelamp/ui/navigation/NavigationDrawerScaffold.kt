package com.davanok.firelamp.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.DrawerState
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
import androidx.compose.ui.util.fastForEach
import androidx.navigation3.runtime.NavKey
import com.davanok.firelamp.R
import kotlinx.coroutines.launch


private enum class MenuNavigation(
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

enum class NavButtonType {
    Menu, Back, None
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawerScaffold(
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    currentRoute: NavKey?,
    navigate: (NavKey) -> Unit,
    navigateBack: () -> Unit,
    title: @Composable () -> Unit,
    navButtonType: NavButtonType = NavButtonType.Menu,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
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
                    title = title,
                    navigationIcon = {
                        when(navButtonType) {
                            NavButtonType.Menu -> IconButton(
                                onClick = {
                                    scope.launch {
                                        if (drawerState.isClosed)
                                            drawerState.open()
                                        else
                                            drawerState.close()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = stringResource(R.string.menu)
                                )
                            }
                            NavButtonType.Back -> IconButton(
                                onClick = navigateBack
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                    contentDescription = stringResource(R.string.navigate_back)
                                )
                            }
                            NavButtonType.None -> {}
                        }
                    }
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