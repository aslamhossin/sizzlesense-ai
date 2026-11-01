package com.sizzlesense.ai.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.sizzlesense.ai.presentation.mvi.SizzleIntent
import com.sizzlesense.ai.presentation.mvi.SizzleUiState
import com.sizzlesense.ai.presentation.screens.AiXRayRoute
import com.sizzlesense.ai.presentation.screens.CoachingLoopRoute
import com.sizzlesense.ai.presentation.screens.EvalBenchRoute
import com.sizzlesense.ai.presentation.screens.KitchenCaptureRoute

@Composable
fun SizzleNavGraph(
    state: SizzleUiState,
    onIntent: (SizzleIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(SizzleRoute.KitchenLive)

    LaunchedEffect(state.activeTab) {
        val targetRoute = SizzleRoute.fromAppTab(state.activeTab)
        if (backStack.lastOrNull() != targetRoute) {
            backStack.removeAll { it == targetRoute }
            backStack.add(targetRoute)
        }
    }

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
                val previousRoute = backStack.lastOrNull() as? SizzleRoute
                if (previousRoute != null) {
                    onIntent(SizzleIntent.SelectTab(previousRoute.toAppTab()))
                }
            }
        },
        entryProvider = entryProvider<NavKey> {
            entry<SizzleRoute.KitchenLive> {
                KitchenCaptureRoute(
                    state = state,
                    onIntent = onIntent
                )
            }
            entry<SizzleRoute.CoachingLoop> {
                CoachingLoopRoute(
                    state = state,
                    onIntent = onIntent
                )
            }
            entry<SizzleRoute.AiXRay> {
                AiXRayRoute(
                    state = state
                )
            }
            entry<SizzleRoute.EvalBench> {
                EvalBenchRoute(
                    state = state,
                    onIntent = onIntent
                )
            }
        }
    )
}
