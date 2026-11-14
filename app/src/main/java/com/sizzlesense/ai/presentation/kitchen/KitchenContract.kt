package com.sizzlesense.ai.presentation.kitchen

import com.sizzlesense.ai.BuildConfig
import com.sizzlesense.ai.domain.model.session.LearnerArchetypeOption
import com.sizzlesense.ai.domain.model.camera.TapoCameraConfig

data class KitchenUiState(
    val backendBaseUrl: String = BuildConfig.DEFAULT_BACKEND_URL,
    val tapoConfig: TapoCameraConfig = TapoCameraConfig(ipAddress = BuildConfig.DEFAULT_TAPO_IP),
    val capturedFrameLabel: String = "",
    val capturedAudioLabel: String = "Record pan sound or pick a sample",
    val selectedArchetype: LearnerArchetypeOption = LearnerArchetypeOption.MOISTURE_BLINDNESS,
    val currentAttemptNumber: Int = 1,
    val learnerReflectionText: String =
        LearnerArchetypeOption.MOISTURE_BLINDNESS.presetReflections[1].orEmpty(),
    val isAnalyzing: Boolean = false,
    val activePipelineStageLabel: String = "Ready",
    val errorMessage: String? = null
)

sealed interface KitchenUiIntent {
    data class UpdateBackendUrl(val url: String) : KitchenUiIntent
    data class UpdateTapoConfig(
        val ipAddress: String,
        val username: String,
        val password: String,
        val useSubStream: Boolean
    ) : KitchenUiIntent
    data class SetTapoStreamConnected(val connected: Boolean) : KitchenUiIntent
    data class OnLiveFrameCaptured(
        val base64Jpeg: String,
        val timestampLabel: String
    ) : KitchenUiIntent
    data class OnAudioSampleCaptured(
        val base64Wav: String,
        val timestampLabel: String
    ) : KitchenUiIntent
    data class SelectArchetype(val archetype: LearnerArchetypeOption) : KitchenUiIntent
    data class UpdateReflectionText(val text: String) : KitchenUiIntent
    data object AnalyzeCurrentSession : KitchenUiIntent
    data object CaptureFromBackendTapoAndAnalyze : KitchenUiIntent
    data object ClearError : KitchenUiIntent
}

sealed interface KitchenUiEffect {
    data object NavigateToCoaching : KitchenUiEffect
}
