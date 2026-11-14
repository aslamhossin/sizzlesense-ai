package com.sizzlesense.ai.presentation.navigation

enum class SizzleDestination(
    val label: String,
    val topBarSubtitle: String
) {
    KITCHEN(
        label = "Kitchen",
        topBarSubtitle = "SizzleSense AI • Pan & Sound"
    ),
    COACHING(
        label = "Coach",
        topBarSubtitle = "SizzleSense AI • Sensory Feedback"
    ),
    XRAY(
        label = "AI X-Ray",
        topBarSubtitle = "SizzleSense AI • Reasoning Trace"
    ),
    EVAL_BENCH(
        label = "Evals",
        topBarSubtitle = "SizzleSense AI • Quality Benchmark"
    )
}
