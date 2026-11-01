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
