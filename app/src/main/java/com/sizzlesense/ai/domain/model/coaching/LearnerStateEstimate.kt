package com.sizzlesense.ai.domain.model.coaching

data class LearnerStateEstimate(
    val learnerArchetypeId: String = "moisture_blindness",
    val demonstratedUnderstandings: List<String>,
    val latentMisconceptions: List<MisconceptionHypothesis>,
    val decisionRootCause: String,
    val transferStatus: BehavioralTransferStatus = BehavioralTransferStatus.INITIAL_BASELINE,
    val conceptualShiftVerified: Boolean = false,
    val longitudinalDeltaSummary: String = ""
)
