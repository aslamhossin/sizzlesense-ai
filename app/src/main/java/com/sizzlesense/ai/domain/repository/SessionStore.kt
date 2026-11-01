package com.sizzlesense.ai.domain.repository

import com.sizzlesense.ai.domain.model.coaching.CognitiveCoachingResponse
import com.sizzlesense.ai.domain.model.eval.EvalSuiteReport
import com.sizzlesense.ai.domain.model.session.LearnerArchetypeOption
import com.sizzlesense.ai.domain.model.session.SessionSnapshot
import com.sizzlesense.ai.domain.model.camera.TapoCameraConfig
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
