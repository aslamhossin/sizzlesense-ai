package com.sizzlesense.ai.presentation.main

import com.sizzlesense.ai.core.ui.BaseViewModel
import com.sizzlesense.ai.di.qualifier.DeveloperEvalsEnabled
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
    private val sessionStore: SessionStore,
    @param:DeveloperEvalsEnabled private val isDeveloperEvalsEnabled: Boolean
) : BaseViewModel<MainUiState, MainUiIntent, MainUiEffect>() {

    private val activeDestination = MutableStateFlow(SizzleDestination.KITCHEN)
    private val isDeveloperEvalsOpen = MutableStateFlow(false)

    override val uiState: StateFlow<MainUiState> = combine(
        sessionStore.sessionState,
        activeDestination,
        isDeveloperEvalsOpen
    ) { session, destination, evalsOpen ->
        session.toMainUiState(
            destination = destination,
            evalsOpen = evalsOpen && isDeveloperEvalsEnabled
        )
    }.stateWhileSubscribed(
        initialValue = sessionStore.sessionState.value.toMainUiState(
            destination = activeDestination.value,
            evalsOpen = false
        )
    )

    override fun onIntent(intent: MainUiIntent) {
        when (intent) {
            is MainUiIntent.SelectDestination -> {
                isDeveloperEvalsOpen.value = false
                activeDestination.value = intent.destination
            }

            MainUiIntent.OpenDeveloperEvals -> {
                if (isDeveloperEvalsEnabled) {
                    isDeveloperEvalsOpen.value = true
                }
            }

            MainUiIntent.CloseDeveloperEvals -> {
                isDeveloperEvalsOpen.value = false
            }

            is MainUiIntent.SelectAttemptNumber -> {
                sessionStore.selectAttemptNumber(intent.attemptNumber)
            }
        }
    }

    private fun SessionSnapshot.toMainUiState(
        destination: SizzleDestination,
        evalsOpen: Boolean
    ) = MainUiState(
        activeDestination = destination,
        isDeveloperEvalsEnabled = isDeveloperEvalsEnabled,
        isDeveloperEvalsOpen = evalsOpen,
        currentAttemptNumber = currentAttemptNumber,
        completedAttemptNumbers = attemptHistory.keys
    )
}
