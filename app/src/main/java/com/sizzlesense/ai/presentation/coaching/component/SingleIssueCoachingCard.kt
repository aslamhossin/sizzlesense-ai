package com.sizzlesense.ai.presentation.coaching.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.sizzlesense.ai.domain.model.coaching.CognitiveCoachingResponse
import com.sizzlesense.ai.presentation.common.component.LabeledDetailRow
import com.sizzlesense.ai.presentation.common.component.SizzleSectionCard
import com.sizzlesense.ai.presentation.theme.SizzleTheme

@Composable
fun SingleIssueCoachingCard(response: CognitiveCoachingResponse) {
    val sensory = response.stage4SensoryCoaching
    SizzleSectionCard(
        title = response.stage3TeachingDecision.selectedPrimaryIssue,
        subtitle = "Single-Issue Focus (Attempt ${response.attemptNumber})",
        icon = Icons.Default.Lightbulb
    ) {
        Text(
            text = sensory.coachingMessage,
            style = SizzleTheme.typography.bodyMedium
        )
        HorizontalDivider()
        SensoryCuesList(sensory = sensory)
        HorizontalDivider()
        LabeledDetailRow(
            label = "Next Attempt Micro-Experiment:",
            value = sensory.nextAttemptMicroExperiment
        )
    }
}
