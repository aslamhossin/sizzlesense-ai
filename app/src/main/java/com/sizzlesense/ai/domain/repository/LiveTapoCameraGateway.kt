package com.sizzlesense.ai.domain.repository

import com.sizzlesense.ai.domain.model.coaching.CognitiveCoachingResponse
import com.sizzlesense.ai.domain.model.camera.TapoCameraConfig

fun interface LiveTapoCameraGateway {
    suspend fun captureFromBackendTapoAndAnalyze(
        baseUrl: String,
        tapoConfig: TapoCameraConfig,
        attemptNumber: Int,
        reflectionText: String
    ): CognitiveCoachingResponse
}
