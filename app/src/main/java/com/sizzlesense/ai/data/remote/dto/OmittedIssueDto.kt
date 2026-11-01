package com.sizzlesense.ai.data.remote.dto

import com.sizzlesense.ai.domain.model.OmittedIssue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OmittedIssueDto(
    val issue: String,
    @SerialName("suppression_reason")
    val suppressionReason: String
)

fun OmittedIssueDto.toDomain(): OmittedIssue = OmittedIssue(
    issue = issue,
    suppressionReason = suppressionReason
)
