package com.sizzlesense.ai.presentation.common.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.sizzlesense.ai.presentation.theme.SizzleTheme

@Composable
fun SizzleSectionCard(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = SizzleTheme.radius.lg,
        colors = CardDefaults.cardColors(
            containerColor = SizzleTheme.colors.surfacePrimary
        ),
        border = BorderStroke(
            width = SizzleTheme.spacing.borderStroke,
            color = SizzleTheme.colors.surfaceElevated
        )
    ) {
        Column(
            modifier = Modifier.padding(SizzleTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(SizzleTheme.spacing.sm)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = SizzleTheme.colors.brandPrimary,
                        modifier = Modifier.size(SizzleTheme.spacing.iconSmall)
                    )
                    Spacer(modifier = Modifier.width(SizzleTheme.spacing.xs))
                }
                Column {
                    Text(
                        text = title,
                        style = SizzleTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SizzleTheme.colors.textPrimary
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = SizzleTheme.typography.labelSmall,
                            color = SizzleTheme.colors.textSecondary
                        )
                    }
                }
            }
            content()
        }
    }
}
