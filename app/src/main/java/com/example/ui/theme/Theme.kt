package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val StudioDarkColorScheme = darkColorScheme(
    primary = LiveRed,
    onPrimary = TextPrimary,
    primaryContainer = LiveRedDark,
    onPrimaryContainer = TextPrimary,
    secondary = StudioBlue,
    onSecondary = TextPrimary,
    secondaryContainer = StudioCardElevated,
    onSecondaryContainer = StudioCyan,
    tertiary = StudioCyan,
    background = StudioDark,
    onBackground = TextPrimary,
    surface = StudioSurface,
    onSurface = TextPrimary,
    surfaceVariant = StudioCard,
    onSurfaceVariant = TextSecondary,
    outline = StudioBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Always enforce premium studio dark theme for broadcast monitor
    content: @Composable () -> Unit,
) {
    val colorScheme = StudioDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = StudioDark.toArgb()
                window.navigationBarColor = StudioDark.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
