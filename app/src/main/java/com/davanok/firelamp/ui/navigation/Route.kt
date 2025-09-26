package com.davanok.firelamp.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
object Route: NavKey {
    @Serializable data object LampControl: NavKey
    @Serializable data object Connection: NavKey
    @Serializable data object AppSettings: NavKey
    @Serializable data object EffectsSettings: NavKey
    @Serializable data object CycleSettings: NavKey
    @Serializable data object AlarmSettings: NavKey
    @Serializable data object TurnOffTimer: NavKey
    @Serializable data object Drawing: NavKey
    @Serializable data object Language: NavKey
}