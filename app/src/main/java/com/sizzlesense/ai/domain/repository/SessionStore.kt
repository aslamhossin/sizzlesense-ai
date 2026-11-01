package com.sizzlesense.ai.domain.repository

import com.sizzlesense.ai.domain.model.CognitiveCoachingResponse
import com.sizzlesense.ai.domain.model.EvalSuiteReport
import com.sizzlesense.ai.domain.model.LearnerArchetypeOption
import com.sizzlesense.ai.domain.model.SessionSnapshot
import com.sizzlesense.ai.domain.model.TapoCameraConfig
import kotlinx.coroutines.flow.StateFlow

interface SessionStore {

    val sessionState: StateFlow<SessionSnapshot>

    fun updateBackendUrl(url: String)

    fun updateTapoConfig(config: TapoCameraConfig)

    fun recordCapturedFrame(
        base64Jpeg: String,
        timestampLabel: String
    )

    fun selectArchetype(archetype: LearnerArchetypeOption)

    fun selectAttemptNumber(attemptNumber: Int)

    fun updateReflectionText(text: String)

    fun recordCoachingResponse(response: CognitiveCoachingResponse)

    fun recordEvalSuiteReport(report: EvalSuiteReport)
}
