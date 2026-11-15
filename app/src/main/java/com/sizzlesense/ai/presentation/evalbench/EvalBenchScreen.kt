package com.sizzlesense.ai.presentation.evalbench

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sizzlesense.ai.presentation.common.component.PinnedErrorScrollColumn
import com.sizzlesense.ai.presentation.evalbench.component.EvalAggregateScoreCard
import com.sizzlesense.ai.presentation.evalbench.component.EvalBenchmarkControlCard
import com.sizzlesense.ai.presentation.evalbench.component.EvalCaseResultCard
import com.sizzlesense.ai.presentation.theme.SizzleTheme

@Composable
fun EvalBenchRoute(
    viewModel: EvalBenchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    EvalBenchScreen(
        state = state,
        onIntent = viewModel::onIntent
    )
}

@Composable
fun EvalBenchScreen(
    state: EvalBenchUiState,
    onIntent: (EvalBenchUiIntent) -> Unit
) {
    PinnedErrorScrollColumn(
        errorMessage = state.errorMessage,
        onDismissError = { onIntent(EvalBenchUiIntent.ClearError) }
    ) {
        EvalBenchmarkControlCard(
            isRunningEvals = state.isRunningEvals,
            onRunEvalSuite = { onIntent(EvalBenchUiIntent.RunHumanLearningEvalSuite) }
        )

        state.evalSuiteReport?.let { report ->
            EvalAggregateScoreCard(report = report)
            report.caseResults.forEach { item ->
                EvalCaseResultCard(item = item)
            }
        }

        Spacer(modifier = Modifier.height(SizzleTheme.spacing.md))
    }
}
