package com.sizzlesense.ai.presentation.navigation

import androidx.compose.runtime.Composable

/**
 * Variant-scoped feature entry for the internal evaluation benchmark screen.
 * Debug builds mount the benchmark UI; release builds bind a no-op stub.
 */
interface EvalBenchEntry {
    @Composable
    fun Content()
}
