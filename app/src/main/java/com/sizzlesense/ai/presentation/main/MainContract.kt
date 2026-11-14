package com.sizzlesense.ai.presentation.main

import com.sizzlesense.ai.presentation.navigation.SizzleDestination

data class MainUiState(
    val activeDestination: SizzleDestination = SizzleDestination.KITCHEN,
    val isDeveloperEvalsEnabled: Boolean = false,
    val isDeveloperEvalsOpen: Boolean = false,
    val currentAttemptNumber: Int = 1,
    val completedAttemptNumbers: Set<Int> = emptySet()
)

sealed interface MainUiIntent {
    data class SelectDestination(val destination: SizzleDestination) : MainUiIntent
    data object OpenDeveloperEvals : MainUiIntent
    data object CloseDeveloperEvals : MainUiIntent
    data class SelectAttemptNumber(val attemptNumber: Int) : MainUiIntent
}

sealed interface MainUiEffect {
    data class ShowNavigationToast(val message: String) : MainUiEffect
}
