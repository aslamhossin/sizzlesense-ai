package com.sizzlesense.ai.presentation.evalbench

import androidx.lifecycle.viewModelScope
import com.sizzlesense.ai.core.ui.BaseViewModel
import com.sizzlesense.ai.domain.repository.SessionStore
import com.sizzlesense.ai.domain.usecase.RunHumanLearningEvalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@HiltViewModel
class EvalBenchViewModel @Inject constructor(
    private val sessionStore: SessionStore,
    private val runHumanLearningEval: RunHumanLearningEvalUseCase
) : BaseViewModel<EvalBenchUiState, EvalBenchUiIntent, EvalBenchUiEffect>(EvalBenchUiState()) {

    init {
        sessionStore.sessionState
            .onEach { session ->
                updateState { state ->
                    state.copy(evalSuiteReport = session.evalSuiteReport)
                }
            }
            .launchIn(viewModelScope)
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
        launchTask(
            onError = { error ->
                updateState {
                    it.copy(
                        isRunningEvals = false,
                        errorMessage = error.message ?: "Eval benchmark execution failed"
                    )
                }
            }
        ) {
            val report = runHumanLearningEval(baseUrl = baseUrl)
            sessionStore.recordEvalSuiteReport(report)
            updateState { it.copy(isRunningEvals = false, errorMessage = null) }
        }
    }
}
