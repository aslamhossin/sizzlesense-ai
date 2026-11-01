package com.sizzlesense.ai.domain.model.coaching

data class ComputerVisionMetrics(
    val maillardBrowningRatio: Double = 0.0,
    val surfaceMoistureIndex: Double = 0.0,
    val charBurnRatio: Double = 0.0,
    val meanLuminance: Double = 128.0,
    val dominantVisualRegime: String = "TRANSITIONAL_SAUTE"
)
