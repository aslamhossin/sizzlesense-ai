package com.sizzlesense.ai.domain.repository

import com.sizzlesense.ai.domain.model.CognitiveCoachingResponse
import com.sizzlesense.ai.domain.model.EvalSuiteReport
import com.sizzlesense.ai.domain.model.SessionAnalyzeRequest
import com.sizzlesense.ai.domain.model.TapoCameraConfig

/**
 * ISP (Interface Segregation Principle) + DIP (Dependency Inversion Principle):
 * Fine-grained domain gateways so each UseCase only depends on the capability it requires.
 */
fun interface SessionAnalysisGateway {
    suspend fun analyzeSession(
        baseUrl: String,
        request: SessionAnalyzeRequest,
    ): CognitiveCoachingResponse
}

fun interface LiveTapoCameraGateway {
    suspend fun captureFromBackendTapoAndAnalyze(
        baseUrl: String,
        tapoConfig: TapoCameraConfig,
        attemptNumber: Int,
        reflectionText: String,
    ): CognitiveCoachingResponse
}

fun interface EvaluationBenchmarkGateway {
    suspend fun runEvaluationBenchmark(baseUrl: String): EvalSuiteReport
}

/**
 * Composite domain repository contract implemented by the Data layer.
 */
interface CognitiveRepository :
    SessionAnalysisGateway,
    LiveTapoCameraGateway,
    EvaluationBenchmarkGateway
