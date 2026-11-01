package com.sizzlesense.ai.presentation.mvi

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
