package com.sizzlesense.ai.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sizzlesense.ai.domain.model.EvalCaseScore
import com.sizzlesense.ai.domain.model.EvalSuiteReport
import com.sizzlesense.ai.presentation.components.SizzleSectionCard
import com.sizzlesense.ai.presentation.mvi.SizzleIntent
import com.sizzlesense.ai.presentation.mvi.SizzleUiState

@Composable
fun EvalBenchRoute(
    state: SizzleUiState,
    onIntent: (SizzleIntent) -> Unit
) {
    EvalBenchScreen(
        state = state,
        onRunEvalSuite = { onIntent(SizzleIntent.RunHumanLearningEvalSuite) }
    )
}

@Composable
fun EvalBenchScreen(
    state: SizzleUiState,
    onRunEvalSuite: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        EvalBenchmarkControlCard(
            isRunningEvals = state.isRunningEvals,
            onRunEvalSuite = onRunEvalSuite
        )

        state.evalSuiteReport?.let { report ->
            EvalAggregateScoreCard(report = report)
            report.caseResults.forEach { item ->
                EvalCaseResultCard(item = item)
            }
        }
    }
}

@Composable
private fun EvalBenchmarkControlCard(
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

@Composable
private fun EvalAggregateScoreCard(report: EvalSuiteReport) {
    val overallPct = (report.overallHumanLearningScore * 100).toInt()
    SizzleSectionCard(
        title = "Overall Score: $overallPct% (${report.totalSessionsEvaluated} Sessions)",
        subtitle = "Verified against Cookpad JD Human Learning Rubrics",
        icon = Icons.Default.CheckCircle
    ) {
        Text("• Perception Gap Recall: ${(report.perceptionGapDetectionScore * 100).toInt()}%")
        Text("• Latent Root-Cause Precision: ${(report.latentRootCauseAccuracy * 100).toInt()}%")
        Text("• Single-Issue Pedagogical Restraint: ${(report.singleIssueRestraintScore * 100).toInt()}%")
        Text("• Sensory Grounding (Timer Penalty Check): ${(report.sensoryGroundingScore * 100).toInt()}%")
        Text("• Cross-Attempt Behavioral Transfer: ${(report.crossAttemptTransferScore * 100).toInt()}%")
    }
}

@Composable
private fun EvalCaseResultCard(item: EvalCaseScore) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "${item.caseId} (Attempt ${item.attemptNumber})",
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = item.summaryNote,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
