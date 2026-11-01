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
import com.sizzlesense.ai.domain.model.ExpertTeachingDecision
import com.sizzlesense.ai.domain.model.LearnerStateEstimate
import com.sizzlesense.ai.domain.model.PhysicalObservationReport
import com.sizzlesense.ai.presentation.components.LabeledDetailRow
import com.sizzlesense.ai.presentation.components.SizzleSectionCard
import com.sizzlesense.ai.presentation.mvi.SizzleUiState

@Composable
fun AiXRayRoute(state: SizzleUiState) {
    AiXRayScreen(state = state)
}

@Composable
fun AiXRayScreen(state: SizzleUiState) {
    val resp = state.latestCoachingResponse
    if (resp == null) {
        EmptyXRayTraceState()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Stage1PhysicalObservationCard(
            visionModelUsed = resp.visionModelUsed,
            stage1 = resp.stage1PhysicalObservation
        )
        Stage2LearnerLatentStateCard(
            reasoningModelUsed = resp.reasoningModelUsed,
            stage2 = resp.stage2LearnerState
        )
        Stage3PedagogicalRestraintCard(
            stage3 = resp.stage3TeachingDecision
        )
    }
}

@Composable
private fun EmptyXRayTraceState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Run a session analysis first to inspect the 4-Stage Cognitive Trace.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun Stage1PhysicalObservationCard(
    visionModelUsed: String,
    stage1: PhysicalObservationReport
) {
    SizzleSectionCard(
        title = "Stage 1: Physical World Understanding",
        subtitle = "Model: $visionModelUsed",
        icon = Icons.Default.Visibility
    ) {
        LabeledDetailRow(
            label = "Perception Gap: ${stage1.perceptionGapLevel}",
            value = stage1.perceptionGapExplanation
        )
        stage1.cvMetrics?.let { cv ->
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
        stage1.inferredPhysicalStates.forEach { stateLine ->
            Text(
                text = "• $stateLine",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun Stage2LearnerLatentStateCard(
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

@Composable
private fun Stage3PedagogicalRestraintCard(stage3: ExpertTeachingDecision) {
    SizzleSectionCard(
        title = "Stage 3: Pedagogical Restraint & Omitted Issues",
        subtitle = "Chef Causal Priority Graph",
        icon = Icons.Default.FilterCenterFocus
    ) {
        LabeledDetailRow(
            label = "Selected Primary: ${stage3.selectedPrimaryIssue}",
            value = stage3.causalPriorityRationale
        )
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
