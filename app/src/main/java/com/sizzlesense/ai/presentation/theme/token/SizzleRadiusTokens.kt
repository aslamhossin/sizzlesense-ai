package com.sizzlesense.ai.presentation.theme.token

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Immutable
data class SizzleRadiusTokens(
    val sm: Shape = RoundedCornerShape(10.dp),
    val md: Shape = RoundedCornerShape(12.dp),
    val lg: Shape = RoundedCornerShape(16.dp)
)

val LocalSizzleRadius = staticCompositionLocalOf { SizzleRadiusTokens() }
