package com.sizzlesense.ai.domain.model.coaching

data class MisconceptionHypothesis(
    val conceptId: String,
    val description: String,
    val confidence: Double
)
