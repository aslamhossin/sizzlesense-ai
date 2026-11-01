package com.sizzlesense.ai.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExpertTeachingDecision(
    @SerialName("candidate_issues_detected")
    val candidateIssuesDetected: List<String>,
    @SerialName("selected_primary_issue")
    val selectedPrimaryIssue: String,
    @SerialName("causal_priority_rationale")
    val causalPriorityRationale: String,
    @SerialName("deliberately_omitted_issues")
    val deliberatelyOmittedIssues: List<OmittedIssue> = emptyList(),
    @SerialName("target_chef_principle_id")
    val targetChefPrincipleId: String
)
