package com.sizzlesense.ai.presentation.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sizzlesense.ai.presentation.main.component.AttemptSelectorBar
import com.sizzlesense.ai.presentation.main.component.SizzleBottomNavigationBar
import com.sizzlesense.ai.presentation.main.component.SizzleTopAppBar
import com.sizzlesense.ai.presentation.navigation.SizzleNavGraph

@Composable
fun SizzleMainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val onIntent: (MainUiIntent) -> Unit = viewModel::onIntent

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AttemptSelectorBar(
                state = state,
                onAttemptSelected = { attempt ->
                    onIntent(MainUiIntent.SelectAttemptNumber(attempt))
                }
            )

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
