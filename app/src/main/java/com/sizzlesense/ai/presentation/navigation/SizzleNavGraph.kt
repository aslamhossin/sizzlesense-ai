package com.sizzlesense.ai.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.sizzlesense.ai.presentation.coaching.CoachingRoute
import com.sizzlesense.ai.presentation.kitchen.KitchenRoute
import com.sizzlesense.ai.presentation.xray.AiXRayRoute

@Composable
fun SizzleNavGraph(
    activeDestination: SizzleDestination,
    isDeveloperEvalsOpen: Boolean,
    onDestinationSelected: (SizzleDestination) -> Unit,
    onCloseDeveloperEvals: () -> Unit,
    evalBenchContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(SizzleRoute.Kitchen)

    LaunchedEffect(activeDestination, isDeveloperEvalsOpen) {
        if (!isDeveloperEvalsOpen) {
            backStack.removeAll { it == SizzleRoute.EvalBench }
        }
        val targetRoute = if (isDeveloperEvalsOpen) {
            SizzleRoute.EvalBench
        } else {
            SizzleRoute.fromDestination(activeDestination)
        }
        if (backStack.lastOrNull() != targetRoute) {
            backStack.removeAll { it == targetRoute }
            backStack.add(targetRoute)
        }
    }

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator()
        ),
        onBack = {
            if (backStack.size > 1) {
                val removedRoute = backStack.removeLastOrNull()
                if (removedRoute == SizzleRoute.EvalBench) {
                    onCloseDeveloperEvals()
                } else {
                    val previousRoute = backStack.lastOrNull() as? SizzleRoute
                    previousRoute?.toDestinationOrNull()?.let(onDestinationSelected)
                }
            }
        },
        entryProvider = entryProvider<NavKey> {
            entry<SizzleRoute.Kitchen> {
                KitchenRoute(
                    onNavigateToCoaching = {
                        onDestinationSelected(SizzleDestination.COACHING)
                    }
                )
            }
            entry<SizzleRoute.Coaching> {
                CoachingRoute()
            }
            entry<SizzleRoute.AiXRay> {
                AiXRayRoute()
            }
            entry<SizzleRoute.EvalBench> {
                evalBenchContent()
            }
        }
    )
}
