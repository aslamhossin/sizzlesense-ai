package com.sizzlesense.ai.presentation.evalbench.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.sizzlesense.ai.domain.model.eval.EvalSuiteReport
import com.sizzlesense.ai.presentation.common.component.SizzleSectionCard

@Composable
fun EvalAggregateScoreCard(report: EvalSuiteReport) {
    val overallPct = (report.overallHumanLearningScore * 100).toInt()
    SizzleSectionCard(
        title = "Overall Score: $overallPct% (${report.totalSessionsEvaluated} Sessions)",
        subtitle = "Verified across 5 Human Learning & Sensory Transfer Rubrics",
        icon = Icons.Default.CheckCircle
    ) {
        Text("• Perception Gap Recall: ${(report.perceptionGapDetectionScore * 100).toInt()}%")
        Text("• Latent Root-Cause Precision: ${(report.latentRootCauseAccuracy * 100).toInt()}%")
        Text("• Single-Issue Pedagogical Restraint: ${(report.singleIssueRestraintScore * 100).toInt()}%")
        Text("• Sensory Grounding (Timer Penalty Check): ${(report.sensoryGroundingScore * 100).toInt()}%")
        Text("• Cross-Attempt Behavioral Transfer: ${(report.crossAttemptTransferScore * 100).toInt()}%")
    }
}
