package com.sizzlesense.ai.domain.usecase

import com.sizzlesense.ai.core.network.safeApiCall
import com.sizzlesense.ai.core.result.AppError
import com.sizzlesense.ai.core.result.AppResult
import com.sizzlesense.ai.data.local.LocalFallbackCognitiveDataSource
import com.sizzlesense.ai.data.remote.FailoverJsonHttpClient
import com.sizzlesense.ai.data.remote.dto.request.toLiveAnalyzeRequestDto
import com.sizzlesense.ai.di.module.NetworkModule
import com.sizzlesense.ai.domain.model.camera.TapoCameraConfig
import com.sizzlesense.ai.domain.model.session.SessionAnalyzeRequest
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CognitiveUseCasesTest {

    private val fallbackDataSource = LocalFallbackCognitiveDataSource()

    @Test
    fun analyzeCookingSessionUseCase_normalizesAttemptBoundsAndTrimsReflection() {
        runTest {
            var capturedRequest: SessionAnalyzeRequest? = null
            val useCase = AnalyzeCookingSessionUseCase { _, request ->
                capturedRequest = request
                AppResult.Success(fallbackDataSource.buildFallbackCoachingResponse(request))
            }

            val result = useCase(
                baseUrl = "http://192.168.11.12:8000",
                archetypeId = "moisture_blindness",
                attemptNumber = 99,
                learnerReflection = "   I waited for the sizzle sound!   ",
                capturedFrameBase64 = "   "
            )

            assertEquals(3, capturedRequest?.attemptNumber)
            assertEquals("I waited for the sizzle sound!", capturedRequest?.learnerReflection)
            assertTrue(capturedRequest?.keyframeBase64List?.isEmpty() == true)
            assertTrue(result is AppResult.Success)
            val response = (result as AppResult.Success).data
            assertEquals("android_moisture_blindness", response.learnerId)
        }
    }

    @Test
    fun captureLiveTapoSessionUseCase_returnsValidationErrorOnBlankCameraIp() {
        runTest {
            val useCase = CaptureLiveTapoSessionUseCase { _, _, _, _ ->
                error("Should not be called when IP is blank")
            }

            val result = useCase(
                baseUrl = "http://192.168.11.12:8000",
                tapoConfig = TapoCameraConfig(ipAddress = "   "),
                attemptNumber = 1,
                reflectionText = "Testing live camera"
            )

            assertTrue(result is AppResult.Failure)
            val error = (result as AppResult.Failure).error
            assertTrue(error is AppError.ValidationError)
        }
    }

    @Test
    fun tapoLiveAnalyzeRequestDto_mapsDomainConfigWithTrimmedFields() {
        val config = TapoCameraConfig(
            ipAddress = " 192.168.11.16 ",
            username = " chef ",
            password = "secret"
        )
        val dto = config.toLiveAnalyzeRequestDto(
            attemptNumber = 2,
            reflectionText = "  Golden edges observed  "
        )
        assertEquals("192.168.11.16", dto.ipAddress)
        assertEquals("chef", dto.username)
        assertEquals("secret", dto.password)
        assertEquals(2, dto.attemptNumber)
        assertEquals("Golden edges observed", dto.learnerReflection)
    }

    @Test
    fun safeApiCall_mapsIoAndSerializationExceptionsToStructuredAppErrors() {
        runTest {
            val dispatcher = UnconfinedTestDispatcher()
            val ioFailure = safeApiCall<String>(dispatcher) {
                throw IOException("Socket closed")
            }
            assertTrue(
                ioFailure is AppResult.Failure &&
                    ioFailure.error is AppError.NetworkUnavailable
            )

            val jsonFailure = safeApiCall<String>(dispatcher) {
                throw SerializationException("Malformed JSON")
            }
            assertTrue(
                jsonFailure is AppResult.Failure &&
                    jsonFailure.error is AppError.SerializationError
            )
        }
    }

    @Test
    fun failoverJsonHttpClient_resolvesDeduplicatedCandidateEndpointsInPriorityOrder() {
        val client = FailoverJsonHttpClient(
            apiService = NetworkModule.provideSizzleSenseApiService(
                json = NetworkModule.provideJson(),
                okHttpClient = NetworkModule.provideOkHttpClient()
            ),
            ioDispatcher = UnconfinedTestDispatcher()
        )
        val endpoints = client.resolveCandidateEndpoints("http://192.168.11.6:8000/")

        assertEquals(
            listOf(
                "http://192.168.11.6:8000",
                "http://10.0.2.2:8000",
                "http://127.0.0.1:8000"
            ),
            endpoints
        )
    }
}
