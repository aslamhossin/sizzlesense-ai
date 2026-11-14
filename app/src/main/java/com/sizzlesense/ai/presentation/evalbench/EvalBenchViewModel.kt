package com.sizzlesense.ai.presentation.evalbench

import com.sizzlesense.ai.core.ui.BaseViewModel
import com.sizzlesense.ai.core.ui.TaskStatus
import com.sizzlesense.ai.domain.model.session.SessionSnapshot
import com.sizzlesense.ai.domain.repository.SessionStore
import com.sizzlesense.ai.domain.usecase.RunHumanLearningEvalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

@HiltViewModel
class EvalBenchViewModel @Inject constructor(
    private val sessionStore: SessionStore,
    private val runHumanLearningEval: RunHumanLearningEvalUseCase
) : BaseViewModel<EvalBenchUiState, EvalBenchUiIntent, EvalBenchUiEffect>() {

    private val taskStatus = MutableStateFlow(TaskStatus())

    override val uiState: StateFlow<EvalBenchUiState> = combine(
        sessionStore.sessionState,
        taskStatus
    ) { session, status ->
        session.toEvalBenchUiState(status)
    }.stateWhileSubscribed(
        initialValue = sessionStore.sessionState.value.toEvalBenchUiState(taskStatus.value)
    )

    override fun onIntent(intent: EvalBenchUiIntent) {
        when (intent) {
            EvalBenchUiIntent.RunHumanLearningEvalSuite -> executeEvalSuite()
            EvalBenchUiIntent.ClearError -> {
                taskStatus.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun executeEvalSuite() {
        val baseUrl = sessionStore.sessionState.value.backendBaseUrl
        taskStatus.launchGuardedTask(
            runningLabel = "Running benchmark...",
            onSuccess = { report -> sessionStore.recordEvalSuiteReport(report) }
        ) {
            runHumanLearningEval(baseUrl = baseUrl)
        }
    }

    private fun SessionSnapshot.toEvalBenchUiState(status: TaskStatus) = EvalBenchUiState(
        isRunningEvals = status.isRunning,
        evalSuiteReport = evalSuiteReport,
        errorMessage = status.errorMessage
    )
}
