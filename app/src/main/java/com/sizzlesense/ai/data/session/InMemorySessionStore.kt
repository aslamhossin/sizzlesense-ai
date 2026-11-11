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
class InMemorySessionStore : SessionStore {

    private val persistence: SessionPersistenceDataSource?

    @Inject
    constructor(persistence: SessionPersistenceDataSource) {
        this.persistence = persistence
        val initial = persistence.loadSnapshot() ?: defaultSnapshot()
        this.sessionState.value = initial
    }

    constructor() {
        this.persistence = null
    }

    override val sessionState: StateFlow<SessionSnapshot>
        field = MutableStateFlow(defaultSnapshot())

    override fun updateBackendUrl(url: String) {
        mutateAndPersist { current ->
            current.copy(backendBaseUrl = url.trim())
        }
    }

    override fun updateTapoConfig(config: TapoCameraConfig) {
        mutateAndPersist { current ->
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
        mutateAndPersist { current ->
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
        mutateAndPersist { current ->
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
        mutateAndPersist { current ->
            current.copy(learnerReflectionText = text)
        }
    }

    override fun recordCoachingResponse(response: CognitiveCoachingResponse) {
        mutateAndPersist { current ->
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
        mutateAndPersist { current ->
            current.copy(evalSuiteReport = report)
        }
    }

    private inline fun mutateAndPersist(transform: (SessionSnapshot) -> SessionSnapshot) {
        sessionState.update { current ->
            val updated = transform(current)
            persistence?.saveSnapshot(updated)
            updated
        }
    }

    companion object {
        private fun defaultSnapshot(): SessionSnapshot = SessionSnapshot(
            backendBaseUrl = BuildConfig.DEFAULT_BACKEND_URL,
            tapoConfig = TapoCameraConfig(ipAddress = BuildConfig.DEFAULT_TAPO_IP)
        )
    }
}
