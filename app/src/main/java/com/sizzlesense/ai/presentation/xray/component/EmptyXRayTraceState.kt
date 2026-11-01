package com.sizzlesense.ai.presentation.xray.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.sizzlesense.ai.presentation.theme.SizzleTheme

@Composable
fun EmptyXRayTraceState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Run a session analysis first to inspect the 4-Stage Cognitive Trace.",
            color = SizzleTheme.colors.textSecondary
        )
    }
}
