package com.sizzlesense.ai.presentation.xray.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.sizzlesense.ai.domain.model.coaching.LearnerStateEstimate
import com.sizzlesense.ai.presentation.common.component.LabeledDetailRow
import com.sizzlesense.ai.presentation.common.component.SizzleSectionCard
import com.sizzlesense.ai.presentation.common.format.DisplayLabelFormatter
import com.sizzlesense.ai.presentation.theme.SizzleTheme

@Composable
fun Stage2LearnerLatentStateCard(
    reasoningModelUsed: String,
    stage2: LearnerStateEstimate
) {
    val statusLabel = DisplayLabelFormatter.formatTransferStatus(stage2.transferStatus)
    val verifiedBadge = if (stage2.conceptualShiftVerified) " • Verified ✓" else ""

    SizzleSectionCard(
        title = "2. Habit Diagnosis",
        subtitle = DisplayLabelFormatter.formatModelSource(reasoningModelUsed),
        icon = Icons.Default.Psychology
    ) {
        Text(
            text = "Pattern: ${DisplayLabelFormatter.formatArchetypeId(stage2.learnerArchetypeId)}",
            fontWeight = FontWeight.SemiBold,
            color = SizzleTheme.colors.textPrimary
        )
        Text(
            text = "Progress: $statusLabel$verifiedBadge",
            style = SizzleTheme.typography.bodySmall,
            color = SizzleTheme.colors.accentSuccess
        )
        LabeledDetailRow(
            label = "Root Cause",
            value = stage2.decisionRootCause
        )
        if (stage2.demonstratedUnderstandings.isNotEmpty()) {
            Text(
                text = "Understood",
                style = SizzleTheme.typography.labelMedium,
                color = SizzleTheme.colors.accentSuccess
            )
            stage2.demonstratedUnderstandings.forEach { understood ->
                Text(
                    text = "• $understood",
                    style = SizzleTheme.typography.bodySmall,
                    color = SizzleTheme.colors.textPrimary
                )
            }
        }
        if (stage2.latentMisconceptions.isNotEmpty()) {
            Text(
                text = "Latent Misconceptions",
                style = SizzleTheme.typography.labelMedium,
                color = SizzleTheme.colors.brandPrimary
            )
            stage2.latentMisconceptions.forEach { hypothesis ->
                val concept = DisplayLabelFormatter.toTitleCaseWords(hypothesis.conceptId)
                val confidencePct = DisplayLabelFormatter.formatRatioPercent(hypothesis.confidence)
                Text(
                    text = "• $concept ($confidencePct): ${hypothesis.description}",
                    style = SizzleTheme.typography.bodySmall,
                    color = SizzleTheme.colors.textSecondary
                )
            }
        }
        if (stage2.longitudinalDeltaSummary.isNotBlank()) {
            LabeledDetailRow(
                label = "Progress Note",
                value = stage2.longitudinalDeltaSummary
            )
        }
    }
}
