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
        if (stage1.learnerStatedBelief.isNotBlank()) {
            LabeledDetailRow(
                label = "Learner Belief",
                value = stage1.learnerStatedBelief
            )
        }
        LabeledDetailRow(
            label = "Observation: ${DisplayLabelFormatter.formatPerceptionGap(stage1.perceptionGapLevel)}",
            value = stage1.perceptionGapExplanation
        )
        val cv = stage1.cvMetrics
        if (cv != null) {
            MetricPillGrid(
                items = listOf(
                    MetricTileItem("Visual State", DisplayLabelFormatter.formatVisualRegime(cv.dominantVisualRegime)),
                    MetricTileItem("Browning", DisplayLabelFormatter.formatRatioPercent(cv.maillardBrowningRatio)),
                    MetricTileItem("Surface Moisture", DisplayLabelFormatter.formatRatioPercent(cv.surfaceMoistureIndex)),
                    MetricTileItem("Char", DisplayLabelFormatter.formatRatioPercent(cv.charBurnRatio)),
                    MetricTileItem("Mean Luminance", "${cv.meanLuminance.roundToInt()}")
                )
            )
        } else {
            LabeledDetailRow(
                label = "Camera Vision",
                value = "No Camera Frame Captured"
            )
        }
        stage1.acousticMetrics?.let { audio ->
            MetricPillGrid(
                items = listOf(
                    MetricTileItem("Sound Pattern", DisplayLabelFormatter.formatAcousticSignature(audio.inferredAcousticSignature)),
                    MetricTileItem("Peak Frequency", "${audio.dominantFrequencyHz.roundToInt()} Hz"),
                    MetricTileItem("Spectral Centroid", "${audio.spectralCentroidHz.roundToInt()} Hz"),
                    MetricTileItem("Crisp Sizzle", DisplayLabelFormatter.formatRatioPercent(audio.highFreqSizzleRatio)),
                    MetricTileItem("Wet Bubble", DisplayLabelFormatter.formatRatioPercent(audio.lowFreqBubbleRatio)),
                    MetricTileItem("Audio Level", "${audio.rmsEnergyDb.roundToInt()} dBFS")
                )
            )
        }
        if (stage1.observedTimelineFacts.isNotEmpty()) {
            ObservationBulletSection(
                title = "Observed Timeline",
                lines = stage1.observedTimelineFacts.map { fact ->
                    "${fact.timestamp} (${DisplayLabelFormatter.formatAcousticSignature(fact.acousticSignature)}): ${fact.visualObservation}"
                }
            )
        }
        if (stage1.inferredPhysicalStates.isNotEmpty()) {
            ObservationBulletSection(
                title = "Pan Physics",
                lines = stage1.inferredPhysicalStates
            )
        }
        if (stage1.environmentalUncertainties.isNotEmpty()) {
            ObservationBulletSection(
                title = "Uncertainties",
                lines = stage1.environmentalUncertainties,
                isSecondary = true
            )
        }
    }
}

@Composable
private fun ObservationBulletSection(
    title: String,
    lines: List<String>,
    isSecondary: Boolean = false
) {
    Text(
        text = title,
        style = SizzleTheme.typography.labelMedium,
        color = SizzleTheme.colors.brandPrimary
    )
    lines.forEach { line ->
        Text(
            text = "• $line",
            style = SizzleTheme.typography.bodySmall,
            color = if (isSecondary) SizzleTheme.colors.textSecondary else SizzleTheme.colors.textPrimary
        )
    }
}
