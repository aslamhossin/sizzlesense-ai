package com.sizzlesense.ai.domain.model.session

import com.sizzlesense.ai.domain.model.eval.EvalSuiteReport

import com.sizzlesense.ai.domain.model.camera.TapoCameraConfig

import com.sizzlesense.ai.domain.model.coaching.CognitiveCoachingResponse

data class SessionSnapshot(
    val backendBaseUrl: String,
    val tapoConfig: TapoCameraConfig,
    val capturedFrameBase64: String? = null,
    val capturedFrameLabel: String = "No live frame captured yet",
    val selectedArchetype: LearnerArchetypeOption = LearnerArchetypeOption.MOISTURE_BLINDNESS,
    val currentAttemptNumber: Int = 1,
    val learnerReflectionText: String =
        LearnerArchetypeOption.MOISTURE_BLINDNESS.presetReflections[1].orEmpty(),
    val latestCoachingResponse: CognitiveCoachingResponse? = null,
    val attemptHistory: Map<Int, CognitiveCoachingResponse> = emptyMap(),
    val evalSuiteReport: EvalSuiteReport? = null
)
