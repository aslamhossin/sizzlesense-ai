package com.sizzlesense.ai.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PhysicalObservationReport(
    @SerialName("observed_timeline_facts")
    val observedTimelineFacts: List<TimelineFact>,
    @SerialName("inferred_physical_states")
    val inferredPhysicalStates: List<String>,
    @SerialName("environmental_uncertainties")
    val environmentalUncertainties: List<String> = emptyList(),
    @SerialName("cv_metrics")
    val cvMetrics: ComputerVisionMetrics? = null,
    @SerialName("learner_stated_belief")
    val learnerStatedBelief: String,
    @SerialName("perception_gap_level")
    val perceptionGapLevel: PerceptionGapLevel,
    @SerialName("perception_gap_explanation")
    val perceptionGapExplanation: String
)
