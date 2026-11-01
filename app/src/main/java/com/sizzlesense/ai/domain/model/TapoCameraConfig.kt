package com.sizzlesense.ai.domain.model

import java.net.URLEncoder

data class TapoCameraConfig(
    val ipAddress: String = "192.168.11.16",
    val username: String = "",
    val password: String = "",
    val useSubStream: Boolean = false,
    val port: Int = 554,
    val isConnected: Boolean = false
) {

    fun buildRtspUrl(): String {
        val cleanIp = ipAddress.trim()
        require(cleanIp.isNotEmpty()) { "Tapo camera IP address must not be empty." }

        val streamPath = if (useSubStream) "stream2" else "stream1"
        if (username.isBlank() && password.isBlank()) {
            return "rtsp://$cleanIp:$port/$streamPath"
        }

        val encUser = URLEncoder.encode(username.trim(), Charsets.UTF_8.name())
            .replace("+", "%20")
        val encPass = URLEncoder.encode(password, Charsets.UTF_8.name())
            .replace("+", "%20")
        return "rtsp://$encUser:$encPass@$cleanIp:$port/$streamPath"
    }
}
