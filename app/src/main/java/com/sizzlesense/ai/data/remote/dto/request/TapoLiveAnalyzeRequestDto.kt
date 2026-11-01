package com.sizzlesense.ai.data.remote.dto.request

import com.sizzlesense.ai.domain.model.camera.TapoCameraConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TapoLiveAnalyzeRequestDto(
    @SerialName("ip_address") val ipAddress: String,
    @SerialName("username") val username: String,
    @SerialName("password") val password: String,
    @SerialName("learner_id") val learnerId: String = DEFAULT_TAPO_LEARNER_ID,
    @SerialName("dish_id") val dishId: String = DEFAULT_DISH_ID,
    @SerialName("attempt_number") val attemptNumber: Int = 1,
    @SerialName("learner_reflection") val learnerReflection: String = ""
) {
    companion object {
        const val DEFAULT_TAPO_LEARNER_ID = "tapo_android_learner"
        const val DEFAULT_DISH_ID = "sauteed_onions"
    }
}

fun TapoCameraConfig.toLiveAnalyzeRequestDto(
    attemptNumber: Int,
    reflectionText: String
): TapoLiveAnalyzeRequestDto = TapoLiveAnalyzeRequestDto(
    ipAddress = ipAddress.trim(),
    username = username.trim(),
    password = password,
    attemptNumber = attemptNumber,
    learnerReflection = reflectionText.trim()
)
