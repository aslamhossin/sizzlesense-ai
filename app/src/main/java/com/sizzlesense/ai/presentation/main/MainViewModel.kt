package com.sizzlesense.ai.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sizzlesense.ai.domain.repository.SessionStore
import com.sizzlesense.ai.presentation.navigation.SizzleDestination
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
class MainViewModel @Inject constructor(
    private val sessionStore: SessionStore
) : ViewModel() {

    private val activeDestination = MutableStateFlow(SizzleDestination.KITCHEN)

    private val _effects = MutableSharedFlow<MainUiEffect>()
    val effects: SharedFlow<MainUiEffect> = _effects.asSharedFlow()

    val uiState: StateFlow<MainUiState> = combine(
        activeDestination,
        sessionStore.sessionState
    ) { destination, session ->
        MainUiState(
            activeDestination = destination,
            currentAttemptNumber = session.currentAttemptNumber,
            completedAttemptNumbers = session.attemptHistory.keys
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainUiState()
    )

    fun onIntent(intent: MainUiIntent) {
        when (intent) {
            is MainUiIntent.SelectDestination -> {
                activeDestination.value = intent.destination
            }

            is MainUiIntent.SelectAttemptNumber -> {
                sessionStore.selectAttemptNumber(intent.attemptNumber)
            }
        }
    }
}
