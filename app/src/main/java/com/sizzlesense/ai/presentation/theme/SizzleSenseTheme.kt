package com.sizzlesense.ai.presentation.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.sizzlesense.ai.presentation.theme.token.LocalSizzleColors
import com.sizzlesense.ai.presentation.theme.token.LocalSizzleRadius
import com.sizzlesense.ai.presentation.theme.token.LocalSizzleSpacing
import com.sizzlesense.ai.presentation.theme.token.SizzleColorTokens
import com.sizzlesense.ai.presentation.theme.token.SizzleRadiusTokens
import com.sizzlesense.ai.presentation.theme.token.SizzleSpacingTokens

@Composable
fun SizzleSenseTheme(
    colorTokens: SizzleColorTokens = remember { SizzleColorTokens() },
    spacingTokens: SizzleSpacingTokens = remember { SizzleSpacingTokens() },
    radiusTokens: SizzleRadiusTokens = remember { SizzleRadiusTokens() },
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

    val materialColorScheme = remember(colorTokens) {
        darkColorScheme(
            primary = colorTokens.brandPrimary,
            secondary = colorTokens.brandSecondary,
            tertiary = colorTokens.accentSuccess,
            background = colorTokens.backgroundPrimary,
            surface = colorTokens.surfacePrimary,
            surfaceVariant = colorTokens.surfaceElevated,
            onPrimary = colorTokens.textOnBrand,
            onSecondary = colorTokens.textOnBrand,
            onBackground = colorTokens.textPrimary,
            onSurface = colorTokens.textPrimary,
            onSurfaceVariant = colorTokens.textSecondary,
            error = colorTokens.statusError
        )
    }

    CompositionLocalProvider(
        LocalSizzleColors provides colorTokens,
        LocalSizzleSpacing provides spacingTokens,
        LocalSizzleRadius provides radiusTokens
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            content = content
        )
    }
}
