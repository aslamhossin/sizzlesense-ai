package com.sizzlesense.ai.presentation.xray

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sizzlesense.ai.domain.repository.SessionStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class AiXRayViewModel @Inject constructor(
    sessionStore: SessionStore
) : ViewModel() {

    private val showSuppressedIssues = MutableStateFlow(true)

    private val _effects = MutableSharedFlow<AiXRayUiEffect>()
    val effects: SharedFlow<AiXRayUiEffect> = _effects.asSharedFlow()

    val uiState: StateFlow<AiXRayUiState> = combine(
        sessionStore.sessionState,
        showSuppressedIssues
    ) { session, showSuppressed ->
        AiXRayUiState(
            currentAttemptNumber = session.currentAttemptNumber,
            latestCoachingResponse = session.latestCoachingResponse,
            showSuppressedIssues = showSuppressed
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AiXRayUiState()
    )

    fun onIntent(intent: AiXRayUiIntent) {
        when (intent) {
            is AiXRayUiIntent.ToggleSuppressedIssues -> {
                showSuppressedIssues.value = intent.visible
            }
        }
    }
}
