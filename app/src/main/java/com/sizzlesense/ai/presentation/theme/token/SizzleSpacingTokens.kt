package com.sizzlesense.ai.presentation.theme.token

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class SizzleSpacingTokens(
    val xxs: Dp = 4.dp,
    val xs: Dp = 8.dp,
    val sm: Dp = 10.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 20.dp,
    val xxl: Dp = 24.dp,
    val borderStroke: Dp = 1.dp,
    val iconSmall: Dp = 20.dp,
    val iconMedium: Dp = 22.dp,
    val topBarLogoBox: Dp = 38.dp,
    val videoPreviewHeight: Dp = 210.dp
)

val LocalSizzleSpacing = staticCompositionLocalOf { SizzleSpacingTokens() }
