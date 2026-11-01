package com.sizzlesense.ai.presentation.xray.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.sizzlesense.ai.domain.model.coaching.PhysicalObservationReport
import com.sizzlesense.ai.presentation.common.component.LabeledDetailRow
import com.sizzlesense.ai.presentation.common.component.SizzleSectionCard

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
