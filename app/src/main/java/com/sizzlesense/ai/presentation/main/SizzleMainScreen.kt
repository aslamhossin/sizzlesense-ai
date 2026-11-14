package com.sizzlesense.ai.presentation.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sizzlesense.ai.presentation.main.component.SizzleBottomNavigationBar
import com.sizzlesense.ai.presentation.main.component.SizzleTopAppBar
import com.sizzlesense.ai.presentation.navigation.SizzleDestination
import com.sizzlesense.ai.presentation.navigation.SizzleNavGraph
import com.sizzlesense.ai.presentation.theme.SizzleTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SizzleMainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val onIntent: (MainUiIntent) -> Unit = viewModel::onIntent
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = SizzleTheme.colors.backgroundPrimary,
        topBar = {
            SizzleTopAppBar(
                activeDestination = state.activeDestination,
                attemptState = state,
                showAttemptSelector = state.activeDestination != SizzleDestination.EVAL_BENCH,
                onAttemptSelected = { attempt ->
                    onIntent(MainUiIntent.SelectAttemptNumber(attempt))
                },
                scrollBehavior = scrollBehavior
            )
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = SizzleTheme.spacing.lg)
        ) {
            SizzleNavGraph(
                activeDestination = state.activeDestination,
                onDestinationSelected = { destination ->
                    onIntent(MainUiIntent.SelectDestination(destination))
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
