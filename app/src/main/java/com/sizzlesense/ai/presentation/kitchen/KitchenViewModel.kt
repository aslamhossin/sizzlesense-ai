package com.sizzlesense.ai.presentation.kitchen

import androidx.lifecycle.viewModelScope
import com.sizzlesense.ai.core.ui.BaseViewModel
import com.sizzlesense.ai.domain.model.coaching.CognitiveCoachingResponse
import com.sizzlesense.ai.domain.model.session.SessionSnapshot
import com.sizzlesense.ai.domain.repository.SessionStore
import com.sizzlesense.ai.domain.usecase.AnalyzeCookingSessionUseCase
import com.sizzlesense.ai.domain.usecase.CaptureLiveTapoSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@HiltViewModel
class KitchenViewModel @Inject constructor(
    private val sessionStore: SessionStore,
    private val analyzeCookingSession: AnalyzeCookingSessionUseCase,
    private val captureLiveTapoSession: CaptureLiveTapoSessionUseCase
) : BaseViewModel<KitchenUiState, KitchenUiIntent, KitchenUiEffect>(KitchenUiState()) {

    init {
        sessionStore.sessionState
            .onEach { session ->
                updateState { state ->
                    state.copy(
                        backendBaseUrl = session.backendBaseUrl,
                        tapoConfig = session.tapoConfig,
                        capturedFrameBase64 = session.capturedFrameBase64,
                        capturedFrameLabel = session.capturedFrameLabel,
                        selectedArchetype = session.selectedArchetype,
                        currentAttemptNumber = session.currentAttemptNumber,
                        learnerReflectionText = session.learnerReflectionText
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onIntent(intent: KitchenUiIntent) {
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
                updateState { it.copy(errorMessage = null) }
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
        block: suspend (SessionSnapshot) -> CognitiveCoachingResponse
    ) {
        val snapshot = sessionStore.sessionState.value
        updateState {
            it.copy(
                isAnalyzing = true,
                activePipelineStageLabel = stageLabel,
                errorMessage = null
            )
        }
        launchTask(
            onError = { error ->
                updateState {
                    it.copy(
                        isAnalyzing = false,
                        activePipelineStageLabel = "Error occurred",
                        errorMessage = error.message ?: fallbackError
                    )
                }
            }
        ) {
            val response = block(snapshot)
            sessionStore.recordCoachingResponse(response)
            updateState {
                it.copy(
                    isAnalyzing = false,
                    activePipelineStageLabel = "Completed (${response.reasoningModelUsed})",
                    errorMessage = null
                )
            }
            emitEffect(KitchenUiEffect.NavigateToCoaching)
        }
    }
}
