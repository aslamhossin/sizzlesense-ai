package com.sizzlesense.ai.domain.usecase

import com.sizzlesense.ai.core.result.AppError
import com.sizzlesense.ai.core.result.AppResult
import com.sizzlesense.ai.domain.repository.LearnerHistoryGateway
import javax.inject.Inject

class ClearLearnerHistoryUseCase @Inject constructor(
    private val gateway: LearnerHistoryGateway
) {

    suspend operator fun invoke(
        baseUrl: String,
        learnerId: String
    ): AppResult<Unit> {
        val cleanedLearnerId = learnerId.trim()
        if (cleanedLearnerId.isEmpty()) {
            return AppResult.Failure(
                AppError.ValidationError("Learner ID cannot be blank.")
            )
        }
        return gateway.clearLearnerHistory(
            baseUrl = baseUrl,
            learnerId = cleanedLearnerId
        )
    }
}
