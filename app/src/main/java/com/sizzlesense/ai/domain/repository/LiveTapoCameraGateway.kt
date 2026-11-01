package com.sizzlesense.ai.domain.repository

import com.sizzlesense.ai.core.result.AppResult
import com.sizzlesense.ai.domain.model.camera.TapoCameraConfig
import com.sizzlesense.ai.domain.model.coaching.CognitiveCoachingResponse

fun interface LiveTapoCameraGateway {
    suspend fun captureFromBackendTapoAndAnalyze(
        baseUrl: String,
        tapoConfig: TapoCameraConfig,
        attemptNumber: Int,
        reflectionText: String
    ): AppResult<CognitiveCoachingResponse>
}
