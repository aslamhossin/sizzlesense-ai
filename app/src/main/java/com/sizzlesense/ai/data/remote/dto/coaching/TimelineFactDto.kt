package com.sizzlesense.ai.data.remote.dto.coaching

import com.sizzlesense.ai.domain.model.coaching.AcousticSizzleSignature
import com.sizzlesense.ai.domain.model.coaching.TimelineFact
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TimelineFactDto(
    val timestamp: String,
    @SerialName("visual_observation")
    val visualObservation: String,
    @SerialName("acoustic_signature")
    val acousticSignature: String = "WET_STEAM_BUBBLE"
)

fun TimelineFactDto.toDomain(): TimelineFact = TimelineFact(
    timestamp = timestamp,
    visualObservation = visualObservation,
    acousticSignature = runCatching {
        AcousticSizzleSignature.valueOf(acousticSignature)
    }.getOrDefault(AcousticSizzleSignature.WET_STEAM_BUBBLE)
)

fun TimelineFact.toDto(): TimelineFactDto = TimelineFactDto(
    timestamp = timestamp,
    visualObservation = visualObservation,
    acousticSignature = acousticSignature.name
)
