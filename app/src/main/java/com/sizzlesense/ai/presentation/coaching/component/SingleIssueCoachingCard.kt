package com.sizzlesense.ai.presentation.coaching.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.sizzlesense.ai.domain.model.coaching.CognitiveCoachingResponse
import com.sizzlesense.ai.presentation.common.component.LabeledDetailRow
import com.sizzlesense.ai.presentation.common.component.SizzleSectionCard
import com.sizzlesense.ai.presentation.common.format.DisplayLabelFormatter
import com.sizzlesense.ai.presentation.theme.SizzleTheme

@Composable
fun SingleIssueCoachingCard(response: CognitiveCoachingResponse) {
    val sensory = response.stage4SensoryCoaching
    val gapLabel = DisplayLabelFormatter.formatPerceptionGap(response.stage1PhysicalObservation.perceptionGapLevel)
    SizzleSectionCard(
        title = response.stage3TeachingDecision.selectedPrimaryIssue,
        subtitle = "Attempt ${response.attemptNumber} • $gapLabel",
        icon = Icons.Default.Lightbulb
    ) {
        Text(
            text = sensory.coachingMessage,
            style = SizzleTheme.typography.bodyMedium,
            color = SizzleTheme.colors.textPrimary
        )
        HorizontalDivider(color = SizzleTheme.colors.outlineSubtle)
        SensoryCuesList(sensory = sensory)
        HorizontalDivider(color = SizzleTheme.colors.outlineSubtle)
        LabeledDetailRow(
            label = "Next Try",
            value = sensory.nextAttemptMicroExperiment
        )
        Text(
            text = "Reasoning: ${DisplayLabelFormatter.formatModelSource(response.reasoningModelUsed)} • Vision: ${DisplayLabelFormatter.formatModelSource(response.visionModelUsed)}",
            style = SizzleTheme.typography.labelSmall,
            color = SizzleTheme.colors.textSecondary
        )
    }
}
