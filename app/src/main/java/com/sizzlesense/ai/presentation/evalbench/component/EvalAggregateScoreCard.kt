package com.sizzlesense.ai.presentation.evalbench.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.Composable
import com.sizzlesense.ai.domain.model.eval.EvalSuiteReport
import com.sizzlesense.ai.presentation.common.component.ScoreBarRow
import com.sizzlesense.ai.presentation.common.component.SizzleSectionCard

@Composable
fun EvalAggregateScoreCard(report: EvalSuiteReport) {
    val overallPct = (report.overallHumanLearningScore * 100).toInt()
    SizzleSectionCard(
        title = "Overall Score: $overallPct%",
        subtitle = "${report.totalSessionsEvaluated} sessions evaluated",
        icon = Icons.Default.CheckCircle
    ) {
        ScoreBarRow("Blindspot Detection", report.perceptionGapDetectionScore)
        ScoreBarRow("Root-Cause Accuracy", report.latentRootCauseAccuracy)
        ScoreBarRow("Single-Focus Restraint", report.singleIssueRestraintScore)
        ScoreBarRow("Sensory Grounding", report.sensoryGroundingScore)
        ScoreBarRow("Skill Transfer", report.crossAttemptTransferScore)
    }
}
