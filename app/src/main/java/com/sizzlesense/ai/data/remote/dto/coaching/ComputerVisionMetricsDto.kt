package com.sizzlesense.ai.data.remote.dto.coaching

import com.sizzlesense.ai.domain.model.coaching.ComputerVisionMetrics
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ComputerVisionMetricsDto(
    @SerialName("maillard_browning_ratio")
    val maillardBrowningRatio: Double = 0.0,
    @SerialName("surface_moisture_index")
    val surfaceMoistureIndex: Double = 0.0,
    @SerialName("char_burn_ratio")
    val charBurnRatio: Double = 0.0,
    @SerialName("mean_luminance")
    val meanLuminance: Double = 128.0,
    @SerialName("dominant_visual_regime")
    val dominantVisualRegime: String = "TRANSITIONAL_SAUTE"
)

fun ComputerVisionMetricsDto.toDomain(): ComputerVisionMetrics = ComputerVisionMetrics(
    maillardBrowningRatio = maillardBrowningRatio,
    surfaceMoistureIndex = surfaceMoistureIndex,
    charBurnRatio = charBurnRatio,
    meanLuminance = meanLuminance,
    dominantVisualRegime = dominantVisualRegime
)
