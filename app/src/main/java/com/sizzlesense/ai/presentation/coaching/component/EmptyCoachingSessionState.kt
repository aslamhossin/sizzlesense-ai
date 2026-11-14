package com.sizzlesense.ai.presentation.coaching.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
fun EmptyCoachingSessionState(
    currentAttemptNumber: Int,
    isAnalyzing: Boolean,
    onAnalyzeCurrentAttempt: () -> Unit
) {
    SizzleSectionCard(title = "") {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = SizzleTheme.spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SizzleTheme.spacing.sm)
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = SizzleTheme.colors.brandPrimary,
                modifier = Modifier.size(SizzleTheme.spacing.xxl)
            )
            Text(
                text = "No coaching yet",
                style = SizzleTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = SizzleTheme.colors.textPrimary
            )
            Text(
                text = "Analyze your pan session to get one focused fix and sensory cues.",
                style = SizzleTheme.typography.bodySmall,
                color = SizzleTheme.colors.textSecondary,
                textAlign = TextAlign.Center
            )
            if (isAnalyzing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(SizzleTheme.spacing.iconMedium)
                )
            } else {
                Button(onClick = onAnalyzeCurrentAttempt) {
                    Text("Analyze Attempt $currentAttemptNumber")
                }
            }
        }
    }
}
