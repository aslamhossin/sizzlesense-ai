package com.sizzlesense.ai.data.remote.dto.coaching

import com.sizzlesense.ai.domain.model.coaching.FiveCoreQuestionsSummary
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FiveCoreQuestionsSummaryDto(
    @SerialName("what_is_happening_now")
    val whatIsHappeningNow: String,
    @SerialName("what_they_understand_vs_not")
    val whatTheyUnderstandVsNot: String,
    @SerialName("why_they_made_that_decision")
    val whyTheyMadeThatDecision: String,
    @SerialName("what_single_thing_to_teach_now")
    val whatSingleThingToTeachNow: String,
    @SerialName("how_to_communicate_to_shift_judgment")
    val howToCommunicateToShiftJudgment: String
)

fun FiveCoreQuestionsSummaryDto.toDomain(): FiveCoreQuestionsSummary = FiveCoreQuestionsSummary(
    whatIsHappeningNow = whatIsHappeningNow,
    whatTheyUnderstandVsNot = whatTheyUnderstandVsNot,
    whyTheyMadeThatDecision = whyTheyMadeThatDecision,
    whatSingleThingToTeachNow = whatSingleThingToTeachNow,
    howToCommunicateToShiftJudgment = howToCommunicateToShiftJudgment
)

fun FiveCoreQuestionsSummary.toDto(): FiveCoreQuestionsSummaryDto = FiveCoreQuestionsSummaryDto(
    whatIsHappeningNow = whatIsHappeningNow,
    whatTheyUnderstandVsNot = whatTheyUnderstandVsNot,
    whyTheyMadeThatDecision = whyTheyMadeThatDecision,
    whatSingleThingToTeachNow = whatSingleThingToTeachNow,
    howToCommunicateToShiftJudgment = howToCommunicateToShiftJudgment
)
