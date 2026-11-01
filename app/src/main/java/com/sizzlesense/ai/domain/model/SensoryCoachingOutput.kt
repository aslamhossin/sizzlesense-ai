package com.sizzlesense.ai.domain.model

data class SensoryCoachingOutput(
    val visualCueToObserve: String,
    val auditoryCueToListen: String,
    val olfactoryOrTactileCue: String,
    val coachingMessage: String,
    val nextAttemptMicroExperiment: String
)
