package com.sizzlesense.ai.presentation.theme.token

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class SizzleColorTokens(
    val brandPrimary: Color = Color(0xFFFFB300),
    val brandSecondary: Color = Color(0xFFFF6D00),
    val brandPrimaryContainer: Color = Color(0xFF332610),
    val onBrandPrimaryContainer: Color = Color(0xFFFFD56B),
    val accentSuccess: Color = Color(0xFF00E676),
    val accentSuccessContainer: Color = Color(0x1F00E676),
    val statusError: Color = Color(0xFFFF5252),
    val statusErrorContainer: Color = Color(0x29FF5252),
    val brandIconContainer: Color = Color(0x29FFB300),
    val backgroundPrimary: Color = Color(0xFF101318),
    val surfacePrimary: Color = Color(0xFF1A1F27),
    val surfaceElevated: Color = Color(0xFF252C37),
    val outlineDefault: Color = Color(0xFF343C4A),
    val outlineSubtle: Color = Color(0xFF282E3A),
    val videoCanvasBackground: Color = Color(0xFF14181F),
    val textPrimary: Color = Color(0xFFF5F7FA),
    val textSecondary: Color = Color(0xFF9EA7B8),
    val textOnBrand: Color = Color(0xFF101318)
)

val LocalSizzleColors = staticCompositionLocalOf { SizzleColorTokens() }
