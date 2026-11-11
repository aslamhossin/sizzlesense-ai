package com.sizzlesense.ai.domain.model.coaching

data class AcousticSpectralMetrics(
    val dominantFrequencyHz: Double,
    val spectralCentroidHz: Double,
    val highFreqSizzleRatio: Double,
    val lowFreqBubbleRatio: Double,
    val rmsEnergyDb: Double,
    val inferredAcousticSignature: AcousticSizzleSignature
)
