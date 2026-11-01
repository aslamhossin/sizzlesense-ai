package com.sizzlesense.ai.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MisconceptionHypothesis(
    @SerialName("concept_id")
    val conceptId: String,
    val description: String,
    val confidence: Double
)
