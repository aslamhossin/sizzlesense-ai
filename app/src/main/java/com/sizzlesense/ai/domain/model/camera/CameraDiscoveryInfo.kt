package com.sizzlesense.ai.domain.model.camera

/**
 * Result of probing the AI backend and LAN Tapo RTSP camera endpoint.
 */
data class CameraDiscoveryInfo(
    val resolvedBackendUrl: String,
    val advertisedBackendUrl: String,
    val cameraIp: String? = null,
    val cameraPort: Int = 554,
    val cameraReachable: Boolean = false,
    val hasBackendCredentials: Boolean = false,
    val discoverySource: String = "none"
)
