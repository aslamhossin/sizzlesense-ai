package com.sizzlesense.ai.presentation.common.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.sizzlesense.ai.presentation.theme.SizzleTheme

@Composable
fun LabeledDetailRow(
    label: String,
    value: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(SizzleTheme.spacing.xxs)) {
        Text(
            text = label,
            style = SizzleTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = SizzleTheme.colors.brandPrimary
        )
        Text(
            text = value,
            style = SizzleTheme.typography.bodySmall,
            color = SizzleTheme.colors.textPrimary
        )
    }
}
