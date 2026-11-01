package com.sizzlesense.ai.presentation.kitchen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sizzlesense.ai.domain.model.SessionSnapshot
import com.sizzlesense.ai.domain.repository.SessionStore
import com.sizzlesense.ai.domain.usecase.AnalyzeCookingSessionUseCase
import com.sizzlesense.ai.domain.usecase.CaptureLiveTapoSessionUseCase
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
class KitchenViewModel @Inject constructor(
    private val sessionStore: SessionStore,
    private val analyzeCookingSession: AnalyzeCookingSessionUseCase,
    private val captureLiveTapoSession: CaptureLiveTapoSessionUseCase
) : ViewModel() {

    private data class ExecutionStatus(
        val isAnalyzing: Boolean = false,
        val stageLabel: String = "Idle — Ready for Kitchen Session",
        val errorMessage: String? = null
    )

    private val executionStatus = MutableStateFlow(ExecutionStatus())

    private val _effects = MutableSharedFlow<KitchenUiEffect>()
    val effects: SharedFlow<KitchenUiEffect> = _effects.asSharedFlow()

    val uiState: StateFlow<KitchenUiState> = combine(
        sessionStore.sessionState,
        executionStatus
    ) { session, exec ->
        KitchenUiState(
            backendBaseUrl = session.backendBaseUrl,
            tapoConfig = session.tapoConfig,
            capturedFrameBase64 = session.capturedFrameBase64,
            capturedFrameLabel = session.capturedFrameLabel,
            selectedArchetype = session.selectedArchetype,
            currentAttemptNumber = session.currentAttemptNumber,
            learnerReflectionText = session.learnerReflectionText,
            isAnalyzing = exec.isAnalyzing,
            activePipelineStageLabel = exec.stageLabel,
            errorMessage = exec.errorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = KitchenUiState()
    )

    fun onIntent(intent: KitchenUiIntent) {
        when (intent) {
            is KitchenUiIntent.UpdateBackendUrl -> {
                sessionStore.updateBackendUrl(intent.url)
            }

            is KitchenUiIntent.UpdateTapoConfig -> {
                val current = sessionStore.sessionState.value.tapoConfig
                sessionStore.updateTapoConfig(
                    current.copy(
                        ipAddress = intent.ipAddress,
                        username = intent.username,
                        password = intent.password,
                        useSubStream = intent.useSubStream
                    )
                )
            }

            is KitchenUiIntent.SetTapoStreamConnected -> {
                val current = sessionStore.sessionState.value.tapoConfig
                sessionStore.updateTapoConfig(current.copy(isConnected = intent.connected))
            }

            is KitchenUiIntent.OnLiveFrameCaptured -> {
                sessionStore.recordCapturedFrame(
                    base64Jpeg = intent.base64Jpeg,
                    timestampLabel = intent.timestampLabel
                )
            }

            is KitchenUiIntent.SelectArchetype -> {
                sessionStore.selectArchetype(intent.archetype)
            }

            is KitchenUiIntent.UpdateReflectionText -> {
                sessionStore.updateReflectionText(intent.text)
            }

            KitchenUiIntent.AnalyzeCurrentSession -> runCognitiveAnalysis()

            KitchenUiIntent.CaptureFromBackendTapoAndAnalyze -> runBackendTapoLiveCapture()

            KitchenUiIntent.ClearError -> {
                executionStatus.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun runCognitiveAnalysis() {
        executePipelineTask(
            stageLabel = "Running 4-Stage Cognitive Pipeline (Stage 1 Physical -> Stage 4 Sensory)...",
            fallbackError = "Failed to analyze cooking session"
        ) { snapshot ->
            analyzeCookingSession(
                baseUrl = snapshot.backendBaseUrl,
                archetypeId = snapshot.selectedArchetype.id,
                attemptNumber = snapshot.currentAttemptNumber,
                learnerReflection = snapshot.learnerReflectionText,
                capturedFrameBase64 = snapshot.capturedFrameBase64
            )
        }
    }

    private fun runBackendTapoLiveCapture() {
        val ip = sessionStore.sessionState.value.tapoConfig.ipAddress
        executePipelineTask(
            stageLabel = "Grabbing live RTSP frame from Tapo C100 ($ip:554) & running Gemini 3...",
            fallbackError = "Live Tapo capture failed"
        ) { snapshot ->
            captureLiveTapoSession(
                baseUrl = snapshot.backendBaseUrl,
                tapoConfig = snapshot.tapoConfig,
                attemptNumber = snapshot.currentAttemptNumber,
                reflectionText = snapshot.learnerReflectionText
            )
        }
    }

    private fun executePipelineTask(
        stageLabel: String,
        fallbackError: String,
        block: suspend (SessionSnapshot) -> com.sizzlesense.ai.domain.model.CognitiveCoachingResponse
    ) {
        val snapshot = sessionStore.sessionState.value
        viewModelScope.launch {
            executionStatus.update {
                ExecutionStatus(
                    isAnalyzing = true,
                    stageLabel = stageLabel,
                    errorMessage = null
                )
            }
            try {
                val response = block(snapshot)
                sessionStore.recordCoachingResponse(response)
                executionStatus.update {
                    ExecutionStatus(
                        isAnalyzing = false,
                        stageLabel = "Completed (${response.reasoningModelUsed})",
                        errorMessage = null
                    )
                }
                _effects.emit(KitchenUiEffect.NavigateToCoaching)
            } catch (e: Exception) {
                executionStatus.update {
                    ExecutionStatus(
                        isAnalyzing = false,
                        stageLabel = "Error occurred",
                        errorMessage = e.message ?: fallbackError
                    )
                }
            }
        }
    }
}
