package com.sizzlesense.ai.presentation.xray

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sizzlesense.ai.presentation.theme.SizzleTheme
import com.sizzlesense.ai.presentation.xray.component.EmptyXRayTraceState
import com.sizzlesense.ai.presentation.xray.component.Stage1PhysicalObservationCard
import com.sizzlesense.ai.presentation.xray.component.Stage2LearnerLatentStateCard
import com.sizzlesense.ai.presentation.xray.component.Stage3PedagogicalRestraintCard

@Composable
fun AiXRayRoute(
    viewModel: AiXRayViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    AiXRayScreen(
        state = state,
        onIntent = viewModel::onIntent
    )
}

@Composable
fun AiXRayScreen(
    state: AiXRayUiState,
    onIntent: (AiXRayUiIntent) -> Unit
) {
    val resp = state.latestCoachingResponse
    if (resp == null) {
        EmptyXRayTraceState()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(SizzleTheme.spacing.md)
    ) {
        Stage1PhysicalObservationCard(
            visionModelUsed = resp.visionModelUsed,
            stage1 = resp.stage1PhysicalObservation
        )
        Stage2LearnerLatentStateCard(
            reasoningModelUsed = resp.reasoningModelUsed,
            stage2 = resp.stage2LearnerState
        )
        Stage3PedagogicalRestraintCard(
            stage3 = resp.stage3TeachingDecision,
            showSuppressedIssues = state.showSuppressedIssues,
            onToggleSuppressedIssues = { visible ->
                onIntent(AiXRayUiIntent.ToggleSuppressedIssues(visible))
            }
        )
        Spacer(modifier = Modifier.height(SizzleTheme.spacing.md))
    }
}
