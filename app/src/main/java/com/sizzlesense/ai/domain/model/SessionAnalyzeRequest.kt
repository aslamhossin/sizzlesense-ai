package com.sizzlesense.ai.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SessionAnalyzeRequest(
    @SerialName("learner_id")
    val learnerId: String = "android_learner",
    @SerialName("dish_id")
    val dishId: String = "sauteed_onions",
    @SerialName("attempt_number")
    val attemptNumber: Int = 1,
    @SerialName("learner_reflection")
    val learnerReflection: String,
    @SerialName("archetype_hint")
    val archetypeHint: String? = null,
    @SerialName("keyframe_base64_list")
    val keyframeBase64List: List<String> = emptyList(),
    @SerialName("gemini_api_key")
    val geminiApiKey: String? = null,
    @SerialName("preferred_reasoning_model")
    val preferredReasoningModel: String = "gemini-3-flash-preview"
)
