package com.sizzlesense.ai.presentation.main.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.sizzlesense.ai.presentation.main.MainUiState
import com.sizzlesense.ai.presentation.theme.SizzleTheme

@Composable
fun AttemptSelectorBar(
    state: MainUiState,
    onAttemptSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SizzleTheme.spacing.xs)
    ) {
        listOf(1, 2, 3).forEach { attempt ->
            val completed = state.completedAttemptNumbers.contains(attempt)
            FilterChip(
                selected = state.currentAttemptNumber == attempt,
                onClick = { onAttemptSelected(attempt) },
                modifier = Modifier.weight(1f),
                label = {
                    Text(
                        text = if (completed) "Attempt $attempt ✓" else "Attempt $attempt",
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            )
        }
    }
}
