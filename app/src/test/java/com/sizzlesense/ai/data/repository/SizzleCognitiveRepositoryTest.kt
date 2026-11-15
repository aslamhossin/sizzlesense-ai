package com.sizzlesense.ai.data.repository

import com.sizzlesense.ai.BuildConfig
import com.sizzlesense.ai.core.result.AppError
import com.sizzlesense.ai.core.result.AppResult
import com.sizzlesense.ai.data.remote.DynamicBaseUrlInterceptor
import com.sizzlesense.ai.data.remote.FailoverJsonHttpClient
import com.sizzlesense.ai.data.remote.LanBackendScanner
import com.sizzlesense.ai.data.remote.dto.coaching.CognitiveCoachingResponseDto
import com.sizzlesense.ai.data.remote.dto.coaching.toDto
import com.sizzlesense.ai.di.module.NetworkModule
import com.sizzlesense.ai.domain.model.camera.TapoCameraConfig
import com.sizzlesense.ai.domain.model.session.SessionAnalyzeRequest
import com.sizzlesense.ai.testing.TestFixtures
import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SizzleCognitiveRepositoryTest {

    private val json = NetworkModule.provideJson()
    private val testDispatcher = UnconfinedTestDispatcher()

    private fun buildRepositoryWithInterceptor(
        lanBackendScanner: LanBackendScanner = LanBackendScanner { emptyList() },
        isPhysicalDevice: () -> Boolean = { false },
        interceptor: Interceptor
    ): SizzleCognitiveRepository {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(DynamicBaseUrlInterceptor())
            .addInterceptor(interceptor)
            .build()
        val apiService = NetworkModule.provideSizzleSenseApiService(
            json = json,
            okHttpClient = okHttpClient
        )
        val remoteClient = FailoverJsonHttpClient(
            apiService = apiService,
            ioDispatcher = testDispatcher,
            lanBackendScanner = lanBackendScanner,
            isPhysicalDevice = isPhysicalDevice
        )
        return SizzleCognitiveRepository(remoteClient = remoteClient)
    }

    @Test
    fun analyzeSession_returnsNetworkUnavailableWithoutFallbackWhenBackendUnreachable() {
        runTest {
            val repository = buildRepositoryWithInterceptor {
                throw IOException("Connection refused")
            }

            val request = SessionAnalyzeRequest(
                learnerId = "learner_1",
                dishId = "sauteed_onions",
                attemptNumber = 1,
                learnerReflection = "Why are my onions pale?",
                archetypeHint = "moisture_blindness"
            )

            val result = repository.analyzeSession(
                baseUrl = "http://192.168.11.12:8000",
                request = request
            )

            assertTrue(result is AppResult.Failure)
            val error = (result as AppResult.Failure).error
            assertTrue(error is AppError.NetworkUnavailable)
        }
    }

    @Test
    fun analyzeSession_abortsFailoverOnHttpErrorAndExtractsFastApiDetail() {
        runTest {
            val visitedHosts = mutableListOf<String>()
            val repository = buildRepositoryWithInterceptor { chain ->
                val req = chain.request()
                visitedHosts.add(req.url.host)
                Response.Builder()
                    .request(req)
                    .protocol(Protocol.HTTP_1_1)
                    .code(422)
                    .message("Unprocessable Entity")
                    .body(
                        """{"detail":"No pan or active cooking surface detected in frame."}"""
                            .toResponseBody("application/json".toMediaType())
                    )
                    .build()
            }

            val request = SessionAnalyzeRequest(
                learnerId = "learner_1",
                dishId = "sauteed_onions",
                attemptNumber = 1,
                learnerReflection = "Testing HTTP 422"
            )

            val result = repository.analyzeSession(
                baseUrl = "http://192.168.11.99:8000",
                request = request
            )

            assertEquals(1, visitedHosts.size)
            assertTrue(result is AppResult.Failure)
            val error = (result as AppResult.Failure).error
            assertTrue(error is AppError.HttpError)
            val httpError = error as AppError.HttpError
            assertEquals(422, httpError.statusCode)
            assertEquals("No pan or active cooking surface detected in frame.", httpError.readableMessage)
        }
    }

    @Test
    fun analyzeSession_preservesTimeoutWithoutFailoverOnSocketReadTimeout() {
        runTest {
            val visitedHosts = mutableListOf<String>()
            val repository = buildRepositoryWithInterceptor { chain ->
                visitedHosts.add(chain.request().url.host)
                throw SocketTimeoutException("timeout")
            }

            val result = repository.analyzeSession(
                baseUrl = "http://192.168.11.99:8000",
                request = SessionAnalyzeRequest(
                    learnerId = "learner_1",
                    dishId = "sauteed_onions",
                    attemptNumber = 1,
                    learnerReflection = "Testing read timeout"
                )
            )

            assertEquals(1, visitedHosts.size)
            assertTrue(result is AppResult.Failure)
            assertTrue((result as AppResult.Failure).error is AppError.Timeout)
        }
    }

    @Test
    fun analyzeSession_failsOverAcrossCandidatesOnSocketConnectTimeout() {
        runTest {
            val visitedHosts = mutableListOf<String>()
            val responseJson = json.encodeToString(
                CognitiveCoachingResponseDto.serializer(),
                TestFixtures.buildCoachingResponse(cvMetrics = null).toDto()
            )
            val repository = buildRepositoryWithInterceptor { chain ->
                val req = chain.request()
                visitedHosts.add(req.url.host)
                if (req.url.host == "192.168.11.99") {
                    throw SocketTimeoutException("failed to connect to /192.168.11.99 (port 8000)")
                }
                Response.Builder()
                    .request(req)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(responseJson.toResponseBody("application/json".toMediaType()))
                    .build()
            }

            val result = repository.analyzeSession(
                baseUrl = "http://192.168.11.99:8000",
                request = SessionAnalyzeRequest(
                    learnerId = "learner_1",
                    dishId = "sauteed_onions",
                    attemptNumber = 1,
                    learnerReflection = "Testing connect failover"
                )
            )

            assertTrue(result is AppResult.Success)
            assertEquals(listOf("192.168.11.99", "10.0.2.2"), visitedHosts)
            assertNull((result as AppResult.Success).data.stage1PhysicalObservation.cvMetrics)
        }
    }

    @Test
    fun analyzeSession_doesNotSwallowSerializationErrorsIntoSilentFallback() {
        runTest {
            val repository = buildRepositoryWithInterceptor { chain ->
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(
                        """{"malformed_schema": true}"""
                            .toResponseBody("application/json".toMediaType())
                    )
                    .build()
            }

            val request = SessionAnalyzeRequest(
                learnerId = "learner_1",
                dishId = "sauteed_onions",
                attemptNumber = 1,
                learnerReflection = "Testing malformed JSON"
            )

            val result = repository.analyzeSession(
                baseUrl = "http://192.168.11.12:8000",
                request = request
            )

            assertTrue(result is AppResult.Failure)
            val error = (result as AppResult.Failure).error
            assertTrue(error is AppError.SerializationError)
        }
    }

    @Test
    fun captureFromBackendTapoAndAnalyze_propagatesNetworkFailureWithoutSilentFallback() {
        runTest {
            val repository = buildRepositoryWithInterceptor {
                throw IOException("RTSP camera unreachable")
            }

            val result = repository.captureFromBackendTapoAndAnalyze(
                baseUrl = "http://192.168.11.12:8000",
                tapoConfig = TapoCameraConfig(ipAddress = "192.168.11.16"),
                attemptNumber = 2,
                reflectionText = "Checking live camera",
                audioWavBase64 = "UklGRg==",
                archetypeHint = "heat_anxiety"
            )

            assertTrue(result is AppResult.Failure)
            val error = (result as AppResult.Failure).error
            assertTrue(error is AppError.NetworkUnavailable)
        }
    }

    @Test
    fun captureFromBackendTapoAndAnalyze_forwardsAudioWavAndArchetypeHintInRequestJson() {
        runTest {
            var requestBodyJson = ""
            val responseJson = json.encodeToString(
                CognitiveCoachingResponseDto.serializer(),
                TestFixtures.buildCoachingResponse(attemptNumber = 2, cvMetrics = null).toDto()
            )
            val repository = buildRepositoryWithInterceptor { chain ->
                val req = chain.request()
                val buffer = Buffer()
                req.body?.writeTo(buffer)
                requestBodyJson = buffer.readUtf8()
                Response.Builder()
                    .request(req)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(responseJson.toResponseBody("application/json".toMediaType()))
                    .build()
            }

            val result = repository.captureFromBackendTapoAndAnalyze(
                baseUrl = "http://192.168.11.12:8000",
                tapoConfig = TapoCameraConfig(ipAddress = "192.168.11.16"),
                attemptNumber = 2,
                reflectionText = "Checking live camera",
                audioWavBase64 = "UklGRg==",
                archetypeHint = "heat_anxiety"
            )

            assertTrue(result is AppResult.Success)
            assertTrue(requestBodyJson.contains(""""audio_wav_base64":"UklGRg==""""))
            assertTrue(requestBodyJson.contains(""""archetype_hint":"heat_anxiety""""))
            assertNull((result as AppResult.Success).data.stage1PhysicalObservation.cvMetrics)
        }
    }

    @Test
    fun runEvaluationBenchmark_returnsNetworkUnavailableAndHttpErrorsWithoutFallback() {
        runTest {
            val offlineRepo = buildRepositoryWithInterceptor {
                throw IOException("Offline")
            }
            val offlineResult = offlineRepo.runEvaluationBenchmark("http://192.168.11.12:8000")
            assertTrue(offlineResult is AppResult.Failure)
            assertTrue((offlineResult as AppResult.Failure).error is AppError.NetworkUnavailable)

            val httpErrorRepo = buildRepositoryWithInterceptor { chain ->
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(503)
                    .message("Service Unavailable")
                    .body("busy".toResponseBody("text/plain".toMediaType()))
                    .build()
            }
            val errorResult = httpErrorRepo.runEvaluationBenchmark("http://192.168.11.12:8000")
            assertTrue(errorResult is AppResult.Failure)
            assertTrue((errorResult as AppResult.Failure).error is AppError.HttpError)
        }
    }

    @Test
    fun clearLearnerHistory_sendsDeleteRequestToLearnerHistoryEndpoint() {
        runTest {
            var methodSeen = ""
            var pathSeen = ""
            val repository = buildRepositoryWithInterceptor { chain ->
                val req = chain.request()
                methodSeen = req.method
                pathSeen = req.url.encodedPath
                Response.Builder()
                    .request(req)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(
                        """{"learner_id":"android_heat_anxiety","deleted_sessions":2}"""
                            .toResponseBody("application/json".toMediaType())
                    )
                    .build()
            }

            val result = repository.clearLearnerHistory(
                baseUrl = "http://192.168.11.12:8000",
                learnerId = "android_heat_anxiety"
            )

            assertTrue(result is AppResult.Success)
            assertEquals("DELETE", methodSeen)
            assertEquals("/api/v1/learners/android_heat_anxiety/history", pathSeen)
        }
    }

    @Test
    fun dynamicBaseUrlInterceptor_rewritesTargetHostAndStripsInternalHeader() {
        runTest {
            val visitedHosts = mutableListOf<String>()
            var leakedInternalHeader: String? = null

            val repository = buildRepositoryWithInterceptor { chain ->
                val req = chain.request()
                visitedHosts.add("${req.url.host}:${req.url.port}${req.url.encodedPath}")
                leakedInternalHeader = req.header("X-Sizzle-Target-Base-Url")
                throw IOException("Simulated timeout")
            }

            repository.runEvaluationBenchmark("http://192.168.11.99:8000")

            val expectedWorkstationHost = BuildConfig.DEFAULT_LAN_BACKEND_URL
                .trim()
                .trimEnd('/')
                .ifBlank { FailoverJsonHttpClient.MAC_WORKSTATION_LAN_URL }
                .removePrefix("http://")

            assertNull(leakedInternalHeader)
            assertEquals(
                listOf(
                    "192.168.11.99:8000/api/v1/evals/run",
                    "10.0.2.2:8000/api/v1/evals/run",
                    "$expectedWorkstationHost/api/v1/evals/run",
                    "127.0.0.1:8000/api/v1/evals/run"
                ),
                visitedHosts
            )
        }
    }

    @Test
    fun discoverCameraAndServer_resolvesWorkingFailoverUrlAndMapsCameraIp() {
        runTest {
            val repository = buildRepositoryWithInterceptor { chain ->
                val req = chain.request()
                if (req.url.host == "192.168.11.99") {
                    throw IOException("Old server IP offline")
                }
                assertEquals("192.168.11.16", req.url.queryParameter("preferred_ip"))
                Response.Builder()
                    .request(req)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(
                        """
                        {
                          "backend_url": "http://192.168.11.7:8000",
                          "camera_ip": "192.168.11.14",
                          "camera_port": 554,
                          "camera_reachable": true,
                          "has_backend_credentials": true,
                          "discovery_source": "lan_scan"
                        }
                        """.trimIndent().toResponseBody("application/json".toMediaType())
                    )
                    .build()
            }

            val result = repository.discoverCameraAndServer(
                baseUrl = "http://192.168.11.99:8000",
                preferredCameraIp = "192.168.11.16"
            )

            assertTrue(result is AppResult.Success)
            val info = (result as AppResult.Success).data
            assertEquals("http://10.0.2.2:8000", info.resolvedBackendUrl)
            assertEquals("http://192.168.11.7:8000", info.advertisedBackendUrl)
            assertEquals("192.168.11.14", info.cameraIp)
            assertTrue(info.cameraReachable)
            assertEquals("lan_scan", info.discoverySource)
        }
    }

    @Test
    fun physicalWirelessDevice_skipsEmulatorHostAndDiscoversLanBackendOnSubnetScan() {
        runTest {
            val visitedHosts = mutableListOf<String>()
            val repository = buildRepositoryWithInterceptor(
                lanBackendScanner = LanBackendScanner { listOf("http://192.168.11.42:8000") },
                isPhysicalDevice = { true }
            ) { chain ->
                val req = chain.request()
                visitedHosts.add(req.url.host)
                if (req.url.host != "192.168.11.42") {
                    throw IOException("Cached host unreachable")
                }
                Response.Builder()
                    .request(req)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(
                        """
                        {
                          "backend_url": "http://192.168.11.42:8000",
                          "camera_ip": "192.168.11.14",
                          "camera_port": 554,
                          "camera_reachable": true,
                          "has_backend_credentials": true,
                          "discovery_source": "lan_scan"
                        }
                        """.trimIndent().toResponseBody("application/json".toMediaType())
                    )
                    .build()
            }

            val result = repository.discoverCameraAndServer(
                baseUrl = "http://10.0.2.2:8000",
                preferredCameraIp = "192.168.11.16"
            )

            assertTrue(result is AppResult.Success)
            assertFalse(visitedHosts.contains("10.0.2.2"))
            assertEquals("192.168.11.42", visitedHosts.last())
            val info = (result as AppResult.Success).data
            assertEquals("http://192.168.11.42:8000", info.resolvedBackendUrl)
            assertEquals("192.168.11.14", info.cameraIp)
        }
    }
}
