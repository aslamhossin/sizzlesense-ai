package com.sizzlesense.ai.data.remote.dto.coaching

import com.sizzlesense.ai.domain.model.coaching.ExpertTeachingDecision
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExpertTeachingDecisionDto(
    @SerialName("candidate_issues_detected")
    val candidateIssuesDetected: List<String>,
    @SerialName("selected_primary_issue")
    val selectedPrimaryIssue: String,
    @SerialName("causal_priority_rationale")
    val causalPriorityRationale: String,
    @SerialName("deliberately_omitted_issues")
    val deliberatelyOmittedIssues: List<OmittedIssueDto> = emptyList(),
    @SerialName("target_chef_principle_id")
    val targetChefPrincipleId: String
)

fun ExpertTeachingDecisionDto.toDomain(): ExpertTeachingDecision = ExpertTeachingDecision(
    candidateIssuesDetected = candidateIssuesDetected,
    selectedPrimaryIssue = selectedPrimaryIssue,
    causalPriorityRationale = causalPriorityRationale,
    deliberatelyOmittedIssues = deliberatelyOmittedIssues.map { it.toDomain() },
    targetChefPrincipleId = targetChefPrincipleId
)

fun ExpertTeachingDecision.toDto(): ExpertTeachingDecisionDto = ExpertTeachingDecisionDto(
    candidateIssuesDetected = candidateIssuesDetected,
    selectedPrimaryIssue = selectedPrimaryIssue,
    causalPriorityRationale = causalPriorityRationale,
    deliberatelyOmittedIssues = deliberatelyOmittedIssues.map { it.toDto() },
    targetChefPrincipleId = targetChefPrincipleId
)
