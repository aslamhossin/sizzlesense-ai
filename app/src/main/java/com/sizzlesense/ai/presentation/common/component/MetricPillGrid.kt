package com.sizzlesense.ai.presentation.common.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sizzlesense.ai.presentation.theme.SizzleTheme
import kotlin.math.roundToInt

data class MetricTileItem(
    val label: String,
    val value: String
)

@Composable
fun MetricPillGrid(
    items: List<MetricTileItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SizzleTheme.spacing.xs)
    ) {
        items.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SizzleTheme.spacing.xs)
            ) {
                pair.forEach { item ->
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = SizzleTheme.radius.sm,
                        color = SizzleTheme.colors.surfaceElevated
                    ) {
                        Column(modifier = Modifier.padding(SizzleTheme.spacing.sm)) {
                            Text(
                                text = item.label,
                                style = SizzleTheme.typography.labelSmall,
                                color = SizzleTheme.colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = item.value,
                                style = SizzleTheme.typography.bodySmall.copy(
                                    fontFeatureSettings = "tnum"
                                ),
                                fontWeight = FontWeight.SemiBold,
                                color = SizzleTheme.colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ScoreBarRow(
    label: String,
    score: Double,
    modifier: Modifier = Modifier
) {
    val clamped = score.toFloat().coerceIn(0f, 1f)
    val percent = (clamped * 100f).roundToInt()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SizzleTheme.spacing.xxs)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = SizzleTheme.typography.bodySmall,
                color = SizzleTheme.colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$percent%",
                style = SizzleTheme.typography.labelMedium.copy(
                    fontFeatureSettings = "tnum"
                ),
                fontWeight = FontWeight.SemiBold,
                color = SizzleTheme.colors.brandPrimary
            )
        }
        LinearProgressIndicator(
            progress = { clamped },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(SizzleTheme.radius.sm),
            color = SizzleTheme.colors.brandPrimary,
            trackColor = SizzleTheme.colors.surfaceElevated
        )
    }
}
