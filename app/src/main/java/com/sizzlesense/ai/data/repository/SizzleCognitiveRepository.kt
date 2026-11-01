package com.sizzlesense.ai.data.repository

import com.sizzlesense.ai.data.local.LocalFallbackCognitiveDataSource
import com.sizzlesense.ai.data.remote.FailoverJsonHttpClient
import com.sizzlesense.ai.data.remote.dto.coaching.CognitiveCoachingResponseDto
import com.sizzlesense.ai.data.remote.dto.coaching.toDomain
import com.sizzlesense.ai.data.remote.dto.eval.EvalSuiteReportDto
import com.sizzlesense.ai.data.remote.dto.eval.toDomain
import com.sizzlesense.ai.data.remote.dto.request.SessionAnalyzeRequestDto
import com.sizzlesense.ai.data.remote.dto.request.toDto
import com.sizzlesense.ai.domain.model.camera.TapoCameraConfig
import com.sizzlesense.ai.domain.model.coaching.CognitiveCoachingResponse
import com.sizzlesense.ai.domain.model.eval.EvalSuiteReport
import com.sizzlesense.ai.domain.model.session.SessionAnalyzeRequest
import com.sizzlesense.ai.domain.repository.CognitiveRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Singleton
class SizzleCognitiveRepository @Inject constructor(
    private val remoteClient: FailoverJsonHttpClient,
    private val localFallback: LocalFallbackCognitiveDataSource
) : CognitiveRepository {

    override suspend fun analyzeSession(
        baseUrl: String,
        request: SessionAnalyzeRequest
    ): CognitiveCoachingResponse {
        val requestDto = request.toDto()
        val bodyString = remoteClient.json.encodeToString(
            SessionAnalyzeRequestDto.serializer(),
            requestDto
        )
        return remoteClient.postJsonWithFailover(
            primaryBaseUrl = baseUrl,
            endpointPath = "/api/v1/sessions/analyze",
            jsonBody = bodyString,
            responseSerializer = CognitiveCoachingResponseDto.serializer()
        ).map { dto ->
            dto.toDomain()
        }.getOrElse {
            localFallback.buildFallbackCoachingResponse(request)
        }
    }

    override suspend fun captureFromBackendTapoAndAnalyze(
        baseUrl: String,
        tapoConfig: TapoCameraConfig,
        attemptNumber: Int,
        reflectionText: String
    ): CognitiveCoachingResponse {
        val payload = buildJsonObject {
            put("ip_address", tapoConfig.ipAddress)
            put("username", tapoConfig.username)
            put("password", tapoConfig.password)
            put("learner_id", "tapo_android_learner")
            put("dish_id", "sauteed_onions")
            put("attempt_number", attemptNumber)
            put("learner_reflection", reflectionText)
        }.toString()

        return remoteClient.postJsonWithFailover(
            primaryBaseUrl = baseUrl,
            endpointPath = "/api/v1/camera/tapo-live-analyze",
            jsonBody = payload,
            responseSerializer = CognitiveCoachingResponseDto.serializer()
        ).map { dto ->
            dto.toDomain()
        }.getOrElse { error ->
            throw IllegalStateException(
                "Live Tapo backend capture failed (${error.message}). Check username/password or backend URL.",
                error
            )
        }
    }

    override suspend fun runEvaluationBenchmark(baseUrl: String): EvalSuiteReport {
        return remoteClient.postJsonWithFailover(
            primaryBaseUrl = baseUrl,
            endpointPath = "/api/v1/evals/run",
            jsonBody = "{}",
            responseSerializer = EvalSuiteReportDto.serializer()
        ).map { dto ->
            dto.toDomain()
        }.getOrElse {
            localFallback.buildFallbackEvalReport()
        }
    }
}
