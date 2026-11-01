package com.sizzlesense.ai.domain.model.camera

import org.junit.Assert.assertEquals
import org.junit.Test

class TapoRtspConfigTest {

    @Test
    fun buildRtspUrl_formatsDefault192_168_11_16MainAndSubstreamUrls() {
        val mainConfig = TapoCameraConfig(
            ipAddress = "192.168.11.16",
            username = "chef",
            password = "secret123",
            useSubStream = false
        )
        assertEquals(
            "rtsp://chef:secret123@192.168.11.16:554/stream1",
            mainConfig.buildRtspUrl()
        )

        val subConfig = mainConfig.copy(useSubStream = true)
        assertEquals(
            "rtsp://chef:secret123@192.168.11.16:554/stream2",
            subConfig.buildRtspUrl()
        )
    }

    @Test
    fun buildRtspUrl_safelyUrlEncodesSpecialCharactersInPassword() {
        val config = TapoCameraConfig(
            ipAddress = "192.168.11.16",
            username = "cook@home",
            password = "p@ss:w/rd!",
            useSubStream = false
        )
        assertEquals(
            "rtsp://cook%40home:p%40ss%3Aw%2Frd%21@192.168.11.16:554/stream1",
            config.buildRtspUrl()
        )
    }
}
