package com.sizzlesense.ai.presentation.mvi

import com.sizzlesense.ai.domain.model.CognitiveCoachingResponse
import com.sizzlesense.ai.domain.model.EvalSuiteReport
import com.sizzlesense.ai.domain.model.TapoCameraConfig

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
