package com.sizzlesense.ai.presentation.main.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.sizzlesense.ai.presentation.main.MainUiState
import com.sizzlesense.ai.presentation.theme.SizzleTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SizzleTopAppBar(
    state: MainUiState,
    onAttemptSelected: (Int) -> Unit,
    onOpenDeveloperEvals: () -> Unit,
    onCloseDeveloperEvals: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    // Keep the app bar and attached attempt row on the same surface tone during scroll.
    val headerContainerColor = lerp(
        start = SizzleTheme.colors.surfacePrimary,
        stop = SizzleTheme.colors.surfaceElevated,
        fraction = scrollBehavior.state.overlappedFraction.coerceIn(0f, 1f)
    )

    val titleText = if (state.isDeveloperEvalsOpen) {
        "Developer Evals"
    } else {
        state.activeDestination.label
    }
    val subtitleText = if (state.isDeveloperEvalsOpen) {
        "Debug Only • Quality Benchmark"
    } else {
        state.activeDestination.topBarSubtitle
    }

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
                navigationIcon = {
                    if (state.isDeveloperEvalsOpen) {
                        IconButton(onClick = onCloseDeveloperEvals) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = SizzleTheme.colors.textPrimary
                            )
                        }
                    }
                },
                title = {
                    TopBarTitleContent(
                        title = titleText,
                        subtitle = subtitleText,
                        showBrandIcon = !state.isDeveloperEvalsOpen
                    )
                },
                actions = {
                    if (state.isDeveloperEvalsEnabled && !state.isDeveloperEvalsOpen) {
                        DeveloperEvalsActionButton(onClick = onOpenDeveloperEvals)
                    }
                }
            )

            if (!state.isDeveloperEvalsOpen) {
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
                        state = state,
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

@Composable
private fun TopBarTitleContent(
    title: String,
    subtitle: String,
    showBrandIcon: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (showBrandIcon) {
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
        }
        Column {
            Text(
                text = title,
                style = SizzleTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = SizzleTheme.typography.labelSmall,
                color = SizzleTheme.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DeveloperEvalsActionButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(end = SizzleTheme.spacing.xs)
            .minimumInteractiveComponentSize()
            .clip(SizzleTheme.radius.sm)
            .clickable(
                role = Role.Button,
                onClickLabel = "Open Developer Evals",
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = SizzleTheme.radius.sm,
            color = SizzleTheme.colors.surfaceElevated,
            border = BorderStroke(
                width = SizzleTheme.spacing.borderStroke,
                color = SizzleTheme.colors.outlineSubtle
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(
                    horizontal = SizzleTheme.spacing.xs,
                    vertical = SizzleTheme.spacing.xxs
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Science,
                    contentDescription = null,
                    tint = SizzleTheme.colors.brandPrimary,
                    modifier = Modifier.size(SizzleTheme.spacing.iconSmall)
                )
                Spacer(modifier = Modifier.width(SizzleTheme.spacing.xxs))
                Text(
                    text = "Evals",
                    style = SizzleTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = SizzleTheme.colors.textPrimary
                )
            }
        }
    }
}
