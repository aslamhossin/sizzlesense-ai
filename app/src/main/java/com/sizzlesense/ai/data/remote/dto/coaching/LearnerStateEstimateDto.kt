package com.sizzlesense.ai.data.remote.dto.coaching

import com.sizzlesense.ai.domain.model.coaching.BehavioralTransferStatus
import com.sizzlesense.ai.domain.model.coaching.LearnerStateEstimate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LearnerStateEstimateDto(
    @SerialName("learner_archetype_id")
    val learnerArchetypeId: String = "moisture_blindness",
    @SerialName("demonstrated_understandings")
    val demonstratedUnderstandings: List<String>,
    @SerialName("latent_misconceptions")
    val latentMisconceptions: List<MisconceptionHypothesisDto>,
    @SerialName("decision_root_cause")
    val decisionRootCause: String,
    @SerialName("transfer_status")
    val transferStatus: String = "INITIAL_BASELINE",
    @SerialName("conceptual_shift_verified")
    val conceptualShiftVerified: Boolean = false,
    @SerialName("longitudinal_delta_summary")
    val longitudinalDeltaSummary: String = ""
)

fun LearnerStateEstimateDto.toDomain(): LearnerStateEstimate = LearnerStateEstimate(
    learnerArchetypeId = learnerArchetypeId,
    demonstratedUnderstandings = demonstratedUnderstandings,
    latentMisconceptions = latentMisconceptions.map { it.toDomain() },
    decisionRootCause = decisionRootCause,
    transferStatus = runCatching {
        BehavioralTransferStatus.valueOf(transferStatus)
    }.getOrDefault(BehavioralTransferStatus.INITIAL_BASELINE),
    conceptualShiftVerified = conceptualShiftVerified,
    longitudinalDeltaSummary = longitudinalDeltaSummary
)

fun LearnerStateEstimate.toDto(): LearnerStateEstimateDto = LearnerStateEstimateDto(
    learnerArchetypeId = learnerArchetypeId,
    demonstratedUnderstandings = demonstratedUnderstandings,
    latentMisconceptions = latentMisconceptions.map { it.toDto() },
    decisionRootCause = decisionRootCause,
    transferStatus = transferStatus.name,
    conceptualShiftVerified = conceptualShiftVerified,
    longitudinalDeltaSummary = longitudinalDeltaSummary
)
