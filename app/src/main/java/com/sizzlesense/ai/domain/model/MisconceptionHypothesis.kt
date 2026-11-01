package com.sizzlesense.ai.domain.model

data class MisconceptionHypothesis(
    val conceptId: String,
    val description: String,
    val confidence: Double
)
