package com.sizzlesense.ai.presentation.coaching

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sizzlesense.ai.presentation.coaching.component.EmptyCoachingSessionState
import com.sizzlesense.ai.presentation.coaching.component.FiveCoreQuestionsCard
import com.sizzlesense.ai.presentation.coaching.component.SingleIssueCoachingCard
import com.sizzlesense.ai.presentation.common.component.PinnedErrorScrollColumn
import com.sizzlesense.ai.presentation.theme.SizzleTheme

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
    PinnedErrorScrollColumn(
        errorMessage = state.errorMessage,
        onDismissError = { onIntent(CoachingUiIntent.ClearError) }
    ) {
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
            Spacer(modifier = Modifier.height(SizzleTheme.spacing.md))
        }
    }
}
