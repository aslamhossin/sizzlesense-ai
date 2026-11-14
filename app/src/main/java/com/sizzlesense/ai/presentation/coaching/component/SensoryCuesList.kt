package com.sizzlesense.ai.presentation.coaching.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.sizzlesense.ai.domain.model.coaching.SensoryCoachingOutput
import com.sizzlesense.ai.presentation.theme.SizzleTheme

@Composable
fun SensoryCuesList(sensory: SensoryCoachingOutput) {
    Column(verticalArrangement = Arrangement.spacedBy(SizzleTheme.spacing.xs)) {
        Text(
            text = "What to Notice",
            style = SizzleTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = SizzleTheme.colors.brandPrimary
        )
        SensoryCueRow(
            icon = Icons.Default.Visibility,
            label = "Look",
            description = sensory.visualCueToObserve
        )
        SensoryCueRow(
            icon = Icons.Default.GraphicEq,
            label = "Listen",
            description = sensory.auditoryCueToListen
        )
        SensoryCueRow(
            icon = Icons.Default.LocalFireDepartment,
            label = "Aroma",
            description = sensory.olfactoryOrTactileCue
        )
    }
}

@Composable
private fun SensoryCueRow(
    icon: ImageVector,
    label: String,
    description: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = SizzleTheme.colors.brandPrimary,
            modifier = Modifier.size(SizzleTheme.spacing.iconSmall)
        )
        Spacer(modifier = Modifier.width(SizzleTheme.spacing.xs))
        Column {
            Text(
                text = label,
                style = SizzleTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = SizzleTheme.colors.textPrimary
            )
            Text(
                text = description,
                style = SizzleTheme.typography.bodySmall,
                color = SizzleTheme.colors.textSecondary
            )
        }
    }
}
