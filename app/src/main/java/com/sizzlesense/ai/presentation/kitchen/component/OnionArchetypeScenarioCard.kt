package com.sizzlesense.ai.presentation.kitchen.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
        title = "Cooking Scenario",
        subtitle = state.selectedArchetype.description,
        icon = Icons.Default.RestaurantMenu
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SizzleTheme.spacing.xs)
        ) {
            LearnerArchetypeOption.entries.forEach { option ->
                FilterChip(
                    selected = state.selectedArchetype == option,
                    onClick = { onIntent(KitchenUiIntent.SelectArchetype(option)) },
                    modifier = Modifier.weight(1f),
                    label = {
                        Text(
                            text = option.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                )
            }
        }

        OutlinedTextField(
            value = state.learnerReflectionText,
            onValueChange = { onIntent(KitchenUiIntent.UpdateReflectionText(it)) },
            label = { Text("What happened in the pan?") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        if (state.capturedFrameLabel.isNotBlank()) {
            Text(
                text = "Attached: ${state.capturedFrameLabel}",
                style = SizzleTheme.typography.labelSmall,
                color = SizzleTheme.colors.accentSuccess
            )
        }

        CoachingActionButtons(
            isAnalyzing = state.isAnalyzing,
            activePipelineStageLabel = state.activePipelineStageLabel,
            currentAttemptNumber = state.currentAttemptNumber,
            onAnalyzeSession = { onIntent(KitchenUiIntent.AnalyzeCurrentSession) },
            onCaptureLiveTapo = { onIntent(KitchenUiIntent.CaptureFromBackendTapoAndAnalyze) }
        )
    }
}
