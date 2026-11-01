package com.sizzlesense.ai.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterCenterFocus
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sizzlesense.ai.presentation.components.LabeledDetailRow
import com.sizzlesense.ai.presentation.components.SizzleSectionCard
import com.sizzlesense.ai.presentation.mvi.SizzleUiState

@Composable
fun AiXRayInspectorTab(state: SizzleUiState) {
    val resp = state.latestCoachingResponse
    if (resp == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Run a session analysis first to inspect the 4-Stage Cognitive Trace.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SizzleSectionCard(
            title = "Stage 1: Physical World Understanding",
            subtitle = "Model: ${resp.visionModelUsed}",
            icon = Icons.Default.Visibility
        ) {
            LabeledDetailRow(
                label = "Perception Gap: ${resp.stage1PhysicalObservation.perceptionGapLevel}",
                value = resp.stage1PhysicalObservation.perceptionGapExplanation
            )
            resp.stage1PhysicalObservation.cvMetrics?.let { cv ->
                Text(
                    text = "CV Metrics -> Browning: ${cv.maillardBrowningRatio} | Moisture: ${cv.surfaceMoistureIndex} | Char: ${cv.charBurnRatio} | Regime: ${cv.dominantVisualRegime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "Inferred Physical States:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            resp.stage1PhysicalObservation.inferredPhysicalStates.forEach { stateLine ->
                Text(
                    text = "• $stateLine",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        SizzleSectionCard(
            title = "Stage 2: Learner Latent State Estimation",
            subtitle = "Model: ${resp.reasoningModelUsed}",
            icon = Icons.Default.Psychology
        ) {
            Text(
                text = "Inferred Archetype: ${resp.stage2LearnerState.learnerArchetypeId}",
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Transfer Status: ${resp.stage2LearnerState.transferStatus} (Verified Shift: ${resp.stage2LearnerState.conceptualShiftVerified})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary
            )
            LabeledDetailRow(
                label = "Decision Root Cause:",
                value = resp.stage2LearnerState.decisionRootCause
            )
            LabeledDetailRow(
                label = "Longitudinal Delta:",
                value = resp.stage2LearnerState.longitudinalDeltaSummary
            )
        }

        SizzleSectionCard(
            title = "Stage 3: Pedagogical Restraint & Omitted Issues",
            subtitle = "Chef Causal Priority Graph",
            icon = Icons.Default.FilterCenterFocus
        ) {
            LabeledDetailRow(
                label = "Selected Primary: ${resp.stage3TeachingDecision.selectedPrimaryIssue}",
                value = resp.stage3TeachingDecision.causalPriorityRationale
            )
            Text(
                text = "Deliberately Suppressed Secondary Issues:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            resp.stage3TeachingDecision.deliberatelyOmittedIssues.forEach { omitted ->
                Text(
                    text = "• ${omitted.issue} — ${omitted.suppressionReason}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
