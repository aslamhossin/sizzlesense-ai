package com.sizzlesense.ai.presentation.evalbench.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.sizzlesense.ai.domain.model.eval.EvalCaseScore
import com.sizzlesense.ai.presentation.theme.SizzleTheme

@Composable
fun EvalCaseResultCard(item: EvalCaseScore) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = SizzleTheme.radius.md,
        colors = CardDefaults.cardColors(
            containerColor = SizzleTheme.colors.surfaceElevated
        )
    ) {
        Column(modifier = Modifier.padding(SizzleTheme.spacing.md)) {
            Text(
                text = "${item.caseId} (Attempt ${item.attemptNumber})",
                fontWeight = FontWeight.SemiBold,
                style = SizzleTheme.typography.labelLarge,
                color = SizzleTheme.colors.brandPrimary
            )
            Text(
                text = item.summaryNote,
                style = SizzleTheme.typography.bodySmall,
                color = SizzleTheme.colors.textPrimary
            )
        }
    }
}
