package com.sizzlesense.ai.presentation.kitchen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sizzlesense.ai.presentation.kitchen.component.TapoRtspPlayerView
import com.sizzlesense.ai.presentation.common.component.DismissibleErrorBanner
import com.sizzlesense.ai.presentation.kitchen.component.OnionArchetypeScenarioCard
import com.sizzlesense.ai.presentation.kitchen.component.TapoBackendSettingsCard

@Composable
fun KitchenRoute(
    onNavigateToCoaching: () -> Unit,
    viewModel: KitchenViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                KitchenUiEffect.NavigateToCoaching -> onNavigateToCoaching()
            }
        }
    }

    KitchenScreen(
        state = state,
        onIntent = viewModel::onIntent
    )
}

@Composable
fun KitchenScreen(
    state: KitchenUiState,
    onIntent: (KitchenUiIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        state.errorMessage?.let { err ->
            DismissibleErrorBanner(
                errorMessage = err,
                onDismiss = { onIntent(KitchenUiIntent.ClearError) }
            )
        }

        TapoRtspPlayerView(
            config = state.tapoConfig,
            onConnectToggle = { connect ->
                onIntent(KitchenUiIntent.SetTapoStreamConnected(connect))
            },
            onFrameCaptured = { b64, label ->
                onIntent(KitchenUiIntent.OnLiveFrameCaptured(b64, label))
            }
        )

        TapoBackendSettingsCard(
            tapo = state.tapoConfig,
            backendBaseUrl = state.backendBaseUrl,
            onIntent = onIntent
        )

        OnionArchetypeScenarioCard(
            state = state,
            onIntent = onIntent
        )
    }
}
