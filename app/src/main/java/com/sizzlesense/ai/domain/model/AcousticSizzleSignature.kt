package com.sizzlesense.ai.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class AcousticSizzleSignature {
    SILENT_COLD,
    WET_STEAM_BUBBLE,
    STEADY_APPLAUSE_SIZZLE,
    AGGRESSIVE_POPPING_BURN
}
