package com.sizzlesense.ai.presentation.evalbench.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sizzlesense.ai.presentation.components.SizzleSectionCard

@Composable
fun EvalBenchmarkControlCard(
    isRunningEvals: Boolean,
    onRunEvalSuite: () -> Unit
) {
    SizzleSectionCard(
        title = "Human Learning Evaluation Benchmark (EvalBench)",
        subtitle = "10 multi-session learner trajectories across 5 rubrics",
        icon = Icons.Default.Analytics
    ) {
        if (isRunningEvals) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = onRunEvalSuite,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Run Full 10-Session Human Learning Eval Suite")
            }
        }
    }
}
