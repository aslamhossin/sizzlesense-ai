package com.sizzlesense.ai.core.ui

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Unidirectional Data Flow contract inspired by Thunderbird Android (`core:ui`).
 */
interface UnidirectionalViewModel<STATE, INTENT, EFFECT> {

    val uiState: StateFlow<STATE>

    val effects: Flow<EFFECT>

    fun onIntent(intent: INTENT)
}
