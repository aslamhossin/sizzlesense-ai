package com.sizzlesense.ai.presentation.kitchen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sizzlesense.ai.data.camera.TapoRtspPlayerView
import com.sizzlesense.ai.domain.model.LearnerArchetypeOption
import com.sizzlesense.ai.domain.model.TapoCameraConfig
import com.sizzlesense.ai.presentation.components.DismissibleErrorBanner
import com.sizzlesense.ai.presentation.components.SizzleSectionCard

@Composable
fun KitchenRoute(
    onNavigateToCoaching: () -> Unit,
    viewModel: KitchenViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                KitchenUiEffect.NavigateToCoaching -> onNavigateToCoaching()
            }
        }
    }

    KitchenScreen(
        state = state,
        onIntent = viewModel::onIntent
    )
}

@Composable
fun KitchenScreen(
    state: KitchenUiState,
    onIntent: (KitchenUiIntent) -> Unit
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
                onDismiss = { onIntent(KitchenUiIntent.ClearError) }
            )
        }

        TapoRtspPlayerView(
            config = state.tapoConfig,
            onConnectToggle = { connect ->
                onIntent(KitchenUiIntent.SetTapoStreamConnected(connect))
            },
            onFrameCaptured = { b64, label ->
                onIntent(KitchenUiIntent.OnLiveFrameCaptured(b64, label))
            }
        )

        TapoBackendSettingsCard(
            tapo = state.tapoConfig,
            backendBaseUrl = state.backendBaseUrl,
            onIntent = onIntent
        )

        OnionArchetypeScenarioCard(
            state = state,
            onIntent = onIntent
        )
    }
}

@Composable
private fun TapoBackendSettingsCard(
    tapo: TapoCameraConfig,
    backendBaseUrl: String,
    onIntent: (KitchenUiIntent) -> Unit
) {
    SizzleSectionCard(
        title = "Tapo C100 RTSP & Python AI Backend",
        subtitle = "Connected on LAN (${tapo.ipAddress}:554)",
        icon = Icons.Default.SettingsEthernet
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = tapo.ipAddress,
                onValueChange = { ip -> onIntent(tapo.updateIntent(ipAddress = ip)) },
                label = { Text("Tapo Camera IP") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = backendBaseUrl,
                onValueChange = { url -> onIntent(KitchenUiIntent.UpdateBackendUrl(url)) },
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
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Switch(
                checked = tapo.useSubStream,
                onCheckedChange = { sub -> onIntent(tapo.updateIntent(useSubStream = sub)) }
            )
        }
    }
}

@Composable
private fun OnionArchetypeScenarioCard(
    state: KitchenUiState,
    onIntent: (KitchenUiIntent) -> Unit
) {
    SizzleSectionCard(
        title = "3 Cookpad JD Sautéed Onion Learner Archetypes",
        subtitle = "Same pale onion failure -> 3 distinct cognitive diagnoses",
        icon = Icons.Default.Person
    ) {
        LearnerArchetypeOption.entries.forEach { option ->
            FilterChip(
                selected = state.selectedArchetype == option,
                onClick = { onIntent(KitchenUiIntent.SelectArchetype(option)) },
                label = { Text(option.title) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        OutlinedTextField(
            value = state.learnerReflectionText,
            onValueChange = { onIntent(KitchenUiIntent.UpdateReflectionText(it)) },
            label = { Text("Post-Cooking Learner Reflection (Voice / Transcript)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Text(
            text = "Frame status: ${state.capturedFrameLabel}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        CoachingActionButtons(
            isAnalyzing = state.isAnalyzing,
            activePipelineStageLabel = state.activePipelineStageLabel,
            currentAttemptNumber = state.currentAttemptNumber,
            onAnalyzeSession = { onIntent(KitchenUiIntent.AnalyzeCurrentSession) },
            onCaptureLiveTapo = { onIntent(KitchenUiIntent.CaptureFromBackendTapoAndAnalyze) }
        )
    }
}

@Composable
private fun CoachingActionButtons(
    isAnalyzing: Boolean,
    activePipelineStageLabel: String,
    currentAttemptNumber: Int,
    onAnalyzeSession: () -> Unit,
    onCaptureLiveTapo: () -> Unit
) {
    if (isAnalyzing) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = activePipelineStageLabel,
                style = MaterialTheme.typography.bodySmall
            )
        }
    } else {
        Button(
            onClick = onAnalyzeSession,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null
            )
            Text("  Run 4-Stage Applied AI Coaching (Attempt $currentAttemptNumber)")
        }

        OutlinedButton(
            onClick = onCaptureLiveTapo,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null
            )
            Text("  Grab Live Tapo C100 Frame via Backend & Analyze")
        }
    }
}

private fun TapoCameraConfig.updateIntent(
    ipAddress: String = this.ipAddress,
    username: String = this.username,
    password: String = this.password,
    useSubStream: Boolean = this.useSubStream
): KitchenUiIntent.UpdateTapoConfig = KitchenUiIntent.UpdateTapoConfig(
    ipAddress = ipAddress,
    username = username,
    password = password,
    useSubStream = useSubStream
)
