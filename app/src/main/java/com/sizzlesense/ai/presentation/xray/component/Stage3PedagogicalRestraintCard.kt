package com.sizzlesense.ai.presentation.xray.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterCenterFocus
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.sizzlesense.ai.domain.model.coaching.ExpertTeachingDecision
import com.sizzlesense.ai.presentation.common.component.LabeledDetailRow
import com.sizzlesense.ai.presentation.common.component.SizzleSectionCard

@Composable
fun Stage3PedagogicalRestraintCard(
    stage3: ExpertTeachingDecision,
    showSuppressedIssues: Boolean
) {
    SizzleSectionCard(
        title = "Stage 3: Pedagogical Restraint & Omitted Issues",
        subtitle = "Chef Causal Priority Graph",
        icon = Icons.Default.FilterCenterFocus
    ) {
        LabeledDetailRow(
            label = "Selected Primary: ${stage3.selectedPrimaryIssue}",
            value = stage3.causalPriorityRationale
        )
        if (showSuppressedIssues) {
            Text(
                text = "Deliberately Suppressed Secondary Issues:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            stage3.deliberatelyOmittedIssues.forEach { omitted ->
                Text(
                    text = "• ${omitted.issue} — ${omitted.suppressionReason}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
