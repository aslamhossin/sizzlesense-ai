package com.sizzlesense.ai.presentation.coaching

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sizzlesense.ai.domain.repository.SessionStore
import com.sizzlesense.ai.domain.usecase.AnalyzeCookingSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CoachingViewModel @Inject constructor(
    private val sessionStore: SessionStore,
    private val analyzeCookingSession: AnalyzeCookingSessionUseCase
) : ViewModel() {

    private data class ExecutionState(
        val isAnalyzing: Boolean = false,
        val errorMessage: String? = null
    )

    private val executionState = MutableStateFlow(ExecutionState())

    private val _effects = MutableSharedFlow<CoachingUiEffect>()
    val effects: SharedFlow<CoachingUiEffect> = _effects.asSharedFlow()

    val uiState: StateFlow<CoachingUiState> = combine(
        sessionStore.sessionState,
        executionState
    ) { session, exec ->
        CoachingUiState(
            currentAttemptNumber = session.currentAttemptNumber,
            latestCoachingResponse = session.latestCoachingResponse,
            isAnalyzing = exec.isAnalyzing,
            errorMessage = exec.errorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CoachingUiState()
    )

    fun onIntent(intent: CoachingUiIntent) {
        when (intent) {
            CoachingUiIntent.AnalyzeCurrentAttempt -> analyzeAttempt()
            CoachingUiIntent.ClearError -> {
                executionState.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun analyzeAttempt() {
        val snapshot = sessionStore.sessionState.value
        viewModelScope.launch {
            executionState.update { ExecutionState(isAnalyzing = true, errorMessage = null) }
            try {
                val response = analyzeCookingSession(
                    baseUrl = snapshot.backendBaseUrl,
                    archetypeId = snapshot.selectedArchetype.id,
                    attemptNumber = snapshot.currentAttemptNumber,
                    learnerReflection = snapshot.learnerReflectionText,
                    capturedFrameBase64 = snapshot.capturedFrameBase64
                )
                sessionStore.recordCoachingResponse(response)
                executionState.update { ExecutionState(isAnalyzing = false, errorMessage = null) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                executionState.update {
                    ExecutionState(
                        isAnalyzing = false,
                        errorMessage = e.message ?: "Failed to analyze current attempt"
                    )
                }
            }
        }
    }
}
