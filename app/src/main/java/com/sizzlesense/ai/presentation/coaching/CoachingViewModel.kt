package com.sizzlesense.ai.presentation.coaching

import androidx.lifecycle.viewModelScope
import com.sizzlesense.ai.core.ui.BaseViewModel
import com.sizzlesense.ai.domain.repository.SessionStore
import com.sizzlesense.ai.domain.usecase.AnalyzeCookingSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@HiltViewModel
class CoachingViewModel @Inject constructor(
    private val sessionStore: SessionStore,
    private val analyzeCookingSession: AnalyzeCookingSessionUseCase
) : BaseViewModel<CoachingUiState, CoachingUiIntent, CoachingUiEffect>(CoachingUiState()) {

    init {
        sessionStore.sessionState
            .onEach { session ->
                updateState { state ->
                    state.copy(
                        currentAttemptNumber = session.currentAttemptNumber,
                        latestCoachingResponse = session.latestCoachingResponse
                    )
                }
            }
            .launchIn(viewModelScope)
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
        launchTask(
            onError = { error ->
                updateState {
                    it.copy(
                        isAnalyzing = false,
                        errorMessage = error.message ?: "Failed to analyze current attempt"
                    )
                }
            }
        ) {
            val response = analyzeCookingSession(
                baseUrl = snapshot.backendBaseUrl,
                archetypeId = snapshot.selectedArchetype.id,
                attemptNumber = snapshot.currentAttemptNumber,
                learnerReflection = snapshot.learnerReflectionText,
                capturedFrameBase64 = snapshot.capturedFrameBase64
            )
            sessionStore.recordCoachingResponse(response)
            updateState { it.copy(isAnalyzing = false, errorMessage = null) }
        }
    }
}
