package com.sizzlesense.ai.presentation.coaching

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sizzlesense.ai.domain.model.CognitiveCoachingResponse
import com.sizzlesense.ai.domain.model.FiveCoreQuestionsSummary
import com.sizzlesense.ai.domain.model.SensoryCoachingOutput
import com.sizzlesense.ai.presentation.components.DismissibleErrorBanner
import com.sizzlesense.ai.presentation.components.LabeledDetailRow
import com.sizzlesense.ai.presentation.components.SizzleSectionCard

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

@Composable
private fun EmptyCoachingSessionState(
    currentAttemptNumber: Int,
    isAnalyzing: Boolean,
    onAnalyzeCurrentAttempt: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No session analyzed yet.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            if (isAnalyzing) {
                CircularProgressIndicator()
            } else {
                Button(onClick = onAnalyzeCurrentAttempt) {
                    Text("Analyze Attempt $currentAttemptNumber Now")
                }
            }
        }
    }
}

@Composable
private fun SingleIssueCoachingCard(response: CognitiveCoachingResponse) {
    val sensory = response.stage4SensoryCoaching
    SizzleSectionCard(
        title = response.stage3TeachingDecision.selectedPrimaryIssue,
        subtitle = "Single-Issue Focus (Attempt ${response.attemptNumber})",
        icon = Icons.Default.Lightbulb
    ) {
        Text(
            text = sensory.coachingMessage,
            style = MaterialTheme.typography.bodyMedium
        )
        HorizontalDivider()
        SensoryCuesList(sensory = sensory)
        HorizontalDivider()
        LabeledDetailRow(
            label = "Next Attempt Micro-Experiment:",
            value = sensory.nextAttemptMicroExperiment
        )
    }
}

@Composable
private fun SensoryCuesList(sensory: SensoryCoachingOutput) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Sensory Cues to Shift How You Judge:",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "👁 Visual: ${sensory.visualCueToObserve}",
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = "👂 Auditory (Sizzle): ${sensory.auditoryCueToListen}",
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = "👃 Aroma: ${sensory.olfactoryOrTactileCue}",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun FiveCoreQuestionsCard(questions: FiveCoreQuestionsSummary) {
    SizzleSectionCard(
        title = "The 5 Core Cookpad JD Questions Answered",
        subtitle = "Synthesized across Stages 1–4",
        icon = Icons.Default.QuestionAnswer
    ) {
        LabeledDetailRow(
            label = "1. What is happening right now?",
            value = questions.whatIsHappeningNow
        )
        LabeledDetailRow(
            label = "2. What do they understand vs. not yet?",
            value = questions.whatTheyUnderstandVsNot
        )
        LabeledDetailRow(
            label = "3. Why did they make that decision?",
            value = questions.whyTheyMadeThatDecision
        )
        LabeledDetailRow(
            label = "4. What single thing should we teach now?",
            value = questions.whatSingleThingToTeachNow
        )
        LabeledDetailRow(
            label = "5. How to communicate to shift judgment?",
            value = questions.howToCommunicateToShiftJudgment
        )
    }
}
