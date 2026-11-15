package com.sizzlesense.ai.domain.usecase

import com.sizzlesense.ai.BuildConfig
import com.sizzlesense.ai.core.network.safeApiCall
import com.sizzlesense.ai.core.result.AppError
import com.sizzlesense.ai.core.result.AppResult
import com.sizzlesense.ai.data.remote.FailoverJsonHttpClient
import com.sizzlesense.ai.data.remote.dto.request.toLiveAnalyzeRequestDto
import com.sizzlesense.ai.di.module.NetworkModule
import com.sizzlesense.ai.domain.model.camera.TapoCameraConfig
import com.sizzlesense.ai.domain.model.session.SessionAnalyzeRequest
import com.sizzlesense.ai.testing.TestFixtures
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

    @Test
    fun analyzeCookingSessionUseCase_normalizesAttemptBoundsAndTrimsReflection() {
        runTest {
            var capturedRequest: SessionAnalyzeRequest? = null
            val useCase = AnalyzeCookingSessionUseCase { _, request ->
                capturedRequest = request
                AppResult.Success(TestFixtures.buildCoachingResponse(request))
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
            val useCase = CaptureLiveTapoSessionUseCase { _, _, _, _, _, _ ->
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
    fun captureLiveTapoSessionUseCase_normalizesBoundsAndForwardsAudioAndArchetype() {
        runTest {
            var capturedAttempt = -1
            var capturedReflection = ""
            var capturedAudio: String? = null
            var capturedArchetype: String? = null
            val useCase = CaptureLiveTapoSessionUseCase { _, _, attempt, reflection, audio, archetype ->
                capturedAttempt = attempt
                capturedReflection = reflection
                capturedAudio = audio
                capturedArchetype = archetype
                AppResult.Success(TestFixtures.buildCoachingResponse(attemptNumber = attempt))
            }

            val result = useCase(
                baseUrl = "http://192.168.11.12:8000",
                tapoConfig = TapoCameraConfig(ipAddress = " 192.168.11.16 "),
                attemptNumber = 9,
                reflectionText = "  Heard sharp sizzle  ",
                audioWavBase64 = "UklGRg==",
                archetypeHint = "heat_anxiety"
            )

            assertTrue(result is AppResult.Success)
            assertEquals(3, capturedAttempt)
            assertEquals("Heard sharp sizzle", capturedReflection)
            assertEquals("UklGRg==", capturedAudio)
            assertEquals("heat_anxiety", capturedArchetype)
        }
    }

    @Test
    fun clearLearnerHistoryUseCase_trimsLearnerIdAndRejectsBlankId() {
        runTest {
            var deletedLearnerId: String? = null
            val useCase = ClearLearnerHistoryUseCase { _, learnerId ->
                deletedLearnerId = learnerId
                AppResult.Success(Unit)
            }

            val invalidResult = useCase("http://192.168.11.12:8000", "   ")
            assertTrue(invalidResult is AppResult.Failure)
            assertTrue((invalidResult as AppResult.Failure).error is AppError.ValidationError)

            val validResult = useCase("http://192.168.11.12:8000", " android_heat_anxiety ")
            assertTrue(validResult is AppResult.Success)
            assertEquals("android_heat_anxiety", deletedLearnerId)
        }
    }

    @Test
    fun appErrorHttpError_extractsFastApiStringAndValidationArrayDetails() {
        val stringDetail = AppError.HttpError(
            statusCode = 422,
            responseBody = """{"detail":"No pan or active cooking surface detected in frame."}"""
        )
        assertEquals(
            "No pan or active cooking surface detected in frame.",
            stringDetail.readableMessage
        )

        val listDetail = AppError.HttpError(
            statusCode = 422,
            responseBody = """{"detail":[{"loc":["body","audio_wav_base64"],"msg":"Invalid WAV header"}]}"""
        )
        assertEquals("Invalid WAV header", listDetail.readableMessage)

        val plainBody = AppError.HttpError(
            statusCode = 502,
            responseBody = "Bad Gateway"
        )
        assertEquals("Backend returned HTTP 502: Bad Gateway", plainBody.readableMessage)
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
            reflectionText = "  Golden edges observed  ",
            audioWavBase64 = "UklGRg==",
            archetypeHint = "perception_blindspot"
        )
        assertEquals("192.168.11.16", dto.ipAddress)
        assertEquals("chef", dto.username)
        assertEquals("secret", dto.password)
        assertEquals("android_perception_blindspot", dto.learnerId)
        assertEquals(2, dto.attemptNumber)
        assertEquals("Golden edges observed", dto.learnerReflection)
        assertEquals("UklGRg==", dto.audioWavBase64)
        assertEquals("perception_blindspot", dto.archetypeHint)

        val defaultLearnerDto = config.toLiveAnalyzeRequestDto(
            attemptNumber = 1,
            reflectionText = "Baseline"
        )
        assertEquals(
            com.sizzlesense.ai.data.remote.dto.request.TapoLiveAnalyzeRequestDto.DEFAULT_TAPO_LEARNER_ID,
            defaultLearnerDto.learnerId
        )
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
        val lanCandidate = BuildConfig.DEFAULT_LAN_BACKEND_URL
            .trim()
            .trimEnd('/')
            .ifBlank { FailoverJsonHttpClient.MAC_WORKSTATION_LAN_URL }
        val endpoints = client.resolveCandidateEndpoints("$lanCandidate/")

        assertEquals(
            listOf(
                lanCandidate,
                FailoverJsonHttpClient.EMULATOR_HOST_URL,
                FailoverJsonHttpClient.LOCALHOST_URL
            ),
            endpoints
        )
    }
}
