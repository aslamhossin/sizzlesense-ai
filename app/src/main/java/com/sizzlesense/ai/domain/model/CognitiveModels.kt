package com.sizzlesense.ai.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AcousticSizzleSignature {
    SILENT_COLD,
    WET_STEAM_BUBBLE,
    STEADY_APPLAUSE_SIZZLE,
    AGGRESSIVE_POPPING_BURN
}

@Serializable
enum class PerceptionGapLevel {
    ALIGNED,
    MODERATE_DRIFT,
    CRITICAL_BLINDSPOT
}

@Serializable
enum class BehavioralTransferStatus {
    INITIAL_BASELINE,
    CONCEPT_ADOPTED,
    PARTIAL_EXECUTION_ONLY,
    REGRESSED_OR_OVERCORRECTED
}

@Serializable
data class TimelineFact(
    val timestamp: String,
    @SerialName("visual_observation")
    val visualObservation: String,
    @SerialName("acoustic_signature")
    val acousticSignature: AcousticSizzleSignature = AcousticSizzleSignature.WET_STEAM_BUBBLE
)

@Serializable
data class ComputerVisionMetrics(
    @SerialName("maillard_browning_ratio")
    val maillardBrowningRatio: Double = 0.0,
    @SerialName("surface_moisture_index")
    val surfaceMoistureIndex: Double = 0.0,
    @SerialName("char_burn_ratio")
    val charBurnRatio: Double = 0.0,
    @SerialName("mean_luminance")
    val meanLuminance: Double = 128.0,
    @SerialName("dominant_visual_regime")
    val dominantVisualRegime: String = "TRANSITIONAL_SAUTE"
)

@Serializable
data class PhysicalObservationReport(
    @SerialName("observed_timeline_facts")
    val observedTimelineFacts: List<TimelineFact>,
    @SerialName("inferred_physical_states")
    val inferredPhysicalStates: List<String>,
    @SerialName("environmental_uncertainties")
    val environmentalUncertainties: List<String> = emptyList(),
    @SerialName("cv_metrics")
    val cvMetrics: ComputerVisionMetrics? = null,
    @SerialName("learner_stated_belief")
    val learnerStatedBelief: String,
    @SerialName("perception_gap_level")
    val perceptionGapLevel: PerceptionGapLevel,
    @SerialName("perception_gap_explanation")
    val perceptionGapExplanation: String
)

@Serializable
data class MisconceptionHypothesis(
    @SerialName("concept_id")
    val conceptId: String,
    val description: String,
    val confidence: Double
)

@Serializable
data class LearnerStateEstimate(
    @SerialName("learner_archetype_id")
    val learnerArchetypeId: String = "moisture_blindness",
    @SerialName("demonstrated_understandings")
    val demonstratedUnderstandings: List<String>,
    @SerialName("latent_misconceptions")
    val latentMisconceptions: List<MisconceptionHypothesis>,
    @SerialName("decision_root_cause")
    val decisionRootCause: String,
    @SerialName("transfer_status")
    val transferStatus: BehavioralTransferStatus = BehavioralTransferStatus.INITIAL_BASELINE,
    @SerialName("conceptual_shift_verified")
    val conceptualShiftVerified: Boolean = false,
    @SerialName("longitudinal_delta_summary")
    val longitudinalDeltaSummary: String = ""
)

@Serializable
data class OmittedIssue(
    val issue: String,
    @SerialName("suppression_reason")
    val suppressionReason: String
)

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

@Serializable
data class SensoryCoachingOutput(
    @SerialName("visual_cue_to_observe")
    val visualCueToObserve: String,
    @SerialName("auditory_cue_to_listen")
    val auditoryCueToListen: String,
    @SerialName("olfactory_or_tactile_cue")
    val olfactoryOrTactileCue: String,
    @SerialName("coaching_message")
    val coachingMessage: String,
    @SerialName("next_attempt_micro_experiment")
    val nextAttemptMicroExperiment: String
)

@Serializable
data class FiveCoreQuestionsSummary(
    @SerialName("what_is_happening_now")
    val whatIsHappeningNow: String,
    @SerialName("what_they_understand_vs_not")
    val whatTheyUnderstandVsNot: String,
    @SerialName("why_they_made_that_decision")
    val whyTheyMadeThatDecision: String,
    @SerialName("what_single_thing_to_teach_now")
    val whatSingleThingToTeachNow: String,
    @SerialName("how_to_communicate_to_shift_judgment")
    val howToCommunicateToShiftJudgment: String
)

@Serializable
data class SessionAnalyzeRequest(
    @SerialName("learner_id")
    val learnerId: String = "android_learner",
    @SerialName("dish_id")
    val dishId: String = "sauteed_onions",
    @SerialName("attempt_number")
    val attemptNumber: Int = 1,
    @SerialName("learner_reflection")
    val learnerReflection: String,
    @SerialName("archetype_hint")
    val archetypeHint: String? = null,
    @SerialName("keyframe_base64_list")
    val keyframeBase64List: List<String> = emptyList(),
    @SerialName("gemini_api_key")
    val geminiApiKey: String? = null,
    @SerialName("preferred_reasoning_model")
    val preferredReasoningModel: String = "gemini-3-flash-preview"
)

@Serializable
data class CognitiveCoachingResponse(
    @SerialName("session_id")
    val sessionId: String,
    @SerialName("learner_id")
    val learnerId: String,
    @SerialName("dish_id")
    val dishId: String,
    @SerialName("attempt_number")
    val attemptNumber: Int,
    @SerialName("reasoning_model_used")
    val reasoningModelUsed: String,
    @SerialName("vision_model_used")
    val visionModelUsed: String,
    @SerialName("five_core_questions")
    val fiveCoreQuestions: FiveCoreQuestionsSummary,
    @SerialName("stage1_physical_observation")
    val stage1PhysicalObservation: PhysicalObservationReport,
    @SerialName("stage2_learner_state")
    val stage2LearnerState: LearnerStateEstimate,
    @SerialName("stage3_teaching_decision")
    val stage3TeachingDecision: ExpertTeachingDecision,
    @SerialName("stage4_sensory_coaching")
    val stage4SensoryCoaching: SensoryCoachingOutput
)

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
