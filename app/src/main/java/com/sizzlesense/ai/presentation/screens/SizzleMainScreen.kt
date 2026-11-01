package com.sizzlesense.ai.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sizzlesense.ai.presentation.components.DismissibleErrorBanner
import com.sizzlesense.ai.presentation.mvi.AppTab
import com.sizzlesense.ai.presentation.mvi.SizzleIntent
import com.sizzlesense.ai.presentation.mvi.SizzleUiState
import com.sizzlesense.ai.presentation.mvi.SizzleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SizzleMainScreen(
    viewModel: SizzleViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val onIntent: (SizzleIntent) -> Unit = viewModel::dispatch

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "SizzleSense AI",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Human Learning Cooking Coach • Gemini 3",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = state.activeTab == tab,
                        onClick = { onIntent(SizzleIntent.SelectTab(tab)) },
                        icon = {
                            Icon(
                                imageVector = tab.toIcon(),
                                contentDescription = tab.label
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                maxLines = 1
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AttemptSelectorBar(
                state = state,
                onIntent = onIntent
            )

            state.errorMessage?.let { err ->
                DismissibleErrorBanner(
                    errorMessage = err,
                    onDismiss = { onIntent(SizzleIntent.ClearError) }
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                when (state.activeTab) {
                    AppTab.KITCHEN_LIVE -> KitchenCaptureTab(
                        state = state,
                        onIntent = onIntent
                    )
                    AppTab.COACHING_LOOP -> CoachingLoopTab(
                        state = state,
                        onIntent = onIntent
                    )
                    AppTab.AI_XRAY -> AiXRayInspectorTab(
                        state = state
                    )
                    AppTab.EVAL_BENCH -> EvalBenchTab(
                        state = state,
                        onIntent = onIntent
                    )
                }
            }
        }
    }
}

@Composable
private fun AttemptSelectorBar(
    state: SizzleUiState,
    onIntent: (SizzleIntent) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(1, 2, 3).forEach { attempt ->
            val completed = state.attemptHistory.containsKey(attempt)
            FilterChip(
                selected = state.currentAttemptNumber == attempt,
                onClick = { onIntent(SizzleIntent.SelectAttemptNumber(attempt)) },
                label = {
                    Text(
                        text = if (completed) "Attempt $attempt ✓" else "Attempt $attempt"
                    )
                }
            )
        }
    }
}

private fun AppTab.toIcon(): ImageVector = when (this) {
    AppTab.KITCHEN_LIVE -> Icons.Default.Videocam
    AppTab.COACHING_LOOP -> Icons.Default.School
    AppTab.AI_XRAY -> Icons.Default.Psychology
    AppTab.EVAL_BENCH -> Icons.Default.Analytics
}
