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
import com.sizzlesense.ai.presentation.evalbench.EvalBenchRoute
import com.sizzlesense.ai.presentation.kitchen.KitchenRoute
import com.sizzlesense.ai.presentation.xray.AiXRayRoute

@Composable
fun SizzleNavGraph(
    activeDestination: SizzleDestination,
    onDestinationSelected: (SizzleDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(SizzleRoute.Kitchen)

    LaunchedEffect(activeDestination) {
        val targetRoute = SizzleRoute.fromDestination(activeDestination)
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
                backStack.removeLastOrNull()
                val previousRoute = backStack.lastOrNull() as? SizzleRoute
                if (previousRoute != null) {
                    onDestinationSelected(previousRoute.toDestination())
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
                EvalBenchRoute()
            }
        }
    )
}
