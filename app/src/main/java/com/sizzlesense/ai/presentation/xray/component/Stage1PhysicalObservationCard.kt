package com.sizzlesense.ai.presentation.xray.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.sizzlesense.ai.domain.model.coaching.PhysicalObservationReport
import com.sizzlesense.ai.presentation.common.component.LabeledDetailRow
import com.sizzlesense.ai.presentation.common.component.MetricPillGrid
import com.sizzlesense.ai.presentation.common.component.MetricTileItem
import com.sizzlesense.ai.presentation.common.component.SizzleSectionCard
import com.sizzlesense.ai.presentation.common.format.DisplayLabelFormatter
import com.sizzlesense.ai.presentation.theme.SizzleTheme
import kotlin.math.roundToInt

@Composable
fun Stage1PhysicalObservationCard(
    visionModelUsed: String,
    stage1: PhysicalObservationReport
) {
    SizzleSectionCard(
        title = "1. Pan Perception",
        subtitle = DisplayLabelFormatter.formatModelSource(visionModelUsed),
        icon = Icons.Default.Visibility
    ) {
        LabeledDetailRow(
            label = "Observation: ${DisplayLabelFormatter.formatPerceptionGap(stage1.perceptionGapLevel)}",
            value = stage1.perceptionGapExplanation
        )
        stage1.cvMetrics?.let { cv ->
            MetricPillGrid(
                items = listOf(
                    MetricTileItem("Visual State", DisplayLabelFormatter.formatVisualRegime(cv.dominantVisualRegime)),
                    MetricTileItem("Browning", DisplayLabelFormatter.formatRatioPercent(cv.maillardBrowningRatio)),
                    MetricTileItem("Surface Moisture", DisplayLabelFormatter.formatRatioPercent(cv.surfaceMoistureIndex)),
                    MetricTileItem("Char", DisplayLabelFormatter.formatRatioPercent(cv.charBurnRatio))
                )
            )
        }
        stage1.acousticMetrics?.let { audio ->
            MetricPillGrid(
                items = listOf(
                    MetricTileItem("Sound Pattern", DisplayLabelFormatter.formatAcousticSignature(audio.inferredAcousticSignature)),
                    MetricTileItem("Peak Frequency", "${audio.spectralCentroidHz.roundToInt()} Hz"),
                    MetricTileItem("Crisp Sizzle", DisplayLabelFormatter.formatRatioPercent(audio.highFreqSizzleRatio)),
                    MetricTileItem("Wet Bubble", DisplayLabelFormatter.formatRatioPercent(audio.lowFreqBubbleRatio)),
                    MetricTileItem("Audio Level", "${audio.rmsEnergyDb.roundToInt()} dBFS")
                )
            )
        }
        Text(
            text = "Pan Physics",
            style = SizzleTheme.typography.labelMedium,
            color = SizzleTheme.colors.brandPrimary
        )
        stage1.inferredPhysicalStates.forEach { stateLine ->
            Text(
                text = "• $stateLine",
                style = SizzleTheme.typography.bodySmall,
                color = SizzleTheme.colors.textPrimary
            )
        }
    }
}
