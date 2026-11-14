package com.sizzlesense.ai.presentation.xray

import com.sizzlesense.ai.core.ui.BaseViewModel
import com.sizzlesense.ai.domain.model.session.SessionSnapshot
import com.sizzlesense.ai.domain.repository.SessionStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

@HiltViewModel
class AiXRayViewModel @Inject constructor(
    sessionStore: SessionStore
) : BaseViewModel<AiXRayUiState, AiXRayUiIntent, AiXRayUiEffect>() {

    private val showSuppressedIssues = MutableStateFlow(true)

    override val uiState: StateFlow<AiXRayUiState> = combine(
        sessionStore.sessionState,
        showSuppressedIssues
    ) { session, visible ->
        session.toAiXRayUiState(visible)
    }.stateWhileSubscribed(
        initialValue = sessionStore.sessionState.value.toAiXRayUiState(showSuppressedIssues.value)
    )

    override fun onIntent(intent: AiXRayUiIntent) {
        when (intent) {
            is AiXRayUiIntent.ToggleSuppressedIssues -> {
                showSuppressedIssues.value = intent.visible
            }
        }
    }

    private fun SessionSnapshot.toAiXRayUiState(visible: Boolean) = AiXRayUiState(
        currentAttemptNumber = currentAttemptNumber,
        latestCoachingResponse = latestCoachingResponse,
        showSuppressedIssues = visible
    )
}
