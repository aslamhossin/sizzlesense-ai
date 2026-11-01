package com.sizzlesense.ai.presentation.main.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.sizzlesense.ai.presentation.theme.SizzleTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SizzleTopAppBar() {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = SizzleTheme.colors.backgroundPrimary,
            titleContentColor = SizzleTheme.colors.textPrimary
        ),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(SizzleTheme.spacing.topBarLogoBox)
                        .clip(SizzleTheme.radius.sm)
                        .background(SizzleTheme.colors.brandIconContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = SizzleTheme.colors.brandPrimary,
                        modifier = Modifier.size(SizzleTheme.spacing.iconMedium)
                    )
                }
                Spacer(modifier = Modifier.width(SizzleTheme.spacing.sm))
                Column {
                    Text(
                        text = "SizzleSense AI",
                        style = SizzleTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Human Learning Cooking Coach • Gemini 3",
                        style = SizzleTheme.typography.labelSmall,
                        color = SizzleTheme.colors.textSecondary
                    )
                }
            }
        }
    )
}
