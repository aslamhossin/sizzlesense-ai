package com.sizzlesense.ai.domain.repository

import com.sizzlesense.ai.core.result.AppResult

fun interface LearnerHistoryGateway {
    suspend fun clearLearnerHistory(
        baseUrl: String,
        learnerId: String
    ): AppResult<Unit>
}
