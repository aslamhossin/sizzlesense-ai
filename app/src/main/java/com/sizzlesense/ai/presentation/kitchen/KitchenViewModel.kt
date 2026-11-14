package com.sizzlesense.ai.presentation.kitchen

import com.sizzlesense.ai.core.result.AppResult
import com.sizzlesense.ai.core.ui.BaseViewModel
import com.sizzlesense.ai.core.ui.TaskStatus
import com.sizzlesense.ai.domain.model.coaching.CognitiveCoachingResponse
import com.sizzlesense.ai.domain.model.session.SessionSnapshot
import com.sizzlesense.ai.domain.repository.SessionStore
import com.sizzlesense.ai.domain.usecase.AnalyzeCookingSessionUseCase
import com.sizzlesense.ai.domain.usecase.CaptureLiveTapoSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

@HiltViewModel
class KitchenViewModel @Inject constructor(
    private val sessionStore: SessionStore,
    private val analyzeCookingSession: AnalyzeCookingSessionUseCase,
    private val captureLiveTapoSession: CaptureLiveTapoSessionUseCase
) : BaseViewModel<KitchenUiState, KitchenUiIntent, KitchenUiEffect>() {

    private val taskStatus = MutableStateFlow(TaskStatus())

    override val uiState: StateFlow<KitchenUiState> = combine(
        sessionStore.sessionState,
        taskStatus
    ) { session, status ->
        session.toKitchenUiState(status)
    }.stateWhileSubscribed(
        initialValue = sessionStore.sessionState.value.toKitchenUiState(taskStatus.value)
    )

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

            is KitchenUiIntent.OnAudioSampleCaptured -> {
                sessionStore.recordCapturedAudio(
                    base64Wav = intent.base64Wav,
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
                taskStatus.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun runCognitiveAnalysis() {
        executePipelineTask(
            stageLabel = "Analyzing your pan & coaching..."
        ) { snapshot ->
            analyzeCookingSession(
                baseUrl = snapshot.backendBaseUrl,
                archetypeId = snapshot.selectedArchetype.id,
                attemptNumber = snapshot.currentAttemptNumber,
                learnerReflection = snapshot.learnerReflectionText,
                capturedFrameBase64 = snapshot.capturedFrameBase64,
                capturedAudioWavBase64 = snapshot.capturedAudioWavBase64
            )
        }
    }

    private fun runBackendTapoLiveCapture() {
        executePipelineTask(
            stageLabel = "Capturing stove camera & analyzing..."
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
        block: suspend (SessionSnapshot) -> AppResult<CognitiveCoachingResponse>
    ) {
        val snapshot = sessionStore.sessionState.value
        taskStatus.launchGuardedTask(
            runningLabel = stageLabel,
            completedLabel = "Done",
            failedLabel = "Unable to analyze",
            effectOnSuccess = KitchenUiEffect.NavigateToCoaching,
            onSuccess = { response -> sessionStore.recordCoachingResponse(response) }
        ) {
            block(snapshot)
        }
    }

    private fun SessionSnapshot.toKitchenUiState(status: TaskStatus) = KitchenUiState(
        backendBaseUrl = backendBaseUrl,
        tapoConfig = tapoConfig,
        capturedFrameLabel = capturedFrameLabel,
        capturedAudioLabel = capturedAudioLabel,
        selectedArchetype = selectedArchetype,
        currentAttemptNumber = currentAttemptNumber,
        learnerReflectionText = learnerReflectionText,
        isAnalyzing = status.isRunning,
        activePipelineStageLabel = status.label,
        errorMessage = status.errorMessage
    )
}
