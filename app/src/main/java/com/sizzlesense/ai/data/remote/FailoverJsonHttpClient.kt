package com.sizzlesense.ai.data.remote

import com.sizzlesense.ai.BuildConfig
import com.sizzlesense.ai.core.network.safeApiCall
import com.sizzlesense.ai.core.result.AppError
import com.sizzlesense.ai.core.result.AppException
import com.sizzlesense.ai.core.result.AppResult
import com.sizzlesense.ai.core.result.map
import com.sizzlesense.ai.data.remote.dto.camera.CameraDiscoveryResponseDto
import com.sizzlesense.ai.data.remote.dto.coaching.CognitiveCoachingResponseDto
import com.sizzlesense.ai.data.remote.dto.eval.EvalSuiteReportDto
import com.sizzlesense.ai.data.remote.dto.request.SessionAnalyzeRequestDto
import com.sizzlesense.ai.data.remote.dto.request.TapoLiveAnalyzeRequestDto
import com.sizzlesense.ai.data.remote.dto.session.DeleteLearnerHistoryResponseDto
import com.sizzlesense.ai.di.qualifier.IoDispatcher
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

data class ResolvedRemoteResult<out T>(
    val data: T,
    val resolvedBaseUrl: String
)

/**
 * Executes declarative Retrofit [SizzleSenseApiService] calls across candidate LAN/emulator endpoints,
 * preserving structured [AppError] classification (`HttpError`, `SerializationError`, `NetworkUnavailable`).
 */
@Singleton
class FailoverJsonHttpClient internal constructor(
    private val apiService: SizzleSenseApiService,
    private val ioDispatcher: CoroutineDispatcher,
    private val lanBackendScanner: LanBackendScanner,
    private val isPhysicalDevice: () -> Boolean
) {

    @Inject
    constructor(
        apiService: SizzleSenseApiService,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ) : this(
        apiService = apiService,
        ioDispatcher = ioDispatcher,
        lanBackendScanner = SocketLanBackendScanner(ioDispatcher),
        isPhysicalDevice = { AndroidDeviceProfile.isPhysicalDevice() }
    )

    fun resolveCandidateEndpoints(primaryBaseUrl: String): List<String> {
        val workstationLanUrl = BuildConfig.DEFAULT_LAN_BACKEND_URL
            .trim()
            .trimEnd('/')
            .ifBlank { MAC_WORKSTATION_LAN_URL }
        val normalizedPrimary = primaryBaseUrl.trim().trimEnd('/')
        val ordered = if (isPhysicalDevice()) {
            // Avoid wasting socket connect timeouts on 10.0.2.2 when running on a physical device.
            listOf(
                normalizedPrimary.takeUnless { it == EMULATOR_HOST_URL }.orEmpty(),
                workstationLanUrl,
                LOCALHOST_URL
            )
        } else {
            listOf(
                normalizedPrimary,
                EMULATOR_HOST_URL,
                workstationLanUrl,
                LOCALHOST_URL
            )
        }
        return ordered.filter { it.isNotBlank() }.distinct()
    }

    suspend fun discoverCameraAndServerWithFailover(
        primaryBaseUrl: String,
        preferredIp: String
    ): AppResult<ResolvedRemoteResult<CameraDiscoveryResponseDto>> {
        val queryIp = preferredIp.trim().ifBlank { null }
        val call: suspend (String) -> CameraDiscoveryResponseDto = { targetUrl ->
            apiService.discoverCameraAndServer(
                targetBaseUrl = targetUrl,
                preferredIp = queryIp
            )
        }
        val initialCandidates = resolveCandidateEndpoints(primaryBaseUrl)
        val initialResult = executeWithCandidates(initialCandidates, call)
        if (initialResult is AppResult.Success) return initialResult
        val failure = (initialResult as AppResult.Failure).error
        if (failure !is AppError.NetworkUnavailable) return initialResult

        val triedSet = initialCandidates.toSet()
        val lanCandidates = lanBackendScanner
            .scanPort8000OnLocalSubnet()
            .filterNot { it in triedSet }
        if (lanCandidates.isEmpty()) return initialResult

        return executeWithCandidates(lanCandidates, call)
    }

    suspend fun analyzeSessionWithFailover(
        primaryBaseUrl: String,
        request: SessionAnalyzeRequestDto
    ): AppResult<CognitiveCoachingResponseDto> = executeWithFailover(primaryBaseUrl) { targetUrl ->
        apiService.analyzeSession(
            targetBaseUrl = targetUrl,
            request = request
        )
    }

    suspend fun captureFromTapoAndAnalyzeWithFailover(
        primaryBaseUrl: String,
        request: TapoLiveAnalyzeRequestDto
    ): AppResult<CognitiveCoachingResponseDto> = executeWithFailover(primaryBaseUrl) { targetUrl ->
        apiService.captureFromTapoAndAnalyze(
            targetBaseUrl = targetUrl,
            request = request
        )
    }

    suspend fun runEvaluationBenchmarkWithFailover(
        primaryBaseUrl: String
    ): AppResult<EvalSuiteReportDto> = executeWithFailover(primaryBaseUrl) { targetUrl ->
        apiService.runEvaluationBenchmark(targetBaseUrl = targetUrl)
    }

    suspend fun deleteLearnerHistoryWithFailover(
        primaryBaseUrl: String,
        learnerId: String
    ): AppResult<DeleteLearnerHistoryResponseDto> = executeWithFailover(primaryBaseUrl) { targetUrl ->
        apiService.deleteLearnerHistory(
            targetBaseUrl = targetUrl,
            learnerId = learnerId
        )
    }

    suspend fun <RES : Any> executeWithFailover(
        primaryBaseUrl: String,
        call: suspend (targetBaseUrl: String) -> RES
    ): AppResult<RES> = executeWithFailoverAndResolvedUrl(
        primaryBaseUrl = primaryBaseUrl,
        call = call
    ).map { it.data }

    suspend fun <RES : Any> executeWithFailoverAndResolvedUrl(
        primaryBaseUrl: String,
        call: suspend (targetBaseUrl: String) -> RES
    ): AppResult<ResolvedRemoteResult<RES>> =
        executeWithCandidates(resolveCandidateEndpoints(primaryBaseUrl), call)

    private suspend fun <RES : Any> executeWithCandidates(
        candidates: List<String>,
        call: suspend (targetBaseUrl: String) -> RES
    ): AppResult<ResolvedRemoteResult<RES>> = safeApiCall(ioDispatcher) {
        var lastError: AppError = AppError.NetworkUnavailable("No candidate endpoints configured")

        for (rootUrl in candidates) {
            try {
                val data = call(rootUrl)
                return@safeApiCall ResolvedRemoteResult(
                    data = data,
                    resolvedBaseUrl = rootUrl
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: SerializationException) {
                throw e
            } catch (e: HttpException) {
                // Server responded cleanly with an HTTP error; abort failover immediately so it is never overwritten.
                val errorBody = e.response()?.errorBody()?.string().orEmpty()
                throw AppException(
                    AppError.HttpError(
                        statusCode = e.code(),
                        responseBody = errorBody
                    )
                )
            } catch (e: SocketTimeoutException) {
                // Connect timeouts indicate an unreachable host (keep failing over); read timeouts mean the server is busy.
                if (e.message?.contains("connect", ignoreCase = true) == true) {
                    lastError = AppError.NetworkUnavailable(
                        e.message ?: "Endpoint $rootUrl unreachable"
                    )
                } else {
                    throw AppException(
                        AppError.Timeout(
                            e.message ?: "Request timed out while waiting for Gemini 3 reasoning."
                        )
                    )
                }
            } catch (e: Exception) {
                lastError = AppError.NetworkUnavailable(
                    e.message ?: "Endpoint $rootUrl unreachable"
                )
            }
        }
        throw AppException(lastError)
    }

    companion object {
        const val EMULATOR_HOST_URL = "http://10.0.2.2:8000"
        const val MAC_WORKSTATION_LAN_URL = "http://192.168.11.6:8000"
        const val LOCALHOST_URL = "http://127.0.0.1:8000"
    }
}
