package com.sizzlesense.ai.presentation.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sizzlesense.ai.data.repository.SizzleCognitiveRepository
import com.sizzlesense.ai.domain.model.SessionAnalyzeRequest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SizzleViewModel(
    private val repository: SizzleCognitiveRepository = SizzleCognitiveRepository(),
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

    private fun runCognitiveAnalysis() {
        val state = _uiState.value
        viewModelScope.launch {
            applyResult(
                SizzleReductionResult.AnalysisStarted(
                    "Running 4-Stage Cognitive Pipeline (Stage 1 Physical -> Stage 4 Sensory)..."
                )
            )
            try {
                val frames = state.capturedFrameBase64?.let { listOf(it) } ?: emptyList()
                val req = SessionAnalyzeRequest(
                    learnerId = "android_${state.selectedArchetype.id}",
                    dishId = "sauteed_onions",
                    attemptNumber = state.currentAttemptNumber,
                    learnerReflection = state.learnerReflectionText,
                    archetypeHint = state.selectedArchetype.id,
                    keyframeBase64List = frames,
                )
                val response = repository.analyzeSession(state.backendBaseUrl, req)
                applyResult(SizzleReductionResult.AnalysisCompleted(response))
            } catch (e: Exception) {
                applyResult(
                    SizzleReductionResult.ErrorOccurred(
                        e.message ?: "Failed to analyze cooking session"
                    )
                )
            }
        }
    }

    private fun runBackendTapoLiveCaptureAndAnalyze() {
        val state = _uiState.value
        viewModelScope.launch {
            applyResult(
                SizzleReductionResult.AnalysisStarted(
                    "Grabbing live RTSP frame from Tapo C100 (${state.tapoConfig.ipAddress}:554) & running Gemini 3..."
                )
            )
            try {
                val response = repository.captureFromBackendTapoAndAnalyze(
                    baseUrl = state.backendBaseUrl,
                    tapoConfig = state.tapoConfig,
                    attemptNumber = state.currentAttemptNumber,
                    reflectionText = state.learnerReflectionText,
                )
                applyResult(SizzleReductionResult.AnalysisCompleted(response))
            } catch (e: Exception) {
                applyResult(
                    SizzleReductionResult.ErrorOccurred(
                        e.message ?: "Live Tapo capture failed"
                    )
                )
            }
        }
    }

    private fun runEvaluationSuite() {
        val state = _uiState.value
        viewModelScope.launch {
            applyResult(SizzleReductionResult.EvalSuiteStarted)
            try {
                val report = repository.runEvaluationBenchmark(state.backendBaseUrl)
                applyResult(SizzleReductionResult.EvalSuiteCompleted(report))
            } catch (e: Exception) {
                applyResult(
                    SizzleReductionResult.ErrorOccurred(
                        e.message ?: "Eval benchmark execution failed"
                    )
                )
            }
        }
    }
}
