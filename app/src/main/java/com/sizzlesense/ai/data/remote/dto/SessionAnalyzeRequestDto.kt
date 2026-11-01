package com.sizzlesense.ai.data.remote.dto

import com.sizzlesense.ai.domain.model.SessionAnalyzeRequest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SessionAnalyzeRequestDto(
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

fun SessionAnalyzeRequest.toDto(): SessionAnalyzeRequestDto = SessionAnalyzeRequestDto(
    learnerId = learnerId,
    dishId = dishId,
    attemptNumber = attemptNumber,
    learnerReflection = learnerReflection,
    archetypeHint = archetypeHint,
    keyframeBase64List = keyframeBase64List,
    geminiApiKey = geminiApiKey,
    preferredReasoningModel = preferredReasoningModel
)
