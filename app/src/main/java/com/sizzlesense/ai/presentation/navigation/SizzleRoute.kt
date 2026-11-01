package com.sizzlesense.ai.presentation.navigation

import androidx.navigation3.runtime.NavKey
import com.sizzlesense.ai.presentation.mvi.AppTab
import kotlinx.serialization.Serializable

@Serializable
sealed interface SizzleRoute : NavKey {

    @Serializable
    data object KitchenLive : SizzleRoute

    @Serializable
    data object CoachingLoop : SizzleRoute

    @Serializable
    data object AiXRay : SizzleRoute

    @Serializable
    data object EvalBench : SizzleRoute

    fun toAppTab(): AppTab = when (this) {
        KitchenLive -> AppTab.KITCHEN_LIVE
        CoachingLoop -> AppTab.COACHING_LOOP
        AiXRay -> AppTab.AI_XRAY
        EvalBench -> AppTab.EVAL_BENCH
    }

    companion object {
        fun fromAppTab(tab: AppTab): SizzleRoute = when (tab) {
            AppTab.KITCHEN_LIVE -> KitchenLive
            AppTab.COACHING_LOOP -> CoachingLoop
            AppTab.AI_XRAY -> AiXRay
            AppTab.EVAL_BENCH -> EvalBench
        }
    }
}
