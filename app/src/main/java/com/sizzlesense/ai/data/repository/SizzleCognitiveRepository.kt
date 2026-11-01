package com.sizzlesense.ai.data.repository

import com.sizzlesense.ai.core.result.AppError
import com.sizzlesense.ai.core.result.AppResult
import com.sizzlesense.ai.core.result.map
import com.sizzlesense.ai.data.local.LocalFallbackCognitiveDataSource
import com.sizzlesense.ai.data.remote.FailoverJsonHttpClient
import com.sizzlesense.ai.data.remote.dto.coaching.CognitiveCoachingResponseDto
import com.sizzlesense.ai.data.remote.dto.coaching.toDomain
import com.sizzlesense.ai.data.remote.dto.eval.EvalSuiteReportDto
import com.sizzlesense.ai.data.remote.dto.eval.toDomain
import com.sizzlesense.ai.data.remote.dto.request.SessionAnalyzeRequestDto
import com.sizzlesense.ai.data.remote.dto.request.TapoLiveAnalyzeRequestDto
import com.sizzlesense.ai.data.remote.dto.request.toDto
import com.sizzlesense.ai.data.remote.dto.request.toLiveAnalyzeRequestDto
import com.sizzlesense.ai.domain.model.camera.TapoCameraConfig
import com.sizzlesense.ai.domain.model.coaching.CognitiveCoachingResponse
import com.sizzlesense.ai.domain.model.eval.EvalSuiteReport
import com.sizzlesense.ai.domain.model.session.SessionAnalyzeRequest
import com.sizzlesense.ai.domain.repository.EvaluationBenchmarkGateway
import com.sizzlesense.ai.domain.repository.LiveTapoCameraGateway
import com.sizzlesense.ai.domain.repository.SessionAnalysisGateway
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SizzleCognitiveRepository @Inject constructor(
    private val remoteClient: FailoverJsonHttpClient,
    private val localFallback: LocalFallbackCognitiveDataSource
) : SessionAnalysisGateway, LiveTapoCameraGateway, EvaluationBenchmarkGateway {

    override suspend fun analyzeSession(
        baseUrl: String,
        request: SessionAnalyzeRequest
    ): AppResult<CognitiveCoachingResponse> {
        val result = remoteClient.postTypedJsonWithFailover(
            primaryBaseUrl = baseUrl,
            endpointPath = "/api/v1/sessions/analyze",
            requestBody = request.toDto(),
            requestSerializer = SessionAnalyzeRequestDto.serializer(),
            responseSerializer = CognitiveCoachingResponseDto.serializer()
        ).map { dto ->
            dto.toDomain()
        }

        return when (result) {
            is AppResult.Success -> result
            is AppResult.Failure -> {
                if (result.error is AppError.NetworkUnavailable) {
                    AppResult.Success(localFallback.buildFallbackCoachingResponse(request))
                } else {
                    result
                }
            }
        }
    }

    override suspend fun captureFromBackendTapoAndAnalyze(
        baseUrl: String,
        tapoConfig: TapoCameraConfig,
        attemptNumber: Int,
        reflectionText: String
    ): AppResult<CognitiveCoachingResponse> {
        val requestDto = tapoConfig.toLiveAnalyzeRequestDto(
            attemptNumber = attemptNumber,
            reflectionText = reflectionText
        )
        return remoteClient.postTypedJsonWithFailover(
            primaryBaseUrl = baseUrl,
            endpointPath = "/api/v1/camera/tapo-live-analyze",
            requestBody = requestDto,
            requestSerializer = TapoLiveAnalyzeRequestDto.serializer(),
            responseSerializer = CognitiveCoachingResponseDto.serializer()
        ).map { dto ->
            dto.toDomain()
        }
    }

    override suspend fun runEvaluationBenchmark(baseUrl: String): AppResult<EvalSuiteReport> {
        val result = remoteClient.postJsonWithFailover(
            primaryBaseUrl = baseUrl,
            endpointPath = "/api/v1/evals/run",
            jsonBody = "{}",
            responseSerializer = EvalSuiteReportDto.serializer()
        ).map { dto ->
            dto.toDomain()
        }

        return when (result) {
            is AppResult.Success -> result
            is AppResult.Failure -> {
                if (result.error is AppError.NetworkUnavailable) {
                    AppResult.Success(localFallback.buildFallbackEvalReport())
                } else {
                    result
                }
            }
        }
    }
}
