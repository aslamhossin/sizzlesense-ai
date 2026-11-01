package com.sizzlesense.ai.domain.model

data class EvalSuiteReport(
    val totalSessionsEvaluated: Int,
    val perceptionGapDetectionScore: Double,
    val latentRootCauseAccuracy: Double,
    val singleIssueRestraintScore: Double,
    val sensoryGroundingScore: Double,
    val crossAttemptTransferScore: Double,
    val overallHumanLearningScore: Double,
    val caseResults: List<EvalCaseScore>
)
