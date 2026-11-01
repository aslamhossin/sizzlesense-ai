package com.sizzlesense.ai.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LearnerStateEstimate(
    @SerialName("learner_archetype_id")
    val learnerArchetypeId: String = "moisture_blindness",
    @SerialName("demonstrated_understandings")
    val demonstratedUnderstandings: List<String>,
    @SerialName("latent_misconceptions")
    val latentMisconceptions: List<MisconceptionHypothesis>,
    @SerialName("decision_root_cause")
    val decisionRootCause: String,
    @SerialName("transfer_status")
    val transferStatus: BehavioralTransferStatus = BehavioralTransferStatus.INITIAL_BASELINE,
    @SerialName("conceptual_shift_verified")
    val conceptualShiftVerified: Boolean = false,
    @SerialName("longitudinal_delta_summary")
    val longitudinalDeltaSummary: String = ""
)
