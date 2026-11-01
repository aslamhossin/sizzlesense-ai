package com.sizzlesense.ai.presentation.screens

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sizzlesense.ai.presentation.components.LabeledDetailRow
import com.sizzlesense.ai.presentation.components.SizzleSectionCard
import com.sizzlesense.ai.presentation.mvi.SizzleIntent
import com.sizzlesense.ai.presentation.mvi.SizzleUiState

@Composable
fun CoachingLoopTab(
    state: SizzleUiState,
    onIntent: (SizzleIntent) -> Unit
) {
    val resp = state.latestCoachingResponse
    if (resp == null) {
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
                Button(onClick = { onIntent(SizzleIntent.AnalyzeCurrentSession) }) {
                    Text("Analyze Attempt ${state.currentAttemptNumber} Now")
                }
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SizzleSectionCard(
            title = resp.stage3TeachingDecision.selectedPrimaryIssue,
            subtitle = "Single-Issue Focus (Attempt ${resp.attemptNumber})",
            icon = Icons.Default.Lightbulb
        ) {
            Text(
                text = resp.stage4SensoryCoaching.coachingMessage,
                style = MaterialTheme.typography.bodyMedium
            )
            HorizontalDivider()
            Text(
                text = "Sensory Cues to Shift How You Judge:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "👁 Visual: ${resp.stage4SensoryCoaching.visualCueToObserve}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "👂 Auditory (Sizzle): ${resp.stage4SensoryCoaching.auditoryCueToListen}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "👃 Aroma: ${resp.stage4SensoryCoaching.olfactoryOrTactileCue}",
                style = MaterialTheme.typography.bodySmall
            )
            HorizontalDivider()
            LabeledDetailRow(
                label = "Next Attempt Micro-Experiment:",
                value = resp.stage4SensoryCoaching.nextAttemptMicroExperiment
            )
        }

        SizzleSectionCard(
            title = "The 5 Core Cookpad JD Questions Answered",
            subtitle = "Synthesized across Stages 1–4",
            icon = Icons.Default.QuestionAnswer
        ) {
            val q = resp.fiveCoreQuestions
            LabeledDetailRow(
                label = "1. What is happening right now?",
                value = q.whatIsHappeningNow
            )
            LabeledDetailRow(
                label = "2. What do they understand vs. not yet?",
                value = q.whatTheyUnderstandVsNot
            )
            LabeledDetailRow(
                label = "3. Why did they make that decision?",
                value = q.whyTheyMadeThatDecision
            )
            LabeledDetailRow(
                label = "4. What single thing should we teach now?",
                value = q.whatSingleThingToTeachNow
            )
            LabeledDetailRow(
                label = "5. How to communicate to shift judgment?",
                value = q.howToCommunicateToShiftJudgment
            )
        }
    }
}
