package com.sizzlesense.ai.domain.model.eval

data class EvalCaseScore(
    val caseId: String,
    val archetypeId: String,
    val attemptNumber: Int,
    val perceptionGapPassed: Boolean,
    val rootCausePassed: Boolean,
    val singleIssueRestraintPassed: Boolean,
    val sensoryGroundingScore: Double,
    val transferTrackingPassed: Boolean,
    val summaryNote: String
)
