package com.sizzlesense.ai.presentation.evalbench

import com.sizzlesense.ai.domain.model.EvalSuiteReport

data class EvalBenchUiState(
    val isRunningEvals: Boolean = false,
    val evalSuiteReport: EvalSuiteReport? = null,
    val errorMessage: String? = null
)

sealed interface EvalBenchUiIntent {
    data object RunHumanLearningEvalSuite : EvalBenchUiIntent
    data object ClearError : EvalBenchUiIntent
}

sealed interface EvalBenchUiEffect {
    data class ShowEvalCompletedMessage(val message: String) : EvalBenchUiEffect
}
