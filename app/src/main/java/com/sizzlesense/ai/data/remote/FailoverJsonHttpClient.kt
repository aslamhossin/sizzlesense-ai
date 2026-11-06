package com.sizzlesense.ai.data.remote

import com.sizzlesense.ai.core.network.safeApiCall
import com.sizzlesense.ai.core.result.AppError
import com.sizzlesense.ai.core.result.AppException
import com.sizzlesense.ai.core.result.AppResult
import com.sizzlesense.ai.data.remote.dto.coaching.CognitiveCoachingResponseDto
import com.sizzlesense.ai.data.remote.dto.eval.EvalSuiteReportDto
import com.sizzlesense.ai.data.remote.dto.request.SessionAnalyzeRequestDto
import com.sizzlesense.ai.data.remote.dto.request.TapoLiveAnalyzeRequestDto
import com.sizzlesense.ai.di.qualifier.IoDispatcher
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

/**
 * Executes declarative Retrofit [SizzleSenseApiService] calls across candidate LAN/emulator endpoints,
 * preserving structured [AppError] classification (`HttpError`, `SerializationError`, `NetworkUnavailable`).
 */
@Singleton
class FailoverJsonHttpClient @Inject constructor(
    private val apiService: SizzleSenseApiService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    fun resolveCandidateEndpoints(primaryBaseUrl: String): List<String> {
        return listOf(
            primaryBaseUrl.trim().trimEnd('/'),
            EMULATOR_HOST_URL,
            MAC_WORKSTATION_LAN_URL,
            LOCALHOST_URL
        ).filter { it.isNotBlank() }.distinct()
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

    suspend fun <RES : Any> executeWithFailover(
        primaryBaseUrl: String,
        call: suspend (targetBaseUrl: String) -> RES
    ): AppResult<RES> = safeApiCall(ioDispatcher) {
        val candidates = resolveCandidateEndpoints(primaryBaseUrl)
        var lastError: AppError = AppError.NetworkUnavailable("No candidate endpoints configured")

        for (rootUrl in candidates) {
            try {
                return@safeApiCall call(rootUrl)
            } catch (e: CancellationException) {
                throw e
            } catch (e: SerializationException) {
                throw e
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string().orEmpty()
                lastError = AppError.HttpError(
                    statusCode = e.code(),
                    responseBody = errorBody
                )
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
