package com.sizzlesense.ai.data.session

import com.sizzlesense.ai.BuildConfig
import com.sizzlesense.ai.domain.model.camera.TapoCameraConfig
import com.sizzlesense.ai.domain.model.coaching.CognitiveCoachingResponse
import com.sizzlesense.ai.domain.model.eval.EvalSuiteReport
import com.sizzlesense.ai.domain.model.session.LearnerArchetypeOption
import com.sizzlesense.ai.domain.model.session.SessionSnapshot
import com.sizzlesense.ai.domain.repository.SessionStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

@Singleton
class InMemorySessionStore @Inject constructor() : SessionStore {

    override val sessionState: StateFlow<SessionSnapshot>
        field = MutableStateFlow(
            SessionSnapshot(
                backendBaseUrl = BuildConfig.DEFAULT_BACKEND_URL,
                tapoConfig = TapoCameraConfig(ipAddress = BuildConfig.DEFAULT_TAPO_IP)
            )
        )

    override fun updateBackendUrl(url: String) {
        sessionState.update { current ->
            current.copy(backendBaseUrl = url.trim())
        }
    }

    override fun updateTapoConfig(config: TapoCameraConfig) {
        sessionState.update { current ->
            current.copy(tapoConfig = config)
        }
    }

    override fun recordCapturedFrame(
        base64Jpeg: String,
        timestampLabel: String
    ) {
        sessionState.update { current ->
            current.copy(
                capturedFrameBase64 = base64Jpeg,
                capturedFrameLabel = timestampLabel
            )
        }
    }

    override fun selectArchetype(archetype: LearnerArchetypeOption) {
        sessionState.update { current ->
            val presetText = archetype
                .presetReflections[current.currentAttemptNumber]
                .orEmpty()
            current.copy(
                selectedArchetype = archetype,
                learnerReflectionText = presetText,
                attemptHistory = emptyMap(),
                latestCoachingResponse = null
            )
        }
    }

    override fun selectAttemptNumber(attemptNumber: Int) {
        val clamped = attemptNumber.coerceIn(1, 3)
        sessionState.update { current ->
            val presetText = current.selectedArchetype
                .presetReflections[clamped]
                .orEmpty()
            val existingAttemptResponse = current.attemptHistory[clamped]
            current.copy(
                currentAttemptNumber = clamped,
                learnerReflectionText = presetText,
                latestCoachingResponse = existingAttemptResponse
                    ?: current.latestCoachingResponse
            )
        }
    }

    override fun updateReflectionText(text: String) {
        sessionState.update { current ->
            current.copy(learnerReflectionText = text)
        }
    }

    override fun recordCoachingResponse(response: CognitiveCoachingResponse) {
        sessionState.update { current ->
            val updatedHistory = current.attemptHistory.toMutableMap().apply {
                put(response.attemptNumber, response)
            }
            current.copy(
                latestCoachingResponse = response,
                attemptHistory = updatedHistory
            )
        }
    }

    override fun recordEvalSuiteReport(report: EvalSuiteReport) {
        sessionState.update { current ->
            current.copy(evalSuiteReport = report)
        }
    }
}
