package com.sizzlesense.ai.data.remote.dto.coaching

import com.sizzlesense.ai.domain.model.coaching.PerceptionGapLevel
import com.sizzlesense.ai.domain.model.coaching.PhysicalObservationReport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PhysicalObservationReportDto(
    @SerialName("observed_timeline_facts")
    val observedTimelineFacts: List<TimelineFactDto>,
    @SerialName("inferred_physical_states")
    val inferredPhysicalStates: List<String>,
    @SerialName("environmental_uncertainties")
    val environmentalUncertainties: List<String> = emptyList(),
    @SerialName("cv_metrics")
    val cvMetrics: ComputerVisionMetricsDto? = null,
    @SerialName("learner_stated_belief")
    val learnerStatedBelief: String,
    @SerialName("perception_gap_level")
    val perceptionGapLevel: String,
    @SerialName("perception_gap_explanation")
    val perceptionGapExplanation: String
)

fun PhysicalObservationReportDto.toDomain(): PhysicalObservationReport = PhysicalObservationReport(
    observedTimelineFacts = observedTimelineFacts.map { it.toDomain() },
    inferredPhysicalStates = inferredPhysicalStates,
    environmentalUncertainties = environmentalUncertainties,
    cvMetrics = cvMetrics?.toDomain(),
    learnerStatedBelief = learnerStatedBelief,
    perceptionGapLevel = runCatching {
        PerceptionGapLevel.valueOf(perceptionGapLevel)
    }.getOrDefault(PerceptionGapLevel.ALIGNED),
    perceptionGapExplanation = perceptionGapExplanation
)

fun PhysicalObservationReport.toDto(): PhysicalObservationReportDto = PhysicalObservationReportDto(
    observedTimelineFacts = observedTimelineFacts.map { it.toDto() },
    inferredPhysicalStates = inferredPhysicalStates,
    environmentalUncertainties = environmentalUncertainties,
    cvMetrics = cvMetrics?.toDto(),
    learnerStatedBelief = learnerStatedBelief,
    perceptionGapLevel = perceptionGapLevel.name,
    perceptionGapExplanation = perceptionGapExplanation
)
