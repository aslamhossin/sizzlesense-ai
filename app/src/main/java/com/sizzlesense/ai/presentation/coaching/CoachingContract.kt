package com.sizzlesense.ai.presentation.coaching

import com.sizzlesense.ai.domain.model.coaching.CognitiveCoachingResponse

data class CoachingUiState(
    val currentAttemptNumber: Int = 1,
    val latestCoachingResponse: CognitiveCoachingResponse? = null,
    val isAnalyzing: Boolean = false,
    val errorMessage: String? = null
)

sealed interface CoachingUiIntent {
    data object AnalyzeCurrentAttempt : CoachingUiIntent
    data object ClearError : CoachingUiIntent
}

sealed interface CoachingUiEffect {
    data class ShowMessage(val message: String) : CoachingUiEffect
}
