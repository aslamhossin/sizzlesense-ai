package com.sizzlesense.ai.domain.repository

import com.sizzlesense.ai.domain.model.eval.EvalSuiteReport

fun interface EvaluationBenchmarkGateway {
    suspend fun runEvaluationBenchmark(baseUrl: String): EvalSuiteReport
}
