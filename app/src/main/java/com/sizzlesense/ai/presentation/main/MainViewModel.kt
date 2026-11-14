package com.sizzlesense.ai.presentation.main

import com.sizzlesense.ai.core.ui.BaseViewModel
import com.sizzlesense.ai.domain.model.session.SessionSnapshot
import com.sizzlesense.ai.domain.repository.SessionStore
import com.sizzlesense.ai.presentation.navigation.SizzleDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

@HiltViewModel
class MainViewModel @Inject constructor(
    private val sessionStore: SessionStore
) : BaseViewModel<MainUiState, MainUiIntent, MainUiEffect>() {

    private val activeDestination = MutableStateFlow(SizzleDestination.KITCHEN)

    override val uiState: StateFlow<MainUiState> = combine(
        sessionStore.sessionState,
        activeDestination
    ) { session, destination ->
        session.toMainUiState(destination)
    }.stateWhileSubscribed(
        initialValue = sessionStore.sessionState.value.toMainUiState(activeDestination.value)
    )

    override fun onIntent(intent: MainUiIntent) {
        when (intent) {
            is MainUiIntent.SelectDestination -> {
                activeDestination.value = intent.destination
            }

            is MainUiIntent.SelectAttemptNumber -> {
                sessionStore.selectAttemptNumber(intent.attemptNumber)
            }
        }
    }

    private fun SessionSnapshot.toMainUiState(destination: SizzleDestination) = MainUiState(
        activeDestination = destination,
        currentAttemptNumber = currentAttemptNumber,
        completedAttemptNumbers = attemptHistory.keys
    )
}
