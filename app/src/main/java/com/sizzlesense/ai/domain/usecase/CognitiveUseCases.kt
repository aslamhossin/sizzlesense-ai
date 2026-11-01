package com.sizzlesense.ai.domain.usecase

import com.sizzlesense.ai.domain.model.CognitiveCoachingResponse
import com.sizzlesense.ai.domain.model.EvalSuiteReport
import com.sizzlesense.ai.domain.model.SessionAnalyzeRequest
import com.sizzlesense.ai.domain.model.TapoCameraConfig
import com.sizzlesense.ai.domain.repository.EvaluationBenchmarkGateway
import com.sizzlesense.ai.domain.repository.LiveTapoCameraGateway
import com.sizzlesense.ai.domain.repository.SessionAnalysisGateway

/**
 * SRP (Single Responsibility Principle) & Clean Architecture Dependency Rule:
 * Domain UseCase depends strictly on Domain models and Domain gateway interfaces (`Presentation -> Domain <- Data`).
 */
class AnalyzeCookingSessionUseCase(
    private val gateway: SessionAnalysisGateway,
) {
    suspend operator fun invoke(
        baseUrl: String,
        archetypeId: String,
        attemptNumber: Int,
        learnerReflection: String,
        capturedFrameBase64: String?,
        dishId: String = DEFAULT_DISH_ID,
    ): CognitiveCoachingResponse {
        val frames = capturedFrameBase64
            ?.takeIf { it.isNotBlank() }
            ?.let { listOf(it) }
            ?: emptyList()

        val request = SessionAnalyzeRequest(
            learnerId = "android_$archetypeId",
            dishId = dishId,
            attemptNumber = attemptNumber.coerceIn(1, 3),
            learnerReflection = learnerReflection.trim(),
            archetypeHint = archetypeId,
            keyframeBase64List = frames,
        )
        return gateway.analyzeSession(baseUrl = baseUrl, request = request)
    }

    companion object {
        const val DEFAULT_DISH_ID = "sauteed_onions"
    }
}

/**
 * SRP: Encapsulates live RTSP frame grab + Gemini 3 analysis from the kitchen Tapo C100 camera.
 */
class CaptureLiveTapoSessionUseCase(
    private val gateway: LiveTapoCameraGateway,
) {
    suspend operator fun invoke(
        baseUrl: String,
        tapoConfig: TapoCameraConfig,
        attemptNumber: Int,
        reflectionText: String,
    ): CognitiveCoachingResponse {
        require(tapoConfig.ipAddress.isNotBlank()) {
            "Tapo C100 IP address cannot be blank."
        }
        return gateway.captureFromBackendTapoAndAnalyze(
            baseUrl = baseUrl,
            tapoConfig = tapoConfig,
            attemptNumber = attemptNumber.coerceIn(1, 3),
            reflectionText = reflectionText.trim(),
        )
    }
}

/**
 * SRP: Executes the 10-Session Human Learning Evaluation Suite.
 */
class RunHumanLearningEvalUseCase(
    private val gateway: EvaluationBenchmarkGateway,
) {
    suspend operator fun invoke(baseUrl: String): EvalSuiteReport {
        return gateway.runEvaluationBenchmark(baseUrl = baseUrl)
    }
}
