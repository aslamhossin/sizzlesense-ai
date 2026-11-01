package com.sizzlesense.ai.presentation.xray.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.sizzlesense.ai.domain.model.LearnerStateEstimate
import com.sizzlesense.ai.presentation.components.LabeledDetailRow
import com.sizzlesense.ai.presentation.components.SizzleSectionCard

@Composable
fun Stage2LearnerLatentStateCard(
    reasoningModelUsed: String,
    stage2: LearnerStateEstimate
) {
    SizzleSectionCard(
        title = "Stage 2: Learner Latent State Estimation",
        subtitle = "Model: $reasoningModelUsed",
        icon = Icons.Default.Psychology
    ) {
        Text(
            text = "Inferred Archetype: ${stage2.learnerArchetypeId}",
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Transfer Status: ${stage2.transferStatus} (Verified Shift: ${stage2.conceptualShiftVerified})",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.tertiary
        )
        LabeledDetailRow(
            label = "Decision Root Cause:",
            value = stage2.decisionRootCause
        )
        LabeledDetailRow(
            label = "Longitudinal Delta:",
            value = stage2.longitudinalDeltaSummary
        )
    }
}
