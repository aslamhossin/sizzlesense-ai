package com.sizzlesense.ai.domain.repository

import com.sizzlesense.ai.domain.model.CognitiveCoachingResponse
import com.sizzlesense.ai.domain.model.SessionAnalyzeRequest

fun interface SessionAnalysisGateway {
    suspend fun analyzeSession(
        baseUrl: String,
        request: SessionAnalyzeRequest
    ): CognitiveCoachingResponse
}
