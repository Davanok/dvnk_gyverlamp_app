package com.davanok.firelamp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import com.davanok.firelamp.ui.Application
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val dynamicColorsEnabled =  Build.VERSION.SDK_INT > 31
            val isDarkColors = isSystemInDarkTheme()

            val colorScheme =
                if (dynamicColorsEnabled) {
                    if (isDarkColors) dynamicDarkColorScheme(this)
                    else dynamicLightColorScheme(this)
                }
                else {
                    if (isDarkColors) darkColorScheme()
                    else expressiveLightColorScheme()
                }

            Application(colorScheme = colorScheme)
        }
    }
}