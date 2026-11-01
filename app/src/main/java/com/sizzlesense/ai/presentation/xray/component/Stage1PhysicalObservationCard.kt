package com.sizzlesense.ai.presentation.xray.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.sizzlesense.ai.domain.model.coaching.PhysicalObservationReport
import com.sizzlesense.ai.presentation.common.component.LabeledDetailRow
import com.sizzlesense.ai.presentation.common.component.SizzleSectionCard
import com.sizzlesense.ai.presentation.theme.SizzleTheme

@Composable
fun Stage1PhysicalObservationCard(
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
                style = SizzleTheme.typography.bodySmall,
                color = SizzleTheme.colors.textSecondary
            )
        }
        Text(
            text = "Inferred Physical States:",
            style = SizzleTheme.typography.labelMedium,
            color = SizzleTheme.colors.brandPrimary
        )
        stage1.inferredPhysicalStates.forEach { stateLine ->
            Text(
                text = "• $stateLine",
                style = SizzleTheme.typography.bodySmall
            )
        }
    }
}
