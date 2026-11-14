package com.sizzlesense.ai.presentation.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sizzlesense.ai.presentation.main.component.AttemptSelectorBar
import com.sizzlesense.ai.presentation.main.component.SizzleBottomNavigationBar
import com.sizzlesense.ai.presentation.main.component.SizzleTopAppBar
import com.sizzlesense.ai.presentation.navigation.SizzleDestination
import com.sizzlesense.ai.presentation.navigation.SizzleNavGraph
import com.sizzlesense.ai.presentation.theme.SizzleTheme

@Composable
fun SizzleMainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val onIntent: (MainUiIntent) -> Unit = viewModel::onIntent

    Scaffold(
        containerColor = SizzleTheme.colors.backgroundPrimary,
        topBar = {
            SizzleTopAppBar()
        },
        bottomBar = {
            SizzleBottomNavigationBar(
                activeDestination = state.activeDestination,
                onDestinationSelected = { destination ->
                    onIntent(MainUiIntent.SelectDestination(destination))
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(
                    horizontal = SizzleTheme.spacing.lg,
                    vertical = SizzleTheme.spacing.xs
                ),
            verticalArrangement = Arrangement.spacedBy(SizzleTheme.spacing.sm)
        ) {
            if (state.activeDestination != SizzleDestination.EVAL_BENCH) {
                AttemptSelectorBar(
                    state = state,
                    onAttemptSelected = { attempt ->
                        onIntent(MainUiIntent.SelectAttemptNumber(attempt))
                    }
                )
            }

            SizzleNavGraph(
                activeDestination = state.activeDestination,
                onDestinationSelected = { destination ->
                    onIntent(MainUiIntent.SelectDestination(destination))
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
