package com.sizzlesense.ai.presentation.kitchen

import com.sizzlesense.ai.BuildConfig
import com.sizzlesense.ai.core.result.AppResult
import com.sizzlesense.ai.core.ui.BaseViewModel
import com.sizzlesense.ai.core.ui.TaskStatus
import com.sizzlesense.ai.domain.model.camera.CameraDiscoveryInfo
import com.sizzlesense.ai.domain.model.coaching.CognitiveCoachingResponse
import com.sizzlesense.ai.domain.model.session.SessionSnapshot
import com.sizzlesense.ai.domain.repository.SessionStore
import com.sizzlesense.ai.domain.usecase.AnalyzeCookingSessionUseCase
import com.sizzlesense.ai.domain.usecase.CaptureLiveTapoSessionUseCase
import com.sizzlesense.ai.domain.usecase.DiscoverCameraAndServerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

private data class DiscoveryUiStatus(
    val isDiscovering: Boolean = false,
    val statusMessage: String? = null
)

@HiltViewModel
class KitchenViewModel @Inject constructor(
    private val sessionStore: SessionStore,
    private val analyzeCookingSession: AnalyzeCookingSessionUseCase,
    private val captureLiveTapoSession: CaptureLiveTapoSessionUseCase,
    private val discoverCameraAndServer: DiscoverCameraAndServerUseCase
) : BaseViewModel<KitchenUiState, KitchenUiIntent, KitchenUiEffect>() {

    private val taskStatus = MutableStateFlow(TaskStatus())
    private val discoveryStatus = MutableStateFlow(DiscoveryUiStatus())

    init {
        runAutoDiscovery(userInitiated = false)
    }

    override val uiState: StateFlow<KitchenUiState> = combine(
        sessionStore.sessionState,
        taskStatus,
        discoveryStatus
    ) { session, status, discovery ->
        session.toKitchenUiState(status, discovery)
    }.stateWhileSubscribed(
        initialValue = sessionStore.sessionState.value.toKitchenUiState(
            status = taskStatus.value,
            discovery = discoveryStatus.value
        )
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

            KitchenUiIntent.AutoDetectSettings -> {
                runAutoDiscovery(userInitiated = true)
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

    private fun runAutoDiscovery(userInitiated: Boolean) {
        if (discoveryStatus.value.isDiscovering) return
        val snapshot = sessionStore.sessionState.value
        discoveryStatus.update {
            it.copy(
                isDiscovering = true,
                statusMessage = if (userInitiated) "Detecting camera & server..." else it.statusMessage
            )
        }
        if (userInitiated) {
            taskStatus.update { it.copy(errorMessage = null) }
        }

        launchResultTask(
            onSuccess = { info -> applyDiscoveryResult(info) },
            onError = { appError ->
                discoveryStatus.value = DiscoveryUiStatus(
                    isDiscovering = false,
                    statusMessage = if (userInitiated) "Server unreachable on local Wi-Fi" else null
                )
                if (userInitiated) {
                    taskStatus.update { it.copy(errorMessage = appError.readableMessage) }
                }
            },
            block = {
                discoverCameraAndServer(
                    baseUrl = snapshot.backendBaseUrl,
                    preferredCameraIp = snapshot.tapoConfig.ipAddress
                )
            }
        )
    }

    private fun applyDiscoveryResult(info: CameraDiscoveryInfo) {
        val resolvedUrl = info.resolvedBackendUrl.ifBlank { info.advertisedBackendUrl }
        if (resolvedUrl.isNotBlank()) {
            sessionStore.updateBackendUrl(resolvedUrl)
        }
        val discoveredIp = info.cameraIp?.trim().orEmpty()
        val currentTapo = sessionStore.sessionState.value.tapoConfig
        if (discoveredIp.isNotBlank() && info.cameraReachable) {
            sessionStore.updateTapoConfig(
                currentTapo.copy(
                    ipAddress = discoveredIp,
                    port = info.cameraPort,
                    username = currentTapo.username.ifBlank { BuildConfig.DEFAULT_TAPO_USERNAME },
                    password = currentTapo.password.ifBlank { BuildConfig.DEFAULT_TAPO_PASSWORD }
                )
            )
            discoveryStatus.value = DiscoveryUiStatus(
                isDiscovering = false,
                statusMessage = "Auto-synced • Camera $discoveredIp online"
            )
        } else {
            discoveryStatus.value = DiscoveryUiStatus(
                isDiscovering = false,
                statusMessage = "Server ready • Camera offline on LAN"
            )
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

    private fun SessionSnapshot.toKitchenUiState(
        status: TaskStatus,
        discovery: DiscoveryUiStatus
    ) = KitchenUiState(
        backendBaseUrl = backendBaseUrl,
        tapoConfig = tapoConfig,
        capturedFrameLabel = capturedFrameLabel,
        capturedAudioLabel = capturedAudioLabel,
        selectedArchetype = selectedArchetype,
        currentAttemptNumber = currentAttemptNumber,
        learnerReflectionText = learnerReflectionText,
        isAnalyzing = status.isRunning,
        activePipelineStageLabel = status.label,
        isDiscoveringSettings = discovery.isDiscovering,
        setupStatusMessage = discovery.statusMessage,
        errorMessage = status.errorMessage
    )
}
