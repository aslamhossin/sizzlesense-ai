package com.sizzlesense.ai.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SensoryCoachingOutput(
    @SerialName("visual_cue_to_observe")
    val visualCueToObserve: String,
    @SerialName("auditory_cue_to_listen")
    val auditoryCueToListen: String,
    @SerialName("olfactory_or_tactile_cue")
    val olfactoryOrTactileCue: String,
    @SerialName("coaching_message")
    val coachingMessage: String,
    @SerialName("next_attempt_micro_experiment")
    val nextAttemptMicroExperiment: String
)
