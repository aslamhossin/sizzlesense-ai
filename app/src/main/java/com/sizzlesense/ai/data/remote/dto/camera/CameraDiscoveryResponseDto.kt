package com.sizzlesense.ai.data.remote.dto.camera

import com.sizzlesense.ai.domain.model.camera.CameraDiscoveryInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CameraDiscoveryResponseDto(
    @SerialName("backend_url")
    val backendUrl: String,
    @SerialName("camera_ip")
    val cameraIp: String? = null,
    @SerialName("camera_port")
    val cameraPort: Int = 554,
    @SerialName("camera_reachable")
    val cameraReachable: Boolean = false,
    @SerialName("has_backend_credentials")
    val hasBackendCredentials: Boolean = false,
    @SerialName("discovery_source")
    val discoverySource: String = "none"
)

fun CameraDiscoveryResponseDto.toDomain(
    resolvedBackendUrl: String
): CameraDiscoveryInfo = CameraDiscoveryInfo(
    resolvedBackendUrl = resolvedBackendUrl,
    advertisedBackendUrl = backendUrl,
    cameraIp = cameraIp,
    cameraPort = cameraPort,
    cameraReachable = cameraReachable,
    hasBackendCredentials = hasBackendCredentials,
    discoverySource = discoverySource
)
