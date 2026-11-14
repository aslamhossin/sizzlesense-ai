package com.sizzlesense.ai.presentation.main.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.sizzlesense.ai.presentation.main.MainUiState
import com.sizzlesense.ai.presentation.navigation.SizzleDestination
import com.sizzlesense.ai.presentation.theme.SizzleTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SizzleTopAppBar(
    activeDestination: SizzleDestination,
    attemptState: MainUiState,
    showAttemptSelector: Boolean,
    onAttemptSelected: (Int) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    // Keep the app bar and attached attempt row on the same surface tone during scroll.
    val headerContainerColor = lerp(
        start = SizzleTheme.colors.surfacePrimary,
        stop = SizzleTheme.colors.surfaceElevated,
        fraction = scrollBehavior.state.overlappedFraction.coerceIn(0f, 1f)
    )

    Surface(
        color = headerContainerColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = headerContainerColor,
                    scrolledContainerColor = headerContainerColor,
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
                                text = activeDestination.label,
                                style = SizzleTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = activeDestination.topBarSubtitle,
                                style = SizzleTheme.typography.labelSmall,
                                color = SizzleTheme.colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            )

            if (showAttemptSelector) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = SizzleTheme.spacing.lg,
                            end = SizzleTheme.spacing.lg,
                            bottom = SizzleTheme.spacing.sm
                        )
                ) {
                    AttemptSelectorBar(
                        state = attemptState,
                        onAttemptSelected = onAttemptSelected
                    )
                }
            }

            HorizontalDivider(
                thickness = SizzleTheme.spacing.borderStroke,
                color = SizzleTheme.colors.outlineSubtle
            )
        }
    }
}
