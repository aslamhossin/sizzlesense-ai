package com.sizzlesense.ai.data.remote

import com.sizzlesense.ai.data.remote.dto.camera.CameraDiscoveryResponseDto
import com.sizzlesense.ai.data.remote.dto.coaching.CognitiveCoachingResponseDto
import com.sizzlesense.ai.data.remote.dto.eval.EvalSuiteReportDto
import com.sizzlesense.ai.data.remote.dto.request.SessionAnalyzeRequestDto
import com.sizzlesense.ai.data.remote.dto.request.TapoLiveAnalyzeRequestDto
import com.sizzlesense.ai.data.remote.dto.session.DeleteLearnerHistoryResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Declarative Retrofit API contract for the SizzleSense AI FastAPI backend (`/api/v1/`).
 *
 * Uses `converter-kotlinx-serialization` for compile-time `@Serializable` DTO marshalling
 * and supports per-call LAN/emulator base URL failover via [HEADER_TARGET_BASE_URL].
 */
interface SizzleSenseApiService {

    @GET("api/v1/camera/discover")
    suspend fun discoverCameraAndServer(
        @Header(HEADER_TARGET_BASE_URL) targetBaseUrl: String,
        @Query("preferred_ip") preferredIp: String? = null
    ): CameraDiscoveryResponseDto

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

    @DELETE("api/v1/learners/{learner_id}/history")
    suspend fun deleteLearnerHistory(
        @Header(HEADER_TARGET_BASE_URL) targetBaseUrl: String,
        @Path("learner_id") learnerId: String
    ): DeleteLearnerHistoryResponseDto

    companion object {
        const val HEADER_TARGET_BASE_URL = "X-Sizzle-Target-Base-Url"
        const val DEFAULT_BASE_URL = "http://10.0.2.2:8000/"
    }
}
