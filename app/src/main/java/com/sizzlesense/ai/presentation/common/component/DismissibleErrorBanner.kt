package com.sizzlesense.ai.presentation.common.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.sizzlesense.ai.presentation.theme.SizzleTheme

@Composable
fun DismissibleErrorBanner(
    errorMessage: String,
    onDismiss: () -> Unit
) {
    Card(
        shape = SizzleTheme.radius.md,
        colors = CardDefaults.cardColors(
            containerColor = SizzleTheme.colors.statusErrorContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = SizzleTheme.spacing.md,
                    vertical = SizzleTheme.spacing.sm
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = errorMessage,
                color = SizzleTheme.colors.statusError,
                style = SizzleTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss error",
                    tint = SizzleTheme.colors.statusError
                )
            }
        }
    }
}
