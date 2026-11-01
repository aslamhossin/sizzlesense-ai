package com.sizzlesense.ai.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
