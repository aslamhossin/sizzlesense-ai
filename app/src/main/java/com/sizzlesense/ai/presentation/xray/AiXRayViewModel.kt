package com.sizzlesense.ai.presentation.xray

import androidx.lifecycle.viewModelScope
import com.sizzlesense.ai.core.ui.BaseViewModel
import com.sizzlesense.ai.domain.repository.SessionStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@HiltViewModel
class AiXRayViewModel @Inject constructor(
    sessionStore: SessionStore
) : BaseViewModel<AiXRayUiState, AiXRayUiIntent, AiXRayUiEffect>(AiXRayUiState()) {

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

    override fun onIntent(intent: AiXRayUiIntent) {
        when (intent) {
            is AiXRayUiIntent.ToggleSuppressedIssues -> {
                updateState { it.copy(showSuppressedIssues = intent.visible) }
            }
        }
    }
}
