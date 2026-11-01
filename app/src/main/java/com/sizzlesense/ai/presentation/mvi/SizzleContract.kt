package com.sizzlesense.ai.presentation.mvi

import com.sizzlesense.ai.BuildConfig
import com.sizzlesense.ai.domain.model.CognitiveCoachingResponse
import com.sizzlesense.ai.domain.model.EvalSuiteReport
import com.sizzlesense.ai.domain.model.TapoCameraConfig

enum class AppTab(val label: String) {
    KITCHEN_LIVE("1. Kitchen & RTSP"),
    COACHING_LOOP("2. 3-Attempt Coach"),
    AI_XRAY("3. AI X-Ray"),
    EVAL_BENCH("4. Eval Bench")
}

enum class LearnerArchetypeOption(
    val id: String,
    val title: String,
    val jdSubtitle: String,
    val presetReflections: Map<Int, String>
) {
    MOISTURE_BLINDNESS(
        id = "moisture_blindness",
        title = "Learner A: Moisture Blindness",
        jdSubtitle = "Crowds pan & stirs in liquid without evaporating water",
        presetReflections = mapOf(
            1 to "I kept stirring the onions for 8 minutes on medium heat, but they stayed pale and watery instead of browning.",
            2 to "I spread the onions out and waited until the wet bubbling sound turned into a crisp sizzle before stirring!",
            3 to "As soon as I heard the sizzle sharpen after moisture cooked off, I adjusted heat to keep steady golden browning."
        )
    ),
    HEAT_ANXIETY(
        id = "heat_anxiety",
        title = "Learner B: Heat Anxiety",
        jdSubtitle = "Fears burning when steam rises & dials heat too low",
        presetReflections = mapOf(
            1 to "It started steaming a lot right away so I turned the heat way down so it wouldn't burn, and nothing browned.",
            2 to "I saw the white steam again, recognized it was harmless water vapor, and kept the heat steady until golden.",
            3 to "I used the steady applause sizzle sound and clear oil shimmer to ride medium-high heat confidently."
        )
    ),
    PERCEPTION_BLINDSPOT(
        id = "perception_blindspot",
        title = "Learner C: Perception Blindspot",
        jdSubtitle = "Believes soft pale onions are browned after 5-min timer",
        presetReflections = mapOf(
            1 to "I sautéed the onions for 5 minutes just like the recipe said and they browned really nicely!",
            2 to "I ignored the 5-minute timer this time and waited until I saw actual amber-gold color on the edges and pan fond.",
            3 to "Now I can immediately tell the difference between sweaty translucent onions and true nutty golden browning."
        )
    )
}

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
