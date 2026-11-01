package com.sizzlesense.ai.presentation.kitchen.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = activePipelineStageLabel,
                style = MaterialTheme.typography.bodySmall
            )
        }
    } else {
        Button(
            onClick = onAnalyzeSession,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null
            )
            Text("  Run 4-Stage Applied AI Coaching (Attempt $currentAttemptNumber)")
        }

        OutlinedButton(
            onClick = onCaptureLiveTapo,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null
            )
            Text("  Grab Live Tapo C100 Frame via Backend & Analyze")
        }
    }
}
