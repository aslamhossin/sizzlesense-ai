package com.sizzlesense.ai.domain.repository

import com.sizzlesense.ai.domain.model.CognitiveCoachingResponse
import com.sizzlesense.ai.domain.model.TapoCameraConfig

fun interface LiveTapoCameraGateway {
    suspend fun captureFromBackendTapoAndAnalyze(
        baseUrl: String,
        tapoConfig: TapoCameraConfig,
        attemptNumber: Int,
        reflectionText: String
    ): CognitiveCoachingResponse
}
