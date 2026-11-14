package com.sizzlesense.ai.presentation.evalbench

import com.sizzlesense.ai.core.result.AppError
import com.sizzlesense.ai.core.result.AppResult
import com.sizzlesense.ai.data.local.LocalFallbackCognitiveDataSource
import com.sizzlesense.ai.data.session.InMemorySessionStore
import com.sizzlesense.ai.domain.usecase.RunHumanLearningEvalUseCase
import com.sizzlesense.ai.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EvalBenchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fallbackDataSource = LocalFallbackCognitiveDataSource()

    @Test
    fun runHumanLearningEvalSuite_records10TrajectorySuiteInSessionStore() {
        runTest(mainDispatcherRule.testDispatcher) {
            val sessionStore = InMemorySessionStore()
            val viewModel = EvalBenchViewModel(
                sessionStore = sessionStore,
                runHumanLearningEval = RunHumanLearningEvalUseCase {
                    AppResult.Success(fallbackDataSource.buildFallbackEvalReport())
                }
            )
            val collectorJob = viewModel.uiState.onEach {}.launchIn(backgroundScope)

            viewModel.onIntent(EvalBenchUiIntent.RunHumanLearningEvalSuite)

            assertFalse(viewModel.uiState.value.isRunningEvals)
            assertNull(viewModel.uiState.value.errorMessage)
            val report = viewModel.uiState.value.evalSuiteReport
            assertNotNull(report)
            assertEquals(10, report?.totalSessionsEvaluated)
            assertEquals(1.0, report?.overallHumanLearningScore ?: 0.0, 0.001)

            collectorJob.cancel()
        }
    }

    @Test
    fun runHumanLearningEvalSuite_surfacesTimeoutErrorAndSupportsClearError() {
        runTest(mainDispatcherRule.testDispatcher) {
            val sessionStore = InMemorySessionStore()
            val viewModel = EvalBenchViewModel(
                sessionStore = sessionStore,
                runHumanLearningEval = RunHumanLearningEvalUseCase {
                    AppResult.Failure(AppError.Timeout("Evaluation timed out"))
                }
            )
            val collectorJob = viewModel.uiState.onEach {}.launchIn(backgroundScope)

            viewModel.onIntent(EvalBenchUiIntent.RunHumanLearningEvalSuite)

            assertFalse(viewModel.uiState.value.isRunningEvals)
            assertEquals("Evaluation timed out", viewModel.uiState.value.errorMessage)

            viewModel.onIntent(EvalBenchUiIntent.ClearError)
            assertNull(viewModel.uiState.value.errorMessage)

            collectorJob.cancel()
        }
    }
}
