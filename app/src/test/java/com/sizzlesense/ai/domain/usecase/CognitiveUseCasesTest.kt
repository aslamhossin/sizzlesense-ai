package com.sizzlesense.ai.domain.usecase

import com.sizzlesense.ai.data.local.LocalFallbackCognitiveDataSource
import com.sizzlesense.ai.data.remote.FailoverJsonHttpClient
import com.sizzlesense.ai.di.NetworkModule
import com.sizzlesense.ai.domain.model.SessionAnalyzeRequest
import com.sizzlesense.ai.domain.model.TapoCameraConfig
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CognitiveUseCasesTest {

    private val fallbackDataSource = LocalFallbackCognitiveDataSource()

    @Test
    fun analyzeCookingSessionUseCase_normalizesAttemptBoundsAndTrimsReflection() {
        runBlocking {
            var capturedRequest: SessionAnalyzeRequest? = null
            val useCase = AnalyzeCookingSessionUseCase { _, request ->
                capturedRequest = request
                fallbackDataSource.buildFallbackCoachingResponse(request)
            }

            val response = useCase(
                baseUrl = "http://192.168.11.12:8000",
                archetypeId = "moisture_blindness",
                attemptNumber = 99,
                learnerReflection = "   I waited for the sizzle sound!   ",
                capturedFrameBase64 = "   "
            )

            assertEquals(3, capturedRequest?.attemptNumber)
            assertEquals("I waited for the sizzle sound!", capturedRequest?.learnerReflection)
            assertTrue(capturedRequest?.keyframeBase64List?.isEmpty() == true)
            assertEquals("android_moisture_blindness", response.learnerId)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun captureLiveTapoSessionUseCase_rejectsBlankCameraIp() {
        runBlocking {
            val useCase = CaptureLiveTapoSessionUseCase { _, _, _, _ ->
                error("Should not be called when IP is blank")
            }

            useCase(
                baseUrl = "http://192.168.11.12:8000",
                tapoConfig = TapoCameraConfig(ipAddress = "   "),
                attemptNumber = 1,
                reflectionText = "Testing live camera"
            )
        }
    }

    @Test
    fun failoverJsonHttpClient_resolvesDeduplicatedCandidateEndpointsInPriorityOrder() {
        val client = FailoverJsonHttpClient(
            json = NetworkModule.provideJson(),
            httpClient = NetworkModule.provideOkHttpClient()
        )
        val endpoints = client.resolveCandidateEndpoints("http://192.168.11.12:8000/")

        assertEquals(
            listOf(
                "http://192.168.11.12:8000",
                "http://10.0.2.2:8000",
                "http://127.0.0.1:8000"
            ),
            endpoints
        )
    }
}
