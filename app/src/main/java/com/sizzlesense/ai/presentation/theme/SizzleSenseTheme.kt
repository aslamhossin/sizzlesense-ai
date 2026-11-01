package com.sizzlesense.ai.presentation.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SizzleDarkColorScheme = darkColorScheme(
    primary = SizzleAmber,
    secondary = SizzleCopper,
    tertiary = SizzleEmerald,
    background = KitchenDarkBackground,
    surface = KitchenDarkSurface,
    surfaceVariant = KitchenDarkSurfaceVariant,
    onPrimary = KitchenDarkBackground,
    onSecondary = KitchenDarkBackground,
    onBackground = KitchenLightText,
    onSurface = KitchenLightText,
    onSurfaceVariant = KitchenMutedText,
    error = SizzleCrimson
)

@Composable
fun SizzleSenseTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = SizzleDarkColorScheme,
        content = content
    )
}
