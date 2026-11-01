package com.sizzlesense.ai.presentation.coaching

import com.sizzlesense.ai.core.ui.BaseViewModel
import com.sizzlesense.ai.domain.repository.SessionStore
import com.sizzlesense.ai.domain.usecase.AnalyzeCookingSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CoachingViewModel @Inject constructor(
    private val sessionStore: SessionStore,
    private val analyzeCookingSession: AnalyzeCookingSessionUseCase
) : BaseViewModel<CoachingUiState, CoachingUiIntent, CoachingUiEffect>(CoachingUiState()) {

    init {
        sessionStore.sessionState.collectWhileSubscribed { session ->
            updateState { state ->
                state.copy(
                    currentAttemptNumber = session.currentAttemptNumber,
                    latestCoachingResponse = session.latestCoachingResponse
                )
            }
        }
    }

    override fun onIntent(intent: CoachingUiIntent) {
        when (intent) {
            CoachingUiIntent.AnalyzeCurrentAttempt -> analyzeAttempt()
            CoachingUiIntent.ClearError -> {
                updateState { it.copy(errorMessage = null) }
            }
        }
    }

    private fun analyzeAttempt() {
        val snapshot = sessionStore.sessionState.value
        updateState { it.copy(isAnalyzing = true, errorMessage = null) }
        launchResultTask(
            onSuccess = { response ->
                sessionStore.recordCoachingResponse(response)
                updateState { it.copy(isAnalyzing = false, errorMessage = null) }
            },
            onError = { appError ->
                updateState {
                    it.copy(
                        isAnalyzing = false,
                        errorMessage = appError.readableMessage
                    )
                }
            }
        ) {
            analyzeCookingSession(
                baseUrl = snapshot.backendBaseUrl,
                archetypeId = snapshot.selectedArchetype.id,
                attemptNumber = snapshot.currentAttemptNumber,
                learnerReflection = snapshot.learnerReflectionText,
                capturedFrameBase64 = snapshot.capturedFrameBase64
            )
        }
    }
}
