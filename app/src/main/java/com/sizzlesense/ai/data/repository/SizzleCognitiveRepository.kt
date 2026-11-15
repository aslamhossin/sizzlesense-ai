package com.sizzlesense.ai.data.repository

import com.sizzlesense.ai.core.result.AppResult
import com.sizzlesense.ai.core.result.map
import com.sizzlesense.ai.data.remote.FailoverJsonHttpClient
import com.sizzlesense.ai.data.remote.dto.camera.toDomain
import com.sizzlesense.ai.data.remote.dto.coaching.toDomain
import com.sizzlesense.ai.data.remote.dto.eval.toDomain
import com.sizzlesense.ai.data.remote.dto.request.toDto
import com.sizzlesense.ai.data.remote.dto.request.toLiveAnalyzeRequestDto
import com.sizzlesense.ai.domain.model.camera.CameraDiscoveryInfo
import com.sizzlesense.ai.domain.model.camera.TapoCameraConfig
import com.sizzlesense.ai.domain.model.coaching.CognitiveCoachingResponse
import com.sizzlesense.ai.domain.model.eval.EvalSuiteReport
import com.sizzlesense.ai.domain.model.session.SessionAnalyzeRequest
import com.sizzlesense.ai.domain.repository.CameraDiscoveryGateway
import com.sizzlesense.ai.domain.repository.EvaluationBenchmarkGateway
import com.sizzlesense.ai.domain.repository.LearnerHistoryGateway
import com.sizzlesense.ai.domain.repository.LiveTapoCameraGateway
import com.sizzlesense.ai.domain.repository.SessionAnalysisGateway
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SizzleCognitiveRepository @Inject constructor(
    private val remoteClient: FailoverJsonHttpClient
) : SessionAnalysisGateway,
    LiveTapoCameraGateway,
    EvaluationBenchmarkGateway,
    CameraDiscoveryGateway,
    LearnerHistoryGateway {

    override suspend fun analyzeSession(
        baseUrl: String,
        request: SessionAnalyzeRequest
    ): AppResult<CognitiveCoachingResponse> = remoteClient.analyzeSessionWithFailover(
        primaryBaseUrl = baseUrl,
        request = request.toDto()
    ).map { dto ->
        dto.toDomain()
    }

    override suspend fun captureFromBackendTapoAndAnalyze(
        baseUrl: String,
        tapoConfig: TapoCameraConfig,
        attemptNumber: Int,
        reflectionText: String,
        audioWavBase64: String?,
        archetypeHint: String?
    ): AppResult<CognitiveCoachingResponse> {
        val requestDto = tapoConfig.toLiveAnalyzeRequestDto(
            attemptNumber = attemptNumber,
            reflectionText = reflectionText,
            audioWavBase64 = audioWavBase64,
            archetypeHint = archetypeHint
        )
        return remoteClient.captureFromTapoAndAnalyzeWithFailover(
            primaryBaseUrl = baseUrl,
            request = requestDto
        ).map { dto ->
            dto.toDomain()
        }
    }

    override suspend fun discoverCameraAndServer(
        baseUrl: String,
        preferredCameraIp: String
    ): AppResult<CameraDiscoveryInfo> {
        return remoteClient.discoverCameraAndServerWithFailover(
            primaryBaseUrl = baseUrl,
            preferredIp = preferredCameraIp
        ).map { resolved ->
            resolved.data.toDomain(resolvedBackendUrl = resolved.resolvedBaseUrl)
        }
    }

    override suspend fun runEvaluationBenchmark(baseUrl: String): AppResult<EvalSuiteReport> =
        remoteClient.runEvaluationBenchmarkWithFailover(
            primaryBaseUrl = baseUrl
        ).map { dto ->
            dto.toDomain()
        }

    override suspend fun clearLearnerHistory(
        baseUrl: String,
        learnerId: String
    ): AppResult<Unit> = remoteClient.deleteLearnerHistoryWithFailover(
        primaryBaseUrl = baseUrl,
        learnerId = learnerId
    ).map { }
}
