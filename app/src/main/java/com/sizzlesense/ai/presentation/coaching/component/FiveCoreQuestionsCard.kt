package com.sizzlesense.ai.presentation.coaching.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.sizzlesense.ai.domain.model.coaching.FiveCoreQuestionsSummary
import com.sizzlesense.ai.presentation.common.component.LabeledDetailRow
import com.sizzlesense.ai.presentation.common.component.SizzleSectionCard
import com.sizzlesense.ai.presentation.theme.SizzleTheme

@Composable
fun FiveCoreQuestionsCard(questions: FiveCoreQuestionsSummary) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    SizzleSectionCard(
        title = "Why This Advice",
        subtitle = if (expanded) "Tap to collapse" else "Tap to view step-by-step reasoning",
        icon = Icons.Default.QuestionAnswer,
        modifier = Modifier.clickable { expanded = !expanded },
        trailingHeaderContent = {
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = SizzleTheme.colors.textSecondary
            )
        }
    ) {
        AnimatedVisibility(visible = expanded) {
            Column(verticalArrangement = Arrangement.spacedBy(SizzleTheme.spacing.sm)) {
                LabeledDetailRow(
                    label = "What's happening",
                    value = questions.whatIsHappeningNow
                )
                LabeledDetailRow(
                    label = "Current gap",
                    value = questions.whatTheyUnderstandVsNot
                )
                LabeledDetailRow(
                    label = "Why it happened",
                    value = questions.whyTheyMadeThatDecision
                )
                LabeledDetailRow(
                    label = "Core lesson",
                    value = questions.whatSingleThingToTeachNow
                )
                LabeledDetailRow(
                    label = "Sensory shift",
                    value = questions.howToCommunicateToShiftJudgment
                )
            }
        }
    }
}
