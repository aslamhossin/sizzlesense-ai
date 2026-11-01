package com.sizzlesense.ai.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EvalSuiteReport(
    @SerialName("total_sessions_evaluated")
    val totalSessionsEvaluated: Int,
    @SerialName("perception_gap_detection_score")
    val perceptionGapDetectionScore: Double,
    @SerialName("latent_root_cause_accuracy")
    val latentRootCauseAccuracy: Double,
    @SerialName("single_issue_restraint_score")
    val singleIssueRestraintScore: Double,
    @SerialName("sensory_grounding_score")
    val sensoryGroundingScore: Double,
    @SerialName("cross_attempt_transfer_score")
    val crossAttemptTransferScore: Double,
    @SerialName("overall_human_learning_score")
    val overallHumanLearningScore: Double,
    @SerialName("case_results")
    val caseResults: List<EvalCaseScore>
)
