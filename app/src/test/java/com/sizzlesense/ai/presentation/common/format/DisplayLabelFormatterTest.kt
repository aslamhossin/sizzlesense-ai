package com.sizzlesense.ai.presentation.common.format

import com.sizzlesense.ai.domain.model.coaching.AcousticSizzleSignature
import com.sizzlesense.ai.domain.model.coaching.BehavioralTransferStatus
import com.sizzlesense.ai.domain.model.coaching.PerceptionGapLevel
import org.junit.Assert.assertEquals
import org.junit.Test

class DisplayLabelFormatterTest {

    @Test
    fun formatters_convertInternalIdsAndEnumsIntoHumanReadableCopy() {
        assertEquals("Aligned", DisplayLabelFormatter.formatPerceptionGap(PerceptionGapLevel.ALIGNED))
        assertEquals(
            "Perception Blindspot",
            DisplayLabelFormatter.formatPerceptionGap(PerceptionGapLevel.CRITICAL_BLINDSPOT)
        )
        assertEquals(
            "Skill Applied",
            DisplayLabelFormatter.formatTransferStatus(BehavioralTransferStatus.CONCEPT_ADOPTED)
        )
        assertEquals(
            "Crisp Sizzle",
            DisplayLabelFormatter.formatAcousticSignature(AcousticSizzleSignature.STEADY_APPLAUSE_SIZZLE)
        )
        assertEquals(
            "Pale & Steaming",
            DisplayLabelFormatter.formatVisualRegime("PALE_STEAMING_OR_RAW")
        )
        assertEquals(
            "Moisture Blindness",
            DisplayLabelFormatter.formatEvalCaseTitle("moisture_blindness_attempt_1")
        )
        assertEquals(
            "Lucky Timer Control",
            DisplayLabelFormatter.formatEvalCaseTitle("negative_control_lucky_timer")
        )
        assertEquals(
            "Vision + Sound Analysis",
            DisplayLabelFormatter.formatModelSource("gemini-3-flash-preview + CV-Pixel-Analyzer + Audio-FFT")
        )
        assertEquals(
            "Vision + Sound Analysis",
            DisplayLabelFormatter.formatModelSource("gpt-4o-vision + CV-Pixel + Audio-FFT")
        )
        assertEquals(
            "Offline Simulation",
            DisplayLabelFormatter.formatModelSource("Offline Demo Simulation (Backend Unreachable)")
        )
        assertEquals(
            "Caught Perception Blindspot on pale onions",
            DisplayLabelFormatter.formatEvalNote("Caught CRITICAL_BLINDSPOT on pale onions")
        )
        assertEquals(
            "Correctly flagged Partial Progress on blind timer",
            DisplayLabelFormatter.formatEvalNote("Correctly flagged PARTIAL_EXECUTION_ONLY on blind timer")
        )
        assertEquals(
            "Steam is not smoke misconception isolated",
            DisplayLabelFormatter.formatEvalNote("Steam != smoke misconception isolated")
        )
        assertEquals("73%", DisplayLabelFormatter.formatRatioPercent(0.725))
    }
}
