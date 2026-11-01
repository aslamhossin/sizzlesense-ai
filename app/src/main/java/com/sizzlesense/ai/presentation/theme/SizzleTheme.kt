package com.sizzlesense.ai.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.sizzlesense.ai.presentation.theme.token.LocalSizzleColors
import com.sizzlesense.ai.presentation.theme.token.LocalSizzleRadius
import com.sizzlesense.ai.presentation.theme.token.LocalSizzleSpacing
import com.sizzlesense.ai.presentation.theme.token.SizzleColorTokens
import com.sizzlesense.ai.presentation.theme.token.SizzleRadiusTokens
import com.sizzlesense.ai.presentation.theme.token.SizzleSpacingTokens

/**
 * Unified Design System token accessor backed by Compose [staticCompositionLocalOf] providers.
 */
object SizzleTheme {

    val colors: SizzleColorTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalSizzleColors.current

    val spacing: SizzleSpacingTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalSizzleSpacing.current

    val radius: SizzleRadiusTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalSizzleRadius.current

    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography
}
