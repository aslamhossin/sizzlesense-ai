package com.sizzlesense.ai.presentation.xray.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterCenterFocus
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.sizzlesense.ai.domain.model.coaching.ExpertTeachingDecision
import com.sizzlesense.ai.presentation.common.component.LabeledDetailRow
import com.sizzlesense.ai.presentation.common.component.SizzleSectionCard
import com.sizzlesense.ai.presentation.theme.SizzleTheme

@Composable
fun Stage3PedagogicalRestraintCard(
    stage3: ExpertTeachingDecision,
    showSuppressedIssues: Boolean,
    onToggleSuppressedIssues: (Boolean) -> Unit
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Show Deliberately Suppressed Issues (${stage3.deliberatelyOmittedIssues.size})",
                style = SizzleTheme.typography.labelMedium,
                color = SizzleTheme.colors.brandPrimary
            )
            Switch(
                checked = showSuppressedIssues,
                onCheckedChange = onToggleSuppressedIssues
            )
        }
        if (showSuppressedIssues) {
            stage3.deliberatelyOmittedIssues.forEach { omitted ->
                Text(
                    text = "• ${omitted.issue} — ${omitted.suppressionReason}",
                    style = SizzleTheme.typography.bodySmall
                )
            }
        }
    }
}
