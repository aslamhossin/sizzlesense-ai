package com.sizzlesense.ai.data.remote.dto.request

import com.sizzlesense.ai.domain.model.session.SessionAnalyzeRequest
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
    @SerialName("audio_wav_base64")
    val audioWavBase64: String? = null,
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
    audioWavBase64 = audioWavBase64,
    geminiApiKey = geminiApiKey,
    preferredReasoningModel = preferredReasoningModel
)
