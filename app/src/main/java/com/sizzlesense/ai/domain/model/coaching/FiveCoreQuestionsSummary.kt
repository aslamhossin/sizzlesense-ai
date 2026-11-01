package com.sizzlesense.ai.domain.model.coaching

data class FiveCoreQuestionsSummary(
    val whatIsHappeningNow: String,
    val whatTheyUnderstandVsNot: String,
    val whyTheyMadeThatDecision: String,
    val whatSingleThingToTeachNow: String,
    val howToCommunicateToShiftJudgment: String
)
