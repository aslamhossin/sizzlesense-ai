package com.sizzlesense.ai.presentation.coaching

import com.sizzlesense.ai.core.ui.BaseViewModel
import com.sizzlesense.ai.core.ui.TaskStatus
import com.sizzlesense.ai.domain.model.session.SessionSnapshot
import com.sizzlesense.ai.domain.repository.SessionStore
import com.sizzlesense.ai.domain.usecase.AnalyzeCookingSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

@HiltViewModel
class CoachingViewModel @Inject constructor(
    private val sessionStore: SessionStore,
    private val analyzeCookingSession: AnalyzeCookingSessionUseCase
) : BaseViewModel<CoachingUiState, CoachingUiIntent, CoachingUiEffect>() {

    private val taskStatus = MutableStateFlow(TaskStatus())

    override val uiState: StateFlow<CoachingUiState> = combine(
        sessionStore.sessionState,
        taskStatus
    ) { session, status ->
        session.toCoachingUiState(status)
    }.stateWhileSubscribed(
        initialValue = sessionStore.sessionState.value.toCoachingUiState(taskStatus.value)
    )

    override fun onIntent(intent: CoachingUiIntent) {
        when (intent) {
            CoachingUiIntent.AnalyzeCurrentAttempt -> analyzeAttempt()
            CoachingUiIntent.ClearError -> {
                taskStatus.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun analyzeAttempt() {
        val snapshot = sessionStore.sessionState.value
        taskStatus.launchGuardedTask(
            runningLabel = "Analyzing your pan & coaching...",
            onSuccess = { response -> sessionStore.recordCoachingResponse(response) }
        ) {
            analyzeCookingSession(
                baseUrl = snapshot.backendBaseUrl,
                archetypeId = snapshot.selectedArchetype.id,
                attemptNumber = snapshot.currentAttemptNumber,
                learnerReflection = snapshot.learnerReflectionText,
                capturedFrameBase64 = snapshot.capturedFrameBase64,
                capturedAudioWavBase64 = snapshot.capturedAudioWavBase64
            )
        }
    }

    private fun SessionSnapshot.toCoachingUiState(status: TaskStatus) = CoachingUiState(
        currentAttemptNumber = currentAttemptNumber,
        latestCoachingResponse = latestCoachingResponse,
        isAnalyzing = status.isRunning,
        errorMessage = status.errorMessage
    )
}
