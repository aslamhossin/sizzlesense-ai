package com.sizzlesense.ai.presentation.coaching.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.sizzlesense.ai.domain.model.coaching.SensoryCoachingOutput
import com.sizzlesense.ai.presentation.theme.SizzleTheme

@Composable
fun SensoryCuesList(sensory: SensoryCoachingOutput) {
    Column(verticalArrangement = Arrangement.spacedBy(SizzleTheme.spacing.xxs)) {
        Text(
            text = "Sensory Cues to Shift How You Judge:",
            style = SizzleTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = SizzleTheme.colors.brandPrimary
        )
        Text(
            text = "👁 Visual: ${sensory.visualCueToObserve}",
            style = SizzleTheme.typography.bodySmall
        )
        Text(
            text = "👂 Auditory (Sizzle): ${sensory.auditoryCueToListen}",
            style = SizzleTheme.typography.bodySmall
        )
        Text(
            text = "👃 Aroma: ${sensory.olfactoryOrTactileCue}",
            style = SizzleTheme.typography.bodySmall
        )
    }
}
