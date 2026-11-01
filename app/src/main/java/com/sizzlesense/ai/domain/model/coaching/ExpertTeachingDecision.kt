package com.sizzlesense.ai.domain.model.coaching

data class ExpertTeachingDecision(
    val candidateIssuesDetected: List<String>,
    val selectedPrimaryIssue: String,
    val causalPriorityRationale: String,
    val deliberatelyOmittedIssues: List<OmittedIssue> = emptyList(),
    val targetChefPrincipleId: String
)
