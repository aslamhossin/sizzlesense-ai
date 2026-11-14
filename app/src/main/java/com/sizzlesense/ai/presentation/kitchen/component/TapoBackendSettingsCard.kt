package com.sizzlesense.ai.presentation.kitchen.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.sizzlesense.ai.domain.model.camera.TapoCameraConfig
import com.sizzlesense.ai.presentation.common.component.SizzleSectionCard
import com.sizzlesense.ai.presentation.kitchen.KitchenUiIntent
import com.sizzlesense.ai.presentation.theme.SizzleTheme

@Composable
fun TapoBackendSettingsCard(
    tapo: TapoCameraConfig,
    backendBaseUrl: String,
    onIntent: (KitchenUiIntent) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    SizzleSectionCard(
        title = "Camera & Server Setup",
        subtitle = "Camera ${tapo.ipAddress}",
        icon = Icons.Default.SettingsEthernet,
        modifier = Modifier.clickable { expanded = !expanded },
        trailingHeaderContent = {
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse setup" else "Expand setup",
                tint = SizzleTheme.colors.textSecondary
            )
        }
    ) {
        AnimatedVisibility(visible = expanded) {
            androidx.compose.foundation.layout.Column(
                verticalArrangement = Arrangement.spacedBy(SizzleTheme.spacing.sm)
            ) {
                OutlinedTextField(
                    value = backendBaseUrl,
                    onValueChange = { url -> onIntent(KitchenUiIntent.UpdateBackendUrl(url)) },
                    label = { Text("Server URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = tapo.ipAddress,
                    onValueChange = { ip -> onIntent(tapo.updateIntent(ipAddress = ip)) },
                    label = { Text("Camera IP") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(SizzleTheme.spacing.xs)) {
                    OutlinedTextField(
                        value = tapo.username,
                        onValueChange = { user -> onIntent(tapo.updateIntent(username = user)) },
                        label = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = tapo.password,
                        onValueChange = { pass -> onIntent(tapo.updateIntent(password = pass)) },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Low-latency stream (360p)",
                        style = SizzleTheme.typography.bodySmall,
                        color = SizzleTheme.colors.textSecondary
                    )
                    Switch(
                        checked = tapo.useSubStream,
                        onCheckedChange = { sub -> onIntent(tapo.updateIntent(useSubStream = sub)) }
                    )
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
): KitchenUiIntent.UpdateTapoConfig = KitchenUiIntent.UpdateTapoConfig(
    ipAddress = ipAddress,
    username = username,
    password = password,
    useSubStream = useSubStream
)
