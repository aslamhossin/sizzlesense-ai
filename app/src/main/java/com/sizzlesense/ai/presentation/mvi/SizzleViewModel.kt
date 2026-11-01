package com.sizzlesense.ai.presentation.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sizzlesense.ai.data.repository.SizzleCognitiveRepository
import com.sizzlesense.ai.domain.usecase.AnalyzeCookingSessionUseCase
import com.sizzlesense.ai.domain.usecase.CaptureLiveTapoSessionUseCase
import com.sizzlesense.ai.domain.usecase.RunHumanLearningEvalUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Presentation Layer MVI ViewModel.
 *
 * Adheres to:
 * - SRP: Handles Intent dispatching and unidirectional state emission via [SizzleReducer].
 * - DIP: Depends on Domain Use Cases ([AnalyzeCookingSessionUseCase], [CaptureLiveTapoSessionUseCase],
 *   [RunHumanLearningEvalUseCase]) rather than concrete Data layer implementations.
 * - DRY: Centralizes coroutine launch & error reduction in [launchReducedTask].
 */
class SizzleViewModel(
    private val analyzeCookingSession: AnalyzeCookingSessionUseCase = AnalyzeCookingSessionUseCase(
        SizzleCognitiveRepository()
    ),
    private val captureLiveTapoSession: CaptureLiveTapoSessionUseCase = CaptureLiveTapoSessionUseCase(
        SizzleCognitiveRepository()
    ),
    private val runHumanLearningEval: RunHumanLearningEvalUseCase = RunHumanLearningEvalUseCase(
        SizzleCognitiveRepository()
    ),
) : ViewModel() {

    private val _uiState = MutableStateFlow(SizzleUiState())
    val uiState: StateFlow<SizzleUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<SizzleEffect>()
    val effects: SharedFlow<SizzleEffect> = _effects.asSharedFlow()

    fun dispatch(intent: SizzleIntent) {
        when (intent) {
            is SizzleIntent.SelectTab -> applyResult(SizzleReductionResult.TabChanged(intent.tab))

            is SizzleIntent.UpdateBackendUrl -> applyResult(
                SizzleReductionResult.BackendUrlUpdated(intent.url)
            )

            is SizzleIntent.UpdateTapoConfig -> {
                val updated = _uiState.value.tapoConfig.copy(
                    ipAddress = intent.ipAddress,
                    username = intent.username,
                    password = intent.password,
                    useSubStream = intent.useSubStream,
                )
                applyResult(SizzleReductionResult.TapoConfigUpdated(updated))
            }

            is SizzleIntent.SetTapoStreamConnected -> {
                val updated = _uiState.value.tapoConfig.copy(isConnected = intent.connected)
                applyResult(SizzleReductionResult.TapoConfigUpdated(updated))
            }

            is SizzleIntent.OnLiveFrameCaptured -> {
                applyResult(
                    SizzleReductionResult.FrameCaptured(
                        base64Jpeg = intent.base64Jpeg,
                        label = intent.timestampLabel,
                    )
                )
            }

            is SizzleIntent.SelectArchetype -> {
                applyResult(SizzleReductionResult.ArchetypeSelected(intent.archetype))
            }

            is SizzleIntent.SelectAttemptNumber -> {
                applyResult(SizzleReductionResult.AttemptSelected(intent.attemptNumber))
            }

            is SizzleIntent.UpdateReflectionText -> {
                applyResult(SizzleReductionResult.ReflectionUpdated(intent.text))
            }

            SizzleIntent.AnalyzeCurrentSession -> runCognitiveAnalysis()

            SizzleIntent.CaptureFromBackendTapoAndAnalyze -> runBackendTapoLiveCaptureAndAnalyze()

            SizzleIntent.RunHumanLearningEvalSuite -> runEvaluationSuite()

            SizzleIntent.ClearError -> applyResult(SizzleReductionResult.ErrorCleared)
        }
    }

    private fun applyResult(result: SizzleReductionResult) {
        _uiState.value = SizzleReducer.reduce(_uiState.value, result)
    }

    /**
     * DRY coroutine helper: emits [startResult], executes [block], and reduces any exception
     * into [SizzleReductionResult.ErrorOccurred] with [fallbackErrorMessage].
     */
    private fun launchReducedTask(
        startResult: SizzleReductionResult,
        fallbackErrorMessage: String,
        block: suspend (SizzleUiState) -> SizzleReductionResult,
    ) {
        val snapshot = _uiState.value
        viewModelScope.launch {
            applyResult(startResult)
            try {
                applyResult(block(snapshot))
            } catch (e: Exception) {
                applyResult(
                    SizzleReductionResult.ErrorOccurred(
                        e.message ?: fallbackErrorMessage
                    )
                )
            }
        }
    }

    private fun runCognitiveAnalysis() {
        launchReducedTask(
            startResult = SizzleReductionResult.AnalysisStarted(
                "Running 4-Stage Cognitive Pipeline (Stage 1 Physical -> Stage 4 Sensory)..."
            ),
            fallbackErrorMessage = "Failed to analyze cooking session",
        ) { state ->
            val response = analyzeCookingSession(
                baseUrl = state.backendBaseUrl,
                archetypeId = state.selectedArchetype.id,
                attemptNumber = state.currentAttemptNumber,
                learnerReflection = state.learnerReflectionText,
                capturedFrameBase64 = state.capturedFrameBase64,
            )
            SizzleReductionResult.AnalysisCompleted(response)
        }
    }

    private fun runBackendTapoLiveCaptureAndAnalyze() {
        val ip = _uiState.value.tapoConfig.ipAddress
        launchReducedTask(
            startResult = SizzleReductionResult.AnalysisStarted(
                "Grabbing live RTSP frame from Tapo C100 ($ip:554) & running Gemini 3..."
            ),
            fallbackErrorMessage = "Live Tapo capture failed",
        ) { state ->
            val response = captureLiveTapoSession(
                baseUrl = state.backendBaseUrl,
                tapoConfig = state.tapoConfig,
                attemptNumber = state.currentAttemptNumber,
                reflectionText = state.learnerReflectionText,
            )
            SizzleReductionResult.AnalysisCompleted(response)
        }
    }

    private fun runEvaluationSuite() {
        launchReducedTask(
            startResult = SizzleReductionResult.EvalSuiteStarted,
            fallbackErrorMessage = "Eval benchmark execution failed",
        ) { state ->
            val report = runHumanLearningEval(baseUrl = state.backendBaseUrl)
            SizzleReductionResult.EvalSuiteCompleted(report)
        }
    }
}
