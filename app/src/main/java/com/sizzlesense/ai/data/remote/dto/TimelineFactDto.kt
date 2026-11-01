package com.sizzlesense.ai.data.remote.dto

import com.sizzlesense.ai.domain.model.AcousticSizzleSignature
import com.sizzlesense.ai.domain.model.TimelineFact
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
