package com.sizzlesense.ai.presentation.mvi

import com.sizzlesense.ai.BuildConfig
import com.sizzlesense.ai.domain.model.CognitiveCoachingResponse
import com.sizzlesense.ai.domain.model.EvalSuiteReport
import com.sizzlesense.ai.domain.model.TapoCameraConfig

data class SizzleUiState(
    val activeTab: AppTab = AppTab.KITCHEN_LIVE,
    val backendBaseUrl: String = BuildConfig.DEFAULT_BACKEND_URL,
    val tapoConfig: TapoCameraConfig = TapoCameraConfig(ipAddress = BuildConfig.DEFAULT_TAPO_IP),
    val capturedFrameBase64: String? = null,
    val capturedFrameLabel: String = "No live frame captured yet",
    val selectedArchetype: LearnerArchetypeOption = LearnerArchetypeOption.MOISTURE_BLINDNESS,
    val currentAttemptNumber: Int = 1,
    val learnerReflectionText: String =
        LearnerArchetypeOption.MOISTURE_BLINDNESS.presetReflections[1].orEmpty(),
    val isAnalyzing: Boolean = false,
    val activePipelineStageLabel: String = "Idle — Ready for Kitchen Session",
    val latestCoachingResponse: CognitiveCoachingResponse? = null,
    val attemptHistory: Map<Int, CognitiveCoachingResponse> = emptyMap(),
    val isRunningEvals: Boolean = false,
    val evalSuiteReport: EvalSuiteReport? = null,
    val errorMessage: String? = null
)

sealed interface SizzleIntent {
    data class SelectTab(val tab: AppTab) : SizzleIntent
    data class UpdateBackendUrl(val url: String) : SizzleIntent
    data class UpdateTapoConfig(
        val ipAddress: String,
        val username: String,
        val password: String,
        val useSubStream: Boolean
    ) : SizzleIntent
    data class SetTapoStreamConnected(val connected: Boolean) : SizzleIntent
    data class OnLiveFrameCaptured(
        val base64Jpeg: String,
        val timestampLabel: String
    ) : SizzleIntent
    data class SelectArchetype(val archetype: LearnerArchetypeOption) : SizzleIntent
    data class SelectAttemptNumber(val attemptNumber: Int) : SizzleIntent
    data class UpdateReflectionText(val text: String) : SizzleIntent
    data object AnalyzeCurrentSession : SizzleIntent
    data object CaptureFromBackendTapoAndAnalyze : SizzleIntent
    data object RunHumanLearningEvalSuite : SizzleIntent
    data object ClearError : SizzleIntent
}

sealed interface SizzleReductionResult {
    data class TabChanged(val tab: AppTab) : SizzleReductionResult
    data class BackendUrlUpdated(val url: String) : SizzleReductionResult
    data class TapoConfigUpdated(val config: TapoCameraConfig) : SizzleReductionResult
    data class FrameCaptured(val base64Jpeg: String, val label: String) : SizzleReductionResult
    data class ArchetypeSelected(val archetype: LearnerArchetypeOption) : SizzleReductionResult
    data class AttemptSelected(val attemptNumber: Int) : SizzleReductionResult
    data class ReflectionUpdated(val text: String) : SizzleReductionResult
    data class AnalysisStarted(val stageLabel: String) : SizzleReductionResult
    data class AnalysisCompleted(val response: CognitiveCoachingResponse) : SizzleReductionResult
    data object EvalSuiteStarted : SizzleReductionResult
    data class EvalSuiteCompleted(val report: EvalSuiteReport) : SizzleReductionResult
    data class ErrorOccurred(val message: String) : SizzleReductionResult
    data object ErrorCleared : SizzleReductionResult
}

sealed interface SizzleEffect {
    data class ShowToast(val message: String) : SizzleEffect
}
