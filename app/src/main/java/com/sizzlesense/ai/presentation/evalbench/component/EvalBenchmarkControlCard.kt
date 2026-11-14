package com.sizzlesense.ai.presentation.evalbench.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.sizzlesense.ai.presentation.common.component.SizzleSectionCard
import com.sizzlesense.ai.presentation.theme.SizzleTheme

@Composable
fun EvalBenchmarkControlCard(
    isRunningEvals: Boolean,
    onRunEvalSuite: () -> Unit
) {
    SizzleSectionCard(
        title = "Coaching Benchmark",
        subtitle = "10 test sessions across 5 quality checks",
        icon = Icons.Default.Analytics
    ) {
        if (isRunningEvals) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SizzleTheme.spacing.md)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(SizzleTheme.spacing.iconMedium)
                )
                Text(
                    text = "Running benchmark...",
                    style = SizzleTheme.typography.bodySmall,
                    color = SizzleTheme.colors.textSecondary
                )
            }
        } else {
            Button(
                onClick = onRunEvalSuite,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Run Benchmark",
                    maxLines = 1
                )
            }
        }
    }
}
