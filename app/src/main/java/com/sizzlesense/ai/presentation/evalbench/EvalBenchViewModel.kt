package com.sizzlesense.ai.presentation.evalbench

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sizzlesense.ai.domain.repository.SessionStore
import com.sizzlesense.ai.domain.usecase.RunHumanLearningEvalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class EvalBenchViewModel @Inject constructor(
    private val sessionStore: SessionStore,
    private val runHumanLearningEval: RunHumanLearningEvalUseCase
) : ViewModel() {

    private data class ExecutionState(
        val isRunningEvals: Boolean = false,
        val errorMessage: String? = null
    )

    private val executionState = MutableStateFlow(ExecutionState())

    private val _effects = MutableSharedFlow<EvalBenchUiEffect>()
    val effects: SharedFlow<EvalBenchUiEffect> = _effects.asSharedFlow()

    val uiState: StateFlow<EvalBenchUiState> = combine(
        sessionStore.sessionState,
        executionState
    ) { session, exec ->
        EvalBenchUiState(
            isRunningEvals = exec.isRunningEvals,
            evalSuiteReport = session.evalSuiteReport,
            errorMessage = exec.errorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EvalBenchUiState()
    )

    fun onIntent(intent: EvalBenchUiIntent) {
        when (intent) {
            EvalBenchUiIntent.RunHumanLearningEvalSuite -> executeEvalSuite()
            EvalBenchUiIntent.ClearError -> {
                executionState.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun executeEvalSuite() {
        val baseUrl = sessionStore.sessionState.value.backendBaseUrl
        viewModelScope.launch {
            executionState.update { ExecutionState(isRunningEvals = true, errorMessage = null) }
            try {
                val report = runHumanLearningEval(baseUrl = baseUrl)
                sessionStore.recordEvalSuiteReport(report)
                executionState.update { ExecutionState(isRunningEvals = false, errorMessage = null) }
            } catch (e: Exception) {
                executionState.update {
                    ExecutionState(
                        isRunningEvals = false,
                        errorMessage = e.message ?: "Eval benchmark execution failed"
                    )
                }
            }
        }
    }
}
