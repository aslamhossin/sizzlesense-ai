package com.sizzlesense.ai.data.remote

import com.sizzlesense.ai.data.remote.dto.coaching.CognitiveCoachingResponseDto
import com.sizzlesense.ai.data.remote.dto.eval.EvalSuiteReportDto
import com.sizzlesense.ai.data.remote.dto.request.SessionAnalyzeRequestDto
import com.sizzlesense.ai.data.remote.dto.request.TapoLiveAnalyzeRequestDto
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Declarative Retrofit API contract for the SizzleSense AI FastAPI backend (`/api/v1/`).
 *
 * Uses `converter-kotlinx-serialization` for compile-time `@Serializable` DTO marshalling
 * and supports per-call LAN/emulator base URL failover via [HEADER_TARGET_BASE_URL].
 */
interface SizzleSenseApiService {

    @POST("api/v1/sessions/analyze")
    suspend fun analyzeSession(
        @Header(HEADER_TARGET_BASE_URL) targetBaseUrl: String,
        @Body request: SessionAnalyzeRequestDto
    ): CognitiveCoachingResponseDto

    @POST("api/v1/camera/tapo-live-analyze")
    suspend fun captureFromTapoAndAnalyze(
        @Header(HEADER_TARGET_BASE_URL) targetBaseUrl: String,
        @Body request: TapoLiveAnalyzeRequestDto
    ): CognitiveCoachingResponseDto

    @POST("api/v1/evals/run")
    suspend fun runEvaluationBenchmark(
        @Header(HEADER_TARGET_BASE_URL) targetBaseUrl: String,
        @Body emptyBody: Map<String, String> = emptyMap()
    ): EvalSuiteReportDto

    companion object {
        const val HEADER_TARGET_BASE_URL = "X-Sizzle-Target-Base-Url"
        const val DEFAULT_BASE_URL = "http://192.168.11.6:8000/"
    }
}
