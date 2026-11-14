package com.sizzlesense.ai.presentation.kitchen.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.sizzlesense.ai.presentation.theme.SizzleTheme

@Composable
fun CoachingActionButtons(
    isAnalyzing: Boolean,
    activePipelineStageLabel: String,
    currentAttemptNumber: Int,
    onAnalyzeSession: () -> Unit,
    onCaptureLiveTapo: () -> Unit
) {
    if (isAnalyzing) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SizzleTheme.spacing.md)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(SizzleTheme.spacing.iconMedium)
            )
            Text(
                text = activePipelineStageLabel,
                style = SizzleTheme.typography.bodySmall,
                color = SizzleTheme.colors.textSecondary
            )
        }
    } else {
        Button(
            onClick = onAnalyzeSession,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(SizzleTheme.spacing.iconSmall)
            )
            Spacer(modifier = Modifier.width(SizzleTheme.spacing.xs))
            Text(
                text = "Get Coaching • Attempt $currentAttemptNumber",
                maxLines = 1
            )
        }

        OutlinedButton(
            onClick = onCaptureLiveTapo,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(SizzleTheme.spacing.iconSmall)
            )
            Spacer(modifier = Modifier.width(SizzleTheme.spacing.xs))
            Text(
                text = "Snap Camera & Coach",
                maxLines = 1
            )
        }
    }
}
