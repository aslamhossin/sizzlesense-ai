package com.sizzlesense.ai.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TimelineFact(
    val timestamp: String,
    @SerialName("visual_observation")
    val visualObservation: String,
    @SerialName("acoustic_signature")
    val acousticSignature: AcousticSizzleSignature = AcousticSizzleSignature.WET_STEAM_BUBBLE
)
