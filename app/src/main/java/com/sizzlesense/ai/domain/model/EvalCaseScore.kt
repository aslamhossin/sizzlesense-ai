package com.sizzlesense.ai.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EvalCaseScore(
    @SerialName("case_id")
    val caseId: String,
    @SerialName("archetype_id")
    val archetypeId: String,
    @SerialName("attempt_number")
    val attemptNumber: Int,
    @SerialName("perception_gap_passed")
    val perceptionGapPassed: Boolean,
    @SerialName("root_cause_passed")
    val rootCausePassed: Boolean,
    @SerialName("single_issue_restraint_passed")
    val singleIssueRestraintPassed: Boolean,
    @SerialName("sensory_grounding_score")
    val sensoryGroundingScore: Double,
    @SerialName("transfer_tracking_passed")
    val transferTrackingPassed: Boolean,
    @SerialName("summary_note")
    val summaryNote: String
)
