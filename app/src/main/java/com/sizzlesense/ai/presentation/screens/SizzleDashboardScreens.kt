package com.sizzlesense.ai.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sizzlesense.ai.data.camera.TapoRtspPlayerView
import com.sizzlesense.ai.domain.model.TapoCameraConfig
import com.sizzlesense.ai.presentation.components.DismissibleErrorBanner
import com.sizzlesense.ai.presentation.components.LabeledDetailRow
import com.sizzlesense.ai.presentation.components.SizzleSectionCard
import com.sizzlesense.ai.presentation.mvi.AppTab
import com.sizzlesense.ai.presentation.mvi.LearnerArchetypeOption
import com.sizzlesense.ai.presentation.mvi.SizzleIntent
import com.sizzlesense.ai.presentation.mvi.SizzleUiState

@Composable
fun SizzleMainScreen(
    state: SizzleUiState,
    onIntent: (SizzleIntent) -> Unit
) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = state.activeTab == tab,
                        onClick = { onIntent(SizzleIntent.SelectTab(tab)) },
                        icon = {
                            Text(
                                text = (tab.ordinal + 1).toString(),
                                fontWeight = FontWeight.Bold
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                maxLines = 1
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HeaderBar(
                state = state,
                onIntent = onIntent
            )

            state.errorMessage?.let { err ->
                DismissibleErrorBanner(
                    errorMessage = err,
                    onDismiss = { onIntent(SizzleIntent.ClearError) }
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                when (state.activeTab) {
                    AppTab.KITCHEN_LIVE -> KitchenCaptureTab(
                        state = state,
                        onIntent = onIntent
                    )
                    AppTab.COACHING_LOOP -> CoachingLoopTab(
                        state = state,
                        onIntent = onIntent
                    )
                    AppTab.AI_XRAY -> AiXRayInspectorTab(state = state)
                    AppTab.EVAL_BENCH -> EvalBenchTab(
                        state = state,
                        onIntent = onIntent
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderBar(
    state: SizzleUiState,
    onIntent: (SizzleIntent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "SizzleSense AI — Human Learning Coach",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(1, 2, 3).forEach { attempt ->
                val done = state.attemptHistory.containsKey(attempt)
                FilterChip(
                    selected = state.currentAttemptNumber == attempt,
                    onClick = { onIntent(SizzleIntent.SelectAttemptNumber(attempt)) },
                    label = {
                        Text(
                            text = if (done) "Attempt $attempt ✓" else "Attempt $attempt"
                        )
                    }
                )
            }
        }
    }
}

private fun TapoCameraConfig.updateIntent(
    ipAddress: String = this.ipAddress,
    username: String = this.username,
    password: String = this.password,
    useSubStream: Boolean = this.useSubStream
): SizzleIntent.UpdateTapoConfig = SizzleIntent.UpdateTapoConfig(
    ipAddress = ipAddress,
    username = username,
    password = password,
    useSubStream = useSubStream
)

@Composable
private fun KitchenCaptureTab(
    state: SizzleUiState,
    onIntent: (SizzleIntent) -> Unit
) {
    val tapo = state.tapoConfig
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TapoRtspPlayerView(
            config = tapo,
            onConnectToggle = { connect ->
                onIntent(SizzleIntent.SetTapoStreamConnected(connect))
            },
            onFrameCaptured = { b64, label ->
                onIntent(SizzleIntent.OnLiveFrameCaptured(b64, label))
            }
        )

        SizzleSectionCard(title = "Tapo C100 RTSP & Python AI Backend Settings") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = tapo.ipAddress,
                    onValueChange = { ip -> onIntent(tapo.updateIntent(ipAddress = ip)) },
                    label = { Text("Tapo Camera IP") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = state.backendBaseUrl,
                    onValueChange = { url -> onIntent(SizzleIntent.UpdateBackendUrl(url)) },
                    label = { Text("Python AI Backend") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = tapo.username,
                    onValueChange = { user -> onIntent(tapo.updateIntent(username = user)) },
                    label = { Text("Tapo Username") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = tapo.password,
                    onValueChange = { pass -> onIntent(tapo.updateIntent(password = pass)) },
                    label = { Text("Tapo Password") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Use 360p Substream (/stream2) for low-latency AI",
                    style = MaterialTheme.typography.bodySmall
                )
                Switch(
                    checked = tapo.useSubStream,
                    onCheckedChange = { sub -> onIntent(tapo.updateIntent(useSubStream = sub)) }
                )
            }
        }

        SizzleSectionCard(title = "3 Cookpad JD Sautéed Onion Learner Archetypes") {
            LearnerArchetypeOption.entries.forEach { option ->
                FilterChip(
                    selected = state.selectedArchetype == option,
                    onClick = { onIntent(SizzleIntent.SelectArchetype(option)) },
                    label = { Text(option.title) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = state.learnerReflectionText,
                onValueChange = { onIntent(SizzleIntent.UpdateReflectionText(it)) },
                label = { Text("Post-Cooking Learner Reflection (Voice / Transcript)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Text(
                text = "Frame status: ${state.capturedFrameLabel}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            if (state.isAnalyzing) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = state.activePipelineStageLabel,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                Button(
                    onClick = { onIntent(SizzleIntent.AnalyzeCurrentSession) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Run 4-Stage Applied AI Coaching (Attempt ${state.currentAttemptNumber})")
                }

                OutlinedButton(
                    onClick = { onIntent(SizzleIntent.CaptureFromBackendTapoAndAnalyze) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Grab Live Tapo C100 Frame via Backend & Analyze")
                }
            }
        }
    }
}

@Composable
private fun CoachingLoopTab(
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
                Text("No session analyzed yet.")
                Spacer(modifier = Modifier.height(8.dp))
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
            subtitle = "Single-Issue Focus (Attempt ${resp.attemptNumber})"
        ) {
            Text(
                text = resp.stage4SensoryCoaching.coachingMessage,
                style = MaterialTheme.typography.bodyMedium
            )
            HorizontalDivider()
            Text(
                text = "Sensory Cues to Shift How You Judge:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
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

        SizzleSectionCard(title = "The 5 Core Cookpad JD Questions Answered") {
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

@Composable
private fun AiXRayInspectorTab(state: SizzleUiState) {
    val resp = state.latestCoachingResponse
    if (resp == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Run a session analysis first to inspect the 4-Stage Cognitive Trace.")
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
            title = "Stage 1: Physical World Understanding",
            subtitle = "Model: ${resp.visionModelUsed}"
        ) {
            LabeledDetailRow(
                label = "Perception Gap: ${resp.stage1PhysicalObservation.perceptionGapLevel}",
                value = resp.stage1PhysicalObservation.perceptionGapExplanation
            )
            resp.stage1PhysicalObservation.cvMetrics?.let { cv ->
                Text(
                    text = "CV Metrics -> Browning: ${cv.maillardBrowningRatio} | Moisture: ${cv.surfaceMoistureIndex} | Char: ${cv.charBurnRatio} | Regime: ${cv.dominantVisualRegime}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "Inferred Physical States:",
                style = MaterialTheme.typography.labelMedium
            )
            resp.stage1PhysicalObservation.inferredPhysicalStates.forEach { stateLine ->
                Text(
                    text = "• $stateLine",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        SizzleSectionCard(
            title = "Stage 2: Learner Latent State Estimation",
            subtitle = "Model: ${resp.reasoningModelUsed}"
        ) {
            Text(
                text = "Inferred Archetype: ${resp.stage2LearnerState.learnerArchetypeId}",
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Transfer Status: ${resp.stage2LearnerState.transferStatus} (Verified Shift: ${resp.stage2LearnerState.conceptualShiftVerified})",
                style = MaterialTheme.typography.bodySmall
            )
            LabeledDetailRow(
                label = "Decision Root Cause:",
                value = resp.stage2LearnerState.decisionRootCause
            )
            LabeledDetailRow(
                label = "Longitudinal Delta:",
                value = resp.stage2LearnerState.longitudinalDeltaSummary
            )
        }

        SizzleSectionCard(title = "Stage 3: Pedagogical Restraint & Omitted Issues") {
            LabeledDetailRow(
                label = "Selected Primary: ${resp.stage3TeachingDecision.selectedPrimaryIssue}",
                value = resp.stage3TeachingDecision.causalPriorityRationale
            )
            Text(
                text = "Deliberately Suppressed Secondary Issues:",
                style = MaterialTheme.typography.labelMedium
            )
            resp.stage3TeachingDecision.deliberatelyOmittedIssues.forEach { omitted ->
                Text(
                    text = "• ${omitted.issue} — ${omitted.suppressionReason}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun EvalBenchTab(
    state: SizzleUiState,
    onIntent: (SizzleIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SizzleSectionCard(
            title = "Human Learning Evaluation Benchmark (EvalBench)",
            subtitle = "Evaluates 10 multi-session learner trajectories (3 JD Onion Archetypes × 3 Attempts + 1 Lucky-Timer Negative Control) across 5 rubrics."
        ) {
            if (state.isRunningEvals) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = { onIntent(SizzleIntent.RunHumanLearningEvalSuite) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Run Full 10-Session Human Learning Eval Suite")
                }
            }
        }

        state.evalSuiteReport?.let { report ->
            val overallPct = (report.overallHumanLearningScore * 100).toInt()
            SizzleSectionCard(
                title = "Overall Human Learning Score: $overallPct% (${report.totalSessionsEvaluated} Sessions)"
            ) {
                Text("• Perception Gap Recall: ${(report.perceptionGapDetectionScore * 100).toInt()}%")
                Text("• Latent Root-Cause Precision: ${(report.latentRootCauseAccuracy * 100).toInt()}%")
                Text("• Single-Issue Pedagogical Restraint: ${(report.singleIssueRestraintScore * 100).toInt()}%")
                Text("• Sensory Grounding (Timer Penalty Check): ${(report.sensoryGroundingScore * 100).toInt()}%")
                Text("• Cross-Attempt Behavioral Transfer: ${(report.crossAttemptTransferScore * 100).toInt()}%")
            }

            report.caseResults.forEach { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "${item.caseId} (Attempt ${item.attemptNumber})",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = item.summaryNote,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
