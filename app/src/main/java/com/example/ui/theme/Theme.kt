package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.data.SettingsRepository

private val LightColorScheme = lightColorScheme(
    primary = StaticAccentColor,
    secondary = StaticAccentColor,
    tertiary = StaticAccentColor,
    background = StaticBackgroundWavve,
    surface = StaticSurfaceWavve,
    onPrimary = StaticSurfaceWavve,
    onSecondary = StaticSurfaceWavve,
    onTertiary = StaticSurfaceWavve,
    onBackground = StaticPrimaryText,
    onSurface = StaticPrimaryText,
    onSurfaceVariant = StaticSecondaryText,
    outlineVariant = StaticDividerColor
)

private val DarkColorScheme = darkColorScheme(
    primary = StaticAccentColor,
    secondary = StaticAccentColor,
    tertiary = StaticAccentColor,
    background = DarkBackgroundWavve,
    surface = DarkSurfaceWavve,
    onPrimary = DarkSurfaceWavve,
    onSecondary = DarkSurfaceWavve,
    onTertiary = DarkSurfaceWavve,
    onBackground = DarkPrimaryText,
    onSurface = DarkPrimaryText,
    onSurfaceVariant = DarkSecondaryText,
    outlineVariant = DarkDividerColor
)

@Composable
fun MyApplicationTheme(
    settingsRepository: SettingsRepository? = null,
    content: @Composable () -> Unit,
) {
    val themeSetting = settingsRepository?.theme?.collectAsState(initial = "System")?.value ?: "System"
    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    
    val useDarkTheme = when (themeSetting) {
        "Dark" -> true
        "Light" -> false
        else -> isSystemDark
    }

    val colorScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = androidx.compose.ui.graphics.Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !useDarkTheme
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
