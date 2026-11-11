package com.sizzlesense.ai.data.remote.dto.coaching

import com.sizzlesense.ai.domain.model.coaching.AcousticSizzleSignature
import com.sizzlesense.ai.domain.model.coaching.AcousticSpectralMetrics
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AcousticSpectralMetricsDto(
    @SerialName("dominant_frequency_hz")
    val dominantFrequencyHz: Double = 0.0,
    @SerialName("spectral_centroid_hz")
    val spectralCentroidHz: Double = 0.0,
    @SerialName("high_freq_sizzle_ratio")
    val highFreqSizzleRatio: Double = 0.0,
    @SerialName("low_freq_bubble_ratio")
    val lowFreqBubbleRatio: Double = 0.0,
    @SerialName("rms_energy_db")
    val rmsEnergyDb: Double = -60.0,
    @SerialName("inferred_acoustic_signature")
    val inferredAcousticSignature: String = "WET_STEAM_BUBBLE"
)

fun AcousticSpectralMetricsDto.toDomain(): AcousticSpectralMetrics = AcousticSpectralMetrics(
    dominantFrequencyHz = dominantFrequencyHz,
    spectralCentroidHz = spectralCentroidHz,
    highFreqSizzleRatio = highFreqSizzleRatio,
    lowFreqBubbleRatio = lowFreqBubbleRatio,
    rmsEnergyDb = rmsEnergyDb,
    inferredAcousticSignature = runCatching {
        AcousticSizzleSignature.valueOf(inferredAcousticSignature)
    }.getOrDefault(AcousticSizzleSignature.WET_STEAM_BUBBLE)
)

fun AcousticSpectralMetrics.toDto(): AcousticSpectralMetricsDto = AcousticSpectralMetricsDto(
    dominantFrequencyHz = dominantFrequencyHz,
    spectralCentroidHz = spectralCentroidHz,
    highFreqSizzleRatio = highFreqSizzleRatio,
    lowFreqBubbleRatio = lowFreqBubbleRatio,
    rmsEnergyDb = rmsEnergyDb,
    inferredAcousticSignature = inferredAcousticSignature.name
)
