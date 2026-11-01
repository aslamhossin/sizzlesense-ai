package com.sizzlesense.ai.presentation.coaching.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.runtime.Composable
import com.sizzlesense.ai.domain.model.FiveCoreQuestionsSummary
import com.sizzlesense.ai.presentation.components.LabeledDetailRow
import com.sizzlesense.ai.presentation.components.SizzleSectionCard

@Composable
fun FiveCoreQuestionsCard(questions: FiveCoreQuestionsSummary) {
    SizzleSectionCard(
        title = "The 5 Core Cookpad JD Questions Answered",
        subtitle = "Synthesized across Stages 1–4",
        icon = Icons.Default.QuestionAnswer
    ) {
        LabeledDetailRow(
            label = "1. What is happening right now?",
            value = questions.whatIsHappeningNow
        )
        LabeledDetailRow(
            label = "2. What do they understand vs. not yet?",
            value = questions.whatTheyUnderstandVsNot
        )
        LabeledDetailRow(
            label = "3. Why did they make that decision?",
            value = questions.whyTheyMadeThatDecision
        )
        LabeledDetailRow(
            label = "4. What single thing should we teach now?",
            value = questions.whatSingleThingToTeachNow
        )
        LabeledDetailRow(
            label = "5. How to communicate to shift judgment?",
            value = questions.howToCommunicateToShiftJudgment
        )
    }
}
