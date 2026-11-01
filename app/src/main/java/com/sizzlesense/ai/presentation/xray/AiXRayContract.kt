package com.sizzlesense.ai.presentation.xray

import com.sizzlesense.ai.domain.model.CognitiveCoachingResponse

data class AiXRayUiState(
    val currentAttemptNumber: Int = 1,
    val latestCoachingResponse: CognitiveCoachingResponse? = null,
    val showSuppressedIssues: Boolean = true
)

sealed interface AiXRayUiIntent {
    data class ToggleSuppressedIssues(val visible: Boolean) : AiXRayUiIntent
}

sealed interface AiXRayUiEffect {
    data class ShowTraceNote(val note: String) : AiXRayUiEffect
}
