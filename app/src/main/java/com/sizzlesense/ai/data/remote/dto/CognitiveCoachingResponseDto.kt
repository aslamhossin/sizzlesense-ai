package com.sizzlesense.ai.data.remote.dto

import com.sizzlesense.ai.domain.model.CognitiveCoachingResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CognitiveCoachingResponseDto(
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
    val fiveCoreQuestions: FiveCoreQuestionsSummaryDto,
    @SerialName("stage1_physical_observation")
    val stage1PhysicalObservation: PhysicalObservationReportDto,
    @SerialName("stage2_learner_state")
    val stage2LearnerState: LearnerStateEstimateDto,
    @SerialName("stage3_teaching_decision")
    val stage3TeachingDecision: ExpertTeachingDecisionDto,
    @SerialName("stage4_sensory_coaching")
    val stage4SensoryCoaching: SensoryCoachingOutputDto
)

fun CognitiveCoachingResponseDto.toDomain(): CognitiveCoachingResponse = CognitiveCoachingResponse(
    sessionId = sessionId,
    learnerId = learnerId,
    dishId = dishId,
    attemptNumber = attemptNumber,
    reasoningModelUsed = reasoningModelUsed,
    visionModelUsed = visionModelUsed,
    fiveCoreQuestions = fiveCoreQuestions.toDomain(),
    stage1PhysicalObservation = stage1PhysicalObservation.toDomain(),
    stage2LearnerState = stage2LearnerState.toDomain(),
    stage3TeachingDecision = stage3TeachingDecision.toDomain(),
    stage4SensoryCoaching = stage4SensoryCoaching.toDomain()
)
