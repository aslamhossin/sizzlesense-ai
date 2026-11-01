package com.sizzlesense.ai.presentation.kitchen.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sizzlesense.ai.domain.model.session.LearnerArchetypeOption
import com.sizzlesense.ai.presentation.common.component.SizzleSectionCard
import com.sizzlesense.ai.presentation.kitchen.KitchenUiIntent
import com.sizzlesense.ai.presentation.kitchen.KitchenUiState
import com.sizzlesense.ai.presentation.theme.SizzleTheme

@Composable
fun OnionArchetypeScenarioCard(
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
            style = SizzleTheme.typography.labelSmall,
            color = SizzleTheme.colors.textSecondary
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
