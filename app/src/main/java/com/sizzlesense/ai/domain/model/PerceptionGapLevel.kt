package com.sizzlesense.ai.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class PerceptionGapLevel {
    ALIGNED,
    MODERATE_DRIFT,
    CRITICAL_BLINDSPOT
}
