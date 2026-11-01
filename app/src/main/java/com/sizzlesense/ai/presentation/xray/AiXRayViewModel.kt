package com.sizzlesense.ai.presentation.xray

import com.sizzlesense.ai.core.ui.BaseViewModel
import com.sizzlesense.ai.domain.repository.SessionStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AiXRayViewModel @Inject constructor(
    sessionStore: SessionStore
) : BaseViewModel<AiXRayUiState, AiXRayUiIntent, AiXRayUiEffect>(AiXRayUiState()) {

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

    override fun onIntent(intent: AiXRayUiIntent) {
        when (intent) {
            is AiXRayUiIntent.ToggleSuppressedIssues -> {
                updateState { it.copy(showSuppressedIssues = intent.visible) }
            }
        }
    }
}
