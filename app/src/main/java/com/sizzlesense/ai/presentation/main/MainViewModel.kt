package com.sizzlesense.ai.presentation.main

import androidx.lifecycle.viewModelScope
import com.sizzlesense.ai.core.ui.BaseViewModel
import com.sizzlesense.ai.domain.repository.SessionStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@HiltViewModel
class MainViewModel @Inject constructor(
    private val sessionStore: SessionStore
) : BaseViewModel<MainUiState, MainUiIntent, MainUiEffect>(MainUiState()) {

    init {
        sessionStore.sessionState
            .onEach { session ->
                updateState { state ->
                    state.copy(
                        currentAttemptNumber = session.currentAttemptNumber,
                        completedAttemptNumbers = session.attemptHistory.keys
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onIntent(intent: MainUiIntent) {
        when (intent) {
            is MainUiIntent.SelectDestination -> {
                updateState { state ->
                    state.copy(activeDestination = intent.destination)
                }
            }

            is MainUiIntent.SelectAttemptNumber -> {
                sessionStore.selectAttemptNumber(intent.attemptNumber)
            }
        }
    }
}
