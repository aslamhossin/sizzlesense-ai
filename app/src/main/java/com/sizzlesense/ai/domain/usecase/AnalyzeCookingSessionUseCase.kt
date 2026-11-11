package com.sizzlesense.ai.domain.usecase

import com.sizzlesense.ai.core.result.AppResult
import com.sizzlesense.ai.domain.model.coaching.CognitiveCoachingResponse
import com.sizzlesense.ai.domain.model.session.SessionAnalyzeRequest
import com.sizzlesense.ai.domain.repository.SessionAnalysisGateway
import javax.inject.Inject

class AnalyzeCookingSessionUseCase @Inject constructor(
    private val gateway: SessionAnalysisGateway
) {

    suspend operator fun invoke(
        baseUrl: String,
        archetypeId: String,
        attemptNumber: Int,
        learnerReflection: String,
        capturedFrameBase64: String?,
        capturedAudioWavBase64: String? = null,
        dishId: String = DEFAULT_DISH_ID
    ): AppResult<CognitiveCoachingResponse> {
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
            audioWavBase64 = capturedAudioWavBase64?.takeIf { it.isNotBlank() }
        )
        return gateway.analyzeSession(
            baseUrl = baseUrl,
            request = request
        )
    }

    companion object {
        const val DEFAULT_DISH_ID = "sauteed_onions"
    }
}
