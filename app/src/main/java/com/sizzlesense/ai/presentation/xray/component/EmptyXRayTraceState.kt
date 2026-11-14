package com.sizzlesense.ai.presentation.xray.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.sizzlesense.ai.presentation.common.component.SizzleSectionCard
import com.sizzlesense.ai.presentation.theme.SizzleTheme

@Composable
fun EmptyXRayTraceState() {
    SizzleSectionCard(title = "") {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = SizzleTheme.spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SizzleTheme.spacing.sm)
        ) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = null,
                tint = SizzleTheme.colors.brandPrimary,
                modifier = Modifier.size(SizzleTheme.spacing.xxl)
            )
            Text(
                text = "No analysis trace yet",
                style = SizzleTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = SizzleTheme.colors.textPrimary
            )
            Text(
                text = "Analyze an attempt in Kitchen or Coach to inspect how the AI evaluated your pan.",
                style = SizzleTheme.typography.bodySmall,
                color = SizzleTheme.colors.textSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}
