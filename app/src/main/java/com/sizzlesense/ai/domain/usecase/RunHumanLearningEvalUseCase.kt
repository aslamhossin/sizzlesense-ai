package com.sizzlesense.ai.domain.usecase

import com.sizzlesense.ai.domain.model.eval.EvalSuiteReport
import com.sizzlesense.ai.domain.repository.EvaluationBenchmarkGateway
import javax.inject.Inject

class RunHumanLearningEvalUseCase @Inject constructor(
    private val gateway: EvaluationBenchmarkGateway
) {

    suspend operator fun invoke(baseUrl: String): EvalSuiteReport {
        return gateway.runEvaluationBenchmark(baseUrl = baseUrl)
    }
}
