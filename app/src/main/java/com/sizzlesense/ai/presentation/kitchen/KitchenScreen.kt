package com.sizzlesense.ai.presentation.kitchen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sizzlesense.ai.presentation.common.component.DismissibleErrorBanner
import com.sizzlesense.ai.presentation.kitchen.component.AcousticSizzleCaptureCard
import com.sizzlesense.ai.presentation.kitchen.component.OnionArchetypeScenarioCard
import com.sizzlesense.ai.presentation.kitchen.component.TapoBackendSettingsCard
import com.sizzlesense.ai.presentation.kitchen.component.TapoRtspPlayerView
import com.sizzlesense.ai.presentation.theme.SizzleTheme

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
            .verticalScroll(rememberScrollState())
            .padding(top = SizzleTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(SizzleTheme.spacing.md)
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

        AcousticSizzleCaptureCard(
            capturedAudioLabel = state.capturedAudioLabel,
            onAudioCaptured = { wavB64, label ->
                onIntent(KitchenUiIntent.OnAudioSampleCaptured(wavB64, label))
            }
        )

        OnionArchetypeScenarioCard(
            state = state,
            onIntent = onIntent
        )

        TapoBackendSettingsCard(
            tapo = state.tapoConfig,
            backendBaseUrl = state.backendBaseUrl,
            onIntent = onIntent
        )

        Spacer(modifier = Modifier.height(SizzleTheme.spacing.md))
    }
}
