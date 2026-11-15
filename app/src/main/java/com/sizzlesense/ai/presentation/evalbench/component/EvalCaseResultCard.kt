package com.sizzlesense.ai.presentation.evalbench.component

import androidx.compose.foundation.layout.Arrangement
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
import com.sizzlesense.ai.presentation.common.component.MetricPillGrid
import com.sizzlesense.ai.presentation.common.component.MetricTileItem
import com.sizzlesense.ai.presentation.common.format.DisplayLabelFormatter
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
        Column(
            modifier = Modifier.padding(SizzleTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(SizzleTheme.spacing.xs)
        ) {
            Text(
                text = "${DisplayLabelFormatter.formatEvalCaseTitle(item.caseId)} • Attempt ${item.attemptNumber}",
                fontWeight = FontWeight.SemiBold,
                style = SizzleTheme.typography.labelLarge,
                color = SizzleTheme.colors.brandPrimary
            )
            Text(
                text = DisplayLabelFormatter.formatEvalNote(item.summaryNote),
                style = SizzleTheme.typography.bodySmall,
                color = SizzleTheme.colors.textPrimary
            )
            MetricPillGrid(
                items = listOf(
                    MetricTileItem("Blindspot", if (item.perceptionGapPassed) "Pass" else "Miss"),
                    MetricTileItem("Root Cause", if (item.rootCausePassed) "Pass" else "Miss"),
                    MetricTileItem("Restraint", if (item.singleIssueRestraintPassed) "Pass" else "Miss"),
                    MetricTileItem("Sensory", DisplayLabelFormatter.formatRatioPercent(item.sensoryGroundingScore)),
                    MetricTileItem("Transfer", if (item.transferTrackingPassed) "Pass" else "Miss")
                )
            )
        }
    }
}
