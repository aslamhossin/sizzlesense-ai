package com.sizzlesense.ai.presentation.coaching.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sizzlesense.ai.domain.model.coaching.SensoryCoachingOutput

@Composable
fun SensoryCuesList(sensory: SensoryCoachingOutput) {
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
