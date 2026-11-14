package com.sizzlesense.ai.domain.repository

import com.sizzlesense.ai.core.result.AppResult
import com.sizzlesense.ai.domain.model.camera.CameraDiscoveryInfo

fun interface CameraDiscoveryGateway {
    suspend fun discoverCameraAndServer(
        baseUrl: String,
        preferredCameraIp: String
    ): AppResult<CameraDiscoveryInfo>
}
