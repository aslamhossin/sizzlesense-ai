package com.sizzlesense.ai.data.repository

import com.sizzlesense.ai.core.result.AppError
import com.sizzlesense.ai.core.result.AppResult
import com.sizzlesense.ai.core.result.map
import com.sizzlesense.ai.data.local.LocalFallbackCognitiveDataSource
import com.sizzlesense.ai.data.remote.FailoverJsonHttpClient
import com.sizzlesense.ai.data.remote.dto.coaching.toDomain
import com.sizzlesense.ai.data.remote.dto.eval.toDomain
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
        val result = remoteClient.analyzeSessionWithFailover(
            primaryBaseUrl = baseUrl,
            request = request.toDto()
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
        return remoteClient.captureFromTapoAndAnalyzeWithFailover(
            primaryBaseUrl = baseUrl,
            request = requestDto
        ).map { dto ->
            dto.toDomain()
        }
    }

    override suspend fun runEvaluationBenchmark(baseUrl: String): AppResult<EvalSuiteReport> {
        val result = remoteClient.runEvaluationBenchmarkWithFailover(
            primaryBaseUrl = baseUrl
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
