package com.sizzlesense.ai.domain.model

data class TimelineFact(
    val timestamp: String,
    val visualObservation: String,
    val acousticSignature: AcousticSizzleSignature = AcousticSizzleSignature.WET_STEAM_BUBBLE
)
