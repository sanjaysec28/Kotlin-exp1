package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MiniPrimaryDark,
    onPrimary = MiniOnPrimaryDark,
    secondary = SkyBlueSecondary,
    tertiary = SunGold,
    background = MiniBackgroundDark,
    surface = MiniSurfaceDark,
    onBackground = MiniOnSurfaceDark,
    onSurface = MiniOnSurfaceDark
)

private val LightColorScheme = lightColorScheme(
    primary = MiniPrimaryLight,
    onPrimary = MiniOnPrimaryLight,
    primaryContainer = MiniPrimaryContainerLight,
    onPrimaryContainer = MiniOnPrimaryContainerLight,
    secondary = MiniSecondaryLight,
    onSecondary = MiniOnSecondaryLight,
    tertiary = SunGold,
    background = MiniBackgroundLight,
    surface = MiniSurfaceLight,
    onBackground = MiniOnSurfaceLight,
    onSurface = MiniOnSurfaceLight
)

@Composable
fun MiniWeatherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = MiniWeatherTheme(darkTheme, dynamicColor, content)
