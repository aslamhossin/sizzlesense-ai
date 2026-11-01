package com.sizzlesense.ai.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class BehavioralTransferStatus {
    INITIAL_BASELINE,
    CONCEPT_ADOPTED,
    PARTIAL_EXECUTION_ONLY,
    REGRESSED_OR_OVERCORRECTED
}
