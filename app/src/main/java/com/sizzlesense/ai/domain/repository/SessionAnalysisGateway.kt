package com.sizzlesense.ai.domain.repository

import com.sizzlesense.ai.domain.model.coaching.CognitiveCoachingResponse
import com.sizzlesense.ai.domain.model.session.SessionAnalyzeRequest

fun interface SessionAnalysisGateway {
    suspend fun analyzeSession(
        baseUrl: String,
        request: SessionAnalyzeRequest
    ): CognitiveCoachingResponse
}
