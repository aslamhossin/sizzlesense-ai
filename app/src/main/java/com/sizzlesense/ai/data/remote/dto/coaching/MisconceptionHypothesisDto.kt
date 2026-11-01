package com.sizzlesense.ai.data.remote.dto.coaching

import com.sizzlesense.ai.domain.model.coaching.MisconceptionHypothesis
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MisconceptionHypothesisDto(
    @SerialName("concept_id")
    val conceptId: String,
    val description: String,
    val confidence: Double
)

fun MisconceptionHypothesisDto.toDomain(): MisconceptionHypothesis = MisconceptionHypothesis(
    conceptId = conceptId,
    description = description,
    confidence = confidence
)
