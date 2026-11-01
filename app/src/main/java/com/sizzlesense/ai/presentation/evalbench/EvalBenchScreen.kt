package com.sizzlesense.ai.presentation.evalbench

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sizzlesense.ai.presentation.components.DismissibleErrorBanner
import com.sizzlesense.ai.presentation.evalbench.component.EvalAggregateScoreCard
import com.sizzlesense.ai.presentation.evalbench.component.EvalBenchmarkControlCard
import com.sizzlesense.ai.presentation.evalbench.component.EvalCaseResultCard

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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        state.errorMessage?.let { err ->
            DismissibleErrorBanner(
                errorMessage = err,
                onDismiss = { onIntent(EvalBenchUiIntent.ClearError) }
            )
        }

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
    }
}
