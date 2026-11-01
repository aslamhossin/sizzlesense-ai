package com.sizzlesense.ai.presentation.kitchen.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sizzlesense.ai.domain.model.camera.TapoCameraConfig
import com.sizzlesense.ai.presentation.common.component.SizzleSectionCard
import com.sizzlesense.ai.presentation.kitchen.KitchenUiIntent

@Composable
fun TapoBackendSettingsCard(
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
