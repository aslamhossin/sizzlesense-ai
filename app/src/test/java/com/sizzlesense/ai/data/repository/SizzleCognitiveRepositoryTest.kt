package com.sizzlesense.ai.data.repository

import com.sizzlesense.ai.core.result.AppError
import com.sizzlesense.ai.core.result.AppResult
import com.sizzlesense.ai.data.local.LocalFallbackCognitiveDataSource
import com.sizzlesense.ai.data.remote.DynamicBaseUrlInterceptor
import com.sizzlesense.ai.data.remote.FailoverJsonHttpClient
import com.sizzlesense.ai.di.module.NetworkModule
import com.sizzlesense.ai.domain.model.camera.TapoCameraConfig
import com.sizzlesense.ai.domain.model.session.SessionAnalyzeRequest
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SizzleCognitiveRepositoryTest {

    private val json = NetworkModule.provideJson()
    private val fallbackDataSource = LocalFallbackCognitiveDataSource()
    private val testDispatcher = UnconfinedTestDispatcher()

    private fun buildRepositoryWithInterceptor(interceptor: Interceptor): SizzleCognitiveRepository {
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
            ioDispatcher = testDispatcher
        )
        return SizzleCognitiveRepository(
            remoteClient = remoteClient,
            localFallback = fallbackDataSource
        )
    }

    @Test
    fun analyzeSession_fallsBackToOfflineDemoSimulationOnlyOnNetworkUnavailable() {
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

            assertTrue(result is AppResult.Success)
            val response = (result as AppResult.Success).data
            assertEquals(
                "Offline Demo Simulation (Backend Unreachable)",
                response.reasoningModelUsed
            )
            assertEquals("moisture_blindness", response.stage2LearnerState.learnerArchetypeId)
        }
    }

    @Test
    fun analyzeSession_doesNotSwallowHttp500ErrorsIntoSilentFallback() {
        runTest {
            val repository = buildRepositoryWithInterceptor { chain ->
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(500)
                    .message("Internal Server Error")
                    .body(
                        """{"detail":"Gemini API quota exceeded"}"""
                            .toResponseBody("application/json".toMediaType())
                    )
                    .build()
            }

            val request = SessionAnalyzeRequest(
                learnerId = "learner_1",
                dishId = "sauteed_onions",
                attemptNumber = 1,
                learnerReflection = "Testing HTTP 500"
            )

            val result = repository.analyzeSession(
                baseUrl = "http://192.168.11.12:8000",
                request = request
            )

            assertTrue(result is AppResult.Failure)
            val error = (result as AppResult.Failure).error
            assertTrue(error is AppError.HttpError)
            assertEquals(500, (error as AppError.HttpError).statusCode)
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
                reflectionText = "Checking live camera"
            )

            assertTrue(result is AppResult.Failure)
            val error = (result as AppResult.Failure).error
            assertTrue(error is AppError.NetworkUnavailable)
        }
    }

    @Test
    fun runEvaluationBenchmark_fallsBackOnNetworkUnavailableAndPropagatesHttpErrors() {
        runTest {
            val offlineRepo = buildRepositoryWithInterceptor {
                throw IOException("Offline")
            }
            val offlineResult = offlineRepo.runEvaluationBenchmark("http://192.168.11.12:8000")
            assertTrue(offlineResult is AppResult.Success)
            assertEquals(10, (offlineResult as AppResult.Success).data.totalSessionsEvaluated)

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

            assertNull(leakedInternalHeader)
            assertEquals(
                listOf(
                    "192.168.11.99:8000/api/v1/evals/run",
                    "10.0.2.2:8000/api/v1/evals/run",
                    "192.168.11.6:8000/api/v1/evals/run",
                    "127.0.0.1:8000/api/v1/evals/run"
                ),
                visitedHosts
            )
        }
    }
}
