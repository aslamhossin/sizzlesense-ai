package com.sizzlesense.ai.presentation.evalbench

import com.sizzlesense.ai.core.ui.BaseViewModel
import com.sizzlesense.ai.domain.repository.SessionStore
import com.sizzlesense.ai.domain.usecase.RunHumanLearningEvalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EvalBenchViewModel @Inject constructor(
    private val sessionStore: SessionStore,
    private val runHumanLearningEval: RunHumanLearningEvalUseCase
) : BaseViewModel<EvalBenchUiState, EvalBenchUiIntent, EvalBenchUiEffect>(EvalBenchUiState()) {

    init {
        sessionStore.sessionState.collectWhileSubscribed { session ->
            updateState { state ->
                state.copy(evalSuiteReport = session.evalSuiteReport)
            }
        }
    }

    override fun onIntent(intent: EvalBenchUiIntent) {
        when (intent) {
            EvalBenchUiIntent.RunHumanLearningEvalSuite -> executeEvalSuite()
            EvalBenchUiIntent.ClearError -> {
                updateState { it.copy(errorMessage = null) }
            }
        }
    }

    private fun executeEvalSuite() {
        val baseUrl = sessionStore.sessionState.value.backendBaseUrl
        updateState { it.copy(isRunningEvals = true, errorMessage = null) }
        launchResultTask(
            onSuccess = { report ->
                sessionStore.recordEvalSuiteReport(report)
                updateState { it.copy(isRunningEvals = false, errorMessage = null) }
            },
            onError = { appError ->
                updateState {
                    it.copy(
                        isRunningEvals = false,
                        errorMessage = appError.readableMessage
                    )
                }
            }
        ) {
            runHumanLearningEval(baseUrl = baseUrl)
        }
    }
}
