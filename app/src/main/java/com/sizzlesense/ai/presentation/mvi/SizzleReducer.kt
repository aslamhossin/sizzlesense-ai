package com.sizzlesense.ai.presentation.mvi

object SizzleReducer {

    fun reduce(
        currentState: SizzleUiState,
        result: SizzleReductionResult
    ): SizzleUiState {
        return when (result) {
            is SizzleReductionResult.TabChanged -> currentState.copy(
                activeTab = result.tab,
                errorMessage = null
            )

            is SizzleReductionResult.BackendUrlUpdated -> currentState.copy(
                backendBaseUrl = result.url.trim()
            )

            is SizzleReductionResult.TapoConfigUpdated -> currentState.copy(
                tapoConfig = result.config
            )

            is SizzleReductionResult.FrameCaptured -> currentState.copy(
                capturedFrameBase64 = result.base64Jpeg,
                capturedFrameLabel = result.label
            )

            is SizzleReductionResult.ArchetypeSelected -> {
                val presetText = result.archetype
                    .presetReflections[currentState.currentAttemptNumber]
                    .orEmpty()
                currentState.copy(
                    selectedArchetype = result.archetype,
                    learnerReflectionText = presetText,
                    attemptHistory = emptyMap(),
                    latestCoachingResponse = null
                )
            }

            is SizzleReductionResult.AttemptSelected -> {
                val clamped = result.attemptNumber.coerceIn(1, 3)
                val presetText = currentState.selectedArchetype
                    .presetReflections[clamped]
                    .orEmpty()
                val existingAttemptResponse = currentState.attemptHistory[clamped]
                currentState.copy(
                    currentAttemptNumber = clamped,
                    learnerReflectionText = presetText,
                    latestCoachingResponse = existingAttemptResponse
                        ?: currentState.latestCoachingResponse
                )
            }

            is SizzleReductionResult.ReflectionUpdated -> currentState.copy(
                learnerReflectionText = result.text
            )

            is SizzleReductionResult.AnalysisStarted -> currentState.copy(
                isAnalyzing = true,
                activePipelineStageLabel = result.stageLabel,
                errorMessage = null
            )

            is SizzleReductionResult.AnalysisCompleted -> {
                val updatedHistory = currentState.attemptHistory.toMutableMap().apply {
                    put(result.response.attemptNumber, result.response)
                }
                currentState.copy(
                    isAnalyzing = false,
                    activePipelineStageLabel = "Completed (${result.response.reasoningModelUsed})",
                    latestCoachingResponse = result.response,
                    attemptHistory = updatedHistory,
                    activeTab = AppTab.COACHING_LOOP,
                    errorMessage = null
                )
            }

            is SizzleReductionResult.EvalSuiteStarted -> currentState.copy(
                isRunningEvals = true,
                errorMessage = null
            )

            is SizzleReductionResult.EvalSuiteCompleted -> currentState.copy(
                isRunningEvals = false,
                evalSuiteReport = result.report,
                activeTab = AppTab.EVAL_BENCH
            )

            is SizzleReductionResult.ErrorOccurred -> currentState.copy(
                isAnalyzing = false,
                isRunningEvals = false,
                activePipelineStageLabel = "Error occurred",
                errorMessage = result.message
            )

            is SizzleReductionResult.ErrorCleared -> currentState.copy(
                errorMessage = null
            )
        }
    }
}
