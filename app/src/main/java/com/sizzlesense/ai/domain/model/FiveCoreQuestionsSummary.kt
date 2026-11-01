package com.sizzlesense.ai.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FiveCoreQuestionsSummary(
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
