package com.sizzlesense.ai.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OmittedIssue(
    val issue: String,
    @SerialName("suppression_reason")
    val suppressionReason: String
)
