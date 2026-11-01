package com.sizzlesense.ai.domain.model

data class SessionAnalyzeRequest(
    val learnerId: String = "android_learner",
    val dishId: String = "sauteed_onions",
    val attemptNumber: Int = 1,
    val learnerReflection: String,
    val archetypeHint: String? = null,
    val keyframeBase64List: List<String> = emptyList(),
    val geminiApiKey: String? = null,
    val preferredReasoningModel: String = "gemini-3-flash-preview"
)
