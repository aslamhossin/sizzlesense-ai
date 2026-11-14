package com.sizzlesense.ai.presentation.common.format

import com.sizzlesense.ai.domain.model.coaching.AcousticSizzleSignature
import com.sizzlesense.ai.domain.model.coaching.BehavioralTransferStatus
import com.sizzlesense.ai.domain.model.coaching.PerceptionGapLevel
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Formats backend enum identifiers, snake_case keys, and telemetry sources
 * into concise, human-readable UI strings.
 */
object DisplayLabelFormatter {

    private val archetypeLabels = mapOf(
        "moisture_blindness" to "Moisture Blindness",
        "heat_anxiety" to "Heat Anxiety",
        "perception_blindspot" to "Perception Blindspot"
    )

    private val visualRegimeLabels = mapOf(
        "PALE_STEAMING_OR_RAW" to "Pale & Steaming",
        "TRANSITIONAL_SAUTE" to "Warming & Clearing",
        "GOLDEN_MAILLARD_BROWNING" to "Golden Browning",
        "CHARRED_OVERCOOKED" to "Darkening / Scorch Risk"
    )

    fun formatPerceptionGap(level: PerceptionGapLevel): String = when (level) {
        PerceptionGapLevel.ALIGNED -> "Aligned"
        PerceptionGapLevel.MODERATE_DRIFT -> "Moderate Drift"
        PerceptionGapLevel.CRITICAL_BLINDSPOT -> "Perception Blindspot"
    }

    fun formatTransferStatus(status: BehavioralTransferStatus): String = when (status) {
        BehavioralTransferStatus.INITIAL_BASELINE -> "Baseline"
        BehavioralTransferStatus.CONCEPT_ADOPTED -> "Skill Applied"
        BehavioralTransferStatus.PARTIAL_EXECUTION_ONLY -> "Partial Progress"
        BehavioralTransferStatus.REGRESSED_OR_OVERCORRECTED -> "Needs Reinforcement"
    }

    fun formatAcousticSignature(signature: AcousticSizzleSignature): String = when (signature) {
        AcousticSizzleSignature.SILENT_COLD -> "Cold Pan"
        AcousticSizzleSignature.WET_STEAM_BUBBLE -> "Wet Bubble"
        AcousticSizzleSignature.STEADY_APPLAUSE_SIZZLE -> "Crisp Sizzle"
        AcousticSizzleSignature.AGGRESSIVE_POPPING_BURN -> "Harsh Scorch"
    }

    fun formatVisualRegime(raw: String): String =
        visualRegimeLabels[raw.uppercase(Locale.US)] ?: toTitleCaseWords(raw)

    fun formatArchetypeId(rawId: String): String =
        archetypeLabels[rawId.lowercase(Locale.US)] ?: toTitleCaseWords(rawId)

    fun formatEvalCaseTitle(caseId: String): String {
        if (caseId == "negative_control_lucky_timer") {
            return "Lucky Timer Control"
        }
        val cleaned = caseId.replace(Regex("_attempt_\\d+$"), "")
        return formatArchetypeId(cleaned)
    }

    fun formatEvalNote(rawNote: String): String {
        val enumReplacements = mapOf(
            "CRITICAL_BLINDSPOT" to formatPerceptionGap(PerceptionGapLevel.CRITICAL_BLINDSPOT),
            "MODERATE_DRIFT" to formatPerceptionGap(PerceptionGapLevel.MODERATE_DRIFT),
            "ALIGNED" to formatPerceptionGap(PerceptionGapLevel.ALIGNED),
            "INITIAL_BASELINE" to formatTransferStatus(BehavioralTransferStatus.INITIAL_BASELINE),
            "CONCEPT_ADOPTED" to formatTransferStatus(BehavioralTransferStatus.CONCEPT_ADOPTED),
            "PARTIAL_EXECUTION_ONLY" to formatTransferStatus(BehavioralTransferStatus.PARTIAL_EXECUTION_ONLY),
            "REGRESSED_OR_OVERCORRECTED" to formatTransferStatus(BehavioralTransferStatus.REGRESSED_OR_OVERCORRECTED)
        )
        var cleaned = rawNote.replace(" != ", " is not ")
        cleaned = Regex("\\b[A-Z][A-Z0-9]*(?:_[A-Z0-9]+)*\\b").replace(cleaned) { match ->
            val token = match.value
            when {
                enumReplacements.containsKey(token) -> enumReplacements.getValue(token)
                token.contains("_") -> toTitleCaseWords(token)
                else -> token
            }
        }
        return cleaned
    }

    fun formatModelSource(rawModel: String): String {
        val lower = rawModel.lowercase(Locale.US)
        val hasCv = lower.contains("cv") || lower.contains("pixel")
        val hasAudio = lower.contains("audio") || lower.contains("fft")
        return when {
            lower.contains("offline") -> "Offline Simulation"
            hasCv && hasAudio -> "Vision + Sound Analysis"
            hasCv -> "Vision Analysis"
            hasAudio -> "Sound Analysis"
            lower.contains("gemini") || lower.contains("gpt") || lower.contains("vision") -> "AI Coaching Engine"
            else -> rawModel
        }
    }

    fun formatRatioPercent(ratio: Double): String = "${(ratio.coerceIn(0.0, 1.0) * 100.0).roundToInt()}%"

    fun toTitleCaseWords(raw: String): String = raw
        .trim()
        .split('_', '-', ' ')
        .filter { it.isNotBlank() }
        .joinToString(" ") { part ->
            part.lowercase(Locale.US).replaceFirstChar { ch ->
                if (ch.isLowerCase()) ch.titlecase(Locale.US) else ch.toString()
            }
        }
}
