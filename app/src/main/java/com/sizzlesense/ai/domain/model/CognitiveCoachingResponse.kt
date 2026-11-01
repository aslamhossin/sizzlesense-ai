package com.sizzlesense.ai.domain.model

data class CognitiveCoachingResponse(
    val sessionId: String,
    val learnerId: String,
    val dishId: String,
    val attemptNumber: Int,
    val reasoningModelUsed: String,
    val visionModelUsed: String,
    val fiveCoreQuestions: FiveCoreQuestionsSummary,
    val stage1PhysicalObservation: PhysicalObservationReport,
    val stage2LearnerState: LearnerStateEstimate,
    val stage3TeachingDecision: ExpertTeachingDecision,
    val stage4SensoryCoaching: SensoryCoachingOutput
)
