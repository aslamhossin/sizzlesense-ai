package com.sizzlesense.ai.domain.usecase

import com.sizzlesense.ai.domain.model.coaching.CognitiveCoachingResponse
import com.sizzlesense.ai.domain.model.camera.TapoCameraConfig
import com.sizzlesense.ai.domain.repository.LiveTapoCameraGateway
import javax.inject.Inject

class CaptureLiveTapoSessionUseCase @Inject constructor(
    private val gateway: LiveTapoCameraGateway
) {

    suspend operator fun invoke(
        baseUrl: String,
        tapoConfig: TapoCameraConfig,
        attemptNumber: Int,
        reflectionText: String
    ): CognitiveCoachingResponse {
        require(tapoConfig.ipAddress.isNotBlank()) {
            "Tapo C100 IP address cannot be blank."
        }
        return gateway.captureFromBackendTapoAndAnalyze(
            baseUrl = baseUrl,
            tapoConfig = tapoConfig,
            attemptNumber = attemptNumber.coerceIn(1, 3),
            reflectionText = reflectionText.trim()
        )
    }
}
