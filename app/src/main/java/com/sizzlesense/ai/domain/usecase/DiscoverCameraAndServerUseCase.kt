package com.sizzlesense.ai.domain.usecase

import com.sizzlesense.ai.core.result.AppResult
import com.sizzlesense.ai.domain.model.camera.CameraDiscoveryInfo
import com.sizzlesense.ai.domain.repository.CameraDiscoveryGateway
import javax.inject.Inject

class DiscoverCameraAndServerUseCase @Inject constructor(
    private val gateway: CameraDiscoveryGateway
) {

    suspend operator fun invoke(
        baseUrl: String,
        preferredCameraIp: String
    ): AppResult<CameraDiscoveryInfo> {
        return gateway.discoverCameraAndServer(
            baseUrl = baseUrl.trim(),
            preferredCameraIp = preferredCameraIp.trim()
        )
    }
}
