package com.sizzlesense.ai.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface SizzleRoute : NavKey {

    @Serializable
    data object Kitchen : SizzleRoute

    @Serializable
    data object Coaching : SizzleRoute

    @Serializable
    data object AiXRay : SizzleRoute

    @Serializable
    data object EvalBench : SizzleRoute

    fun toDestination(): SizzleDestination = when (this) {
        Kitchen -> SizzleDestination.KITCHEN
        Coaching -> SizzleDestination.COACHING
        AiXRay -> SizzleDestination.XRAY
        EvalBench -> SizzleDestination.EVAL_BENCH
    }

    companion object {
        fun fromDestination(destination: SizzleDestination): SizzleRoute = when (destination) {
            SizzleDestination.KITCHEN -> Kitchen
            SizzleDestination.COACHING -> Coaching
            SizzleDestination.XRAY -> AiXRay
            SizzleDestination.EVAL_BENCH -> EvalBench
        }
    }
}
