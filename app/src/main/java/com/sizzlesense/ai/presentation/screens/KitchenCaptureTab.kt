package com.sizzlesense.ai.presentation.screens

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sizzlesense.ai.data.camera.TapoRtspPlayerView
import com.sizzlesense.ai.domain.model.TapoCameraConfig
import com.sizzlesense.ai.presentation.components.SizzleSectionCard
import com.sizzlesense.ai.presentation.mvi.LearnerArchetypeOption
import com.sizzlesense.ai.presentation.mvi.SizzleIntent
import com.sizzlesense.ai.presentation.mvi.SizzleUiState

@Composable
fun KitchenCaptureTab(
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
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Switch(
                    checked = tapo.useSubStream,
                    onCheckedChange = { sub -> onIntent(tapo.updateIntent(useSubStream = sub)) }
                )
            }
        }

        SizzleSectionCard(
            title = "3 Cookpad JD Sautéed Onion Learner Archetypes",
            subtitle = "Same pale onion failure -> 3 distinct cognitive diagnoses",
            icon = Icons.Default.Person
        ) {
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
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Text("  Run 4-Stage Applied AI Coaching (Attempt ${state.currentAttemptNumber})")
                }

                OutlinedButton(
                    onClick = { onIntent(SizzleIntent.CaptureFromBackendTapoAndAnalyze) },
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
