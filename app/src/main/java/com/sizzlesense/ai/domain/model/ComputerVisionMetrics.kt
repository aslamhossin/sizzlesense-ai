package com.sizzlesense.ai.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ComputerVisionMetrics(
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
