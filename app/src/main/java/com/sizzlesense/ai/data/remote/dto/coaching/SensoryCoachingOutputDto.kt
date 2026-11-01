package com.sizzlesense.ai.data.remote.dto.coaching

import com.sizzlesense.ai.domain.model.coaching.SensoryCoachingOutput
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SensoryCoachingOutputDto(
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

fun SensoryCoachingOutputDto.toDomain(): SensoryCoachingOutput = SensoryCoachingOutput(
    visualCueToObserve = visualCueToObserve,
    auditoryCueToListen = auditoryCueToListen,
    olfactoryOrTactileCue = olfactoryOrTactileCue,
    coachingMessage = coachingMessage,
    nextAttemptMicroExperiment = nextAttemptMicroExperiment
)
