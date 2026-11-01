package com.sizzlesense.ai.data.repository

import com.sizzlesense.ai.data.local.LocalFallbackCognitiveDataSource
import com.sizzlesense.ai.data.remote.FailoverJsonHttpClient
import com.sizzlesense.ai.domain.model.CognitiveCoachingResponse
import com.sizzlesense.ai.domain.model.EvalSuiteReport
import com.sizzlesense.ai.domain.model.SessionAnalyzeRequest
import com.sizzlesense.ai.domain.model.TapoCameraConfig
import com.sizzlesense.ai.domain.repository.CognitiveRepository
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Clean Architecture Data Repository implementing [CognitiveRepository].
 *
 * Adheres to:
 * - SRP: Coordinates remote HTTP calls ([FailoverJsonHttpClient]) and offline fallback ([LocalFallbackCognitiveDataSource]).
 * - DRY: Reuses [FailoverJsonHttpClient.postJsonWithFailover] across all API endpoints.
 * - DIP & OCP: Depends on injectable collaborators and implements the domain [CognitiveRepository] contract.
 */
class SizzleCognitiveRepository(
    private val remoteClient: FailoverJsonHttpClient = FailoverJsonHttpClient(),
    private val localFallback: LocalFallbackCognitiveDataSource = LocalFallbackCognitiveDataSource(),
) : CognitiveRepository {

    override suspend fun analyzeSession(
        baseUrl: String,
        request: SessionAnalyzeRequest,
    ): CognitiveCoachingResponse {
        val bodyString = remoteClient.json.encodeToString(SessionAnalyzeRequest.serializer(), request)
        return remoteClient.postJsonWithFailover(
            primaryBaseUrl = baseUrl,
            endpointPath = "/api/v1/sessions/analyze",
            jsonBody = bodyString,
            responseSerializer = CognitiveCoachingResponse.serializer(),
        ).getOrElse {
            localFallback.buildFallbackCoachingResponse(request)
        }
    }

    override suspend fun captureFromBackendTapoAndAnalyze(
        baseUrl: String,
        tapoConfig: TapoCameraConfig,
        attemptNumber: Int,
        reflectionText: String,
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
            responseSerializer = CognitiveCoachingResponse.serializer(),
        ).getOrElse { error ->
            throw IllegalStateException(
                "Live Tapo backend capture failed (${error.message}). Check username/password or backend URL.",
                error,
            )
        }
    }

    override suspend fun runEvaluationBenchmark(baseUrl: String): EvalSuiteReport {
        return remoteClient.postJsonWithFailover(
            primaryBaseUrl = baseUrl,
            endpointPath = "/api/v1/evals/run",
            jsonBody = "{}",
            responseSerializer = EvalSuiteReport.serializer(),
        ).getOrElse {
            localFallback.buildFallbackEvalReport()
        }
    }
}
