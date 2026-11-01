package com.sizzlesense.ai.presentation.coaching

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
import com.sizzlesense.ai.presentation.coaching.component.EmptyCoachingSessionState
import com.sizzlesense.ai.presentation.coaching.component.FiveCoreQuestionsCard
import com.sizzlesense.ai.presentation.coaching.component.SingleIssueCoachingCard
import com.sizzlesense.ai.presentation.components.DismissibleErrorBanner

@Composable
fun CoachingRoute(
    viewModel: CoachingViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CoachingScreen(
        state = state,
        onIntent = viewModel::onIntent
    )
}

@Composable
fun CoachingScreen(
    state: CoachingUiState,
    onIntent: (CoachingUiIntent) -> Unit
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
                onDismiss = { onIntent(CoachingUiIntent.ClearError) }
            )
        }

        val resp = state.latestCoachingResponse
        if (resp == null) {
            EmptyCoachingSessionState(
                currentAttemptNumber = state.currentAttemptNumber,
                isAnalyzing = state.isAnalyzing,
                onAnalyzeCurrentAttempt = { onIntent(CoachingUiIntent.AnalyzeCurrentAttempt) }
            )
        } else {
            SingleIssueCoachingCard(response = resp)
            FiveCoreQuestionsCard(questions = resp.fiveCoreQuestions)
        }
    }
}
