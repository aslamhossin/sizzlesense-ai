package com.sizzlesense.ai.domain.model.coaching

data class PhysicalObservationReport(
    val observedTimelineFacts: List<TimelineFact>,
    val inferredPhysicalStates: List<String>,
    val environmentalUncertainties: List<String> = emptyList(),
    val cvMetrics: ComputerVisionMetrics? = null,
    val learnerStatedBelief: String,
    val perceptionGapLevel: PerceptionGapLevel,
    val perceptionGapExplanation: String
)
