package com.sizzlesense.ai.presentation.coaching

import com.sizzlesense.ai.core.result.AppError
import com.sizzlesense.ai.core.result.AppResult
import com.sizzlesense.ai.data.local.LocalFallbackCognitiveDataSource
import com.sizzlesense.ai.data.session.InMemorySessionStore
import com.sizzlesense.ai.domain.usecase.AnalyzeCookingSessionUseCase
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
class CoachingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fallbackDataSource = LocalFallbackCognitiveDataSource()

    @Test
    fun analyzeCurrentAttempt_updatesLatestCoachingResponseInStateAndStore() {
        runTest {
            val sessionStore = InMemorySessionStore()
            sessionStore.selectAttemptNumber(2)

            val viewModel = CoachingViewModel(
                sessionStore = sessionStore,
                analyzeCookingSession = AnalyzeCookingSessionUseCase { _, req ->
                    AppResult.Success(fallbackDataSource.buildFallbackCoachingResponse(req))
                }
            )
            val collectorJob = viewModel.uiState.onEach {}.launchIn(backgroundScope)

            viewModel.onIntent(CoachingUiIntent.AnalyzeCurrentAttempt)

            assertFalse(viewModel.uiState.value.isAnalyzing)
            assertNull(viewModel.uiState.value.errorMessage)
            assertNotNull(viewModel.uiState.value.latestCoachingResponse)
            assertEquals(2, viewModel.uiState.value.latestCoachingResponse?.attemptNumber)

            collectorJob.cancel()
        }
    }

    @Test
    fun analyzeCurrentAttempt_surfacesReadableErrorMessageOnFailure() {
        runTest {
            val sessionStore = InMemorySessionStore()
            val viewModel = CoachingViewModel(
                sessionStore = sessionStore,
                analyzeCookingSession = AnalyzeCookingSessionUseCase { _, _ ->
                    AppResult.Failure(
                        AppError.HttpError(
                            statusCode = 502,
                            responseBody = "Bad Gateway"
                        )
                    )
                }
            )
            val collectorJob = viewModel.uiState.onEach {}.launchIn(backgroundScope)

            viewModel.onIntent(CoachingUiIntent.AnalyzeCurrentAttempt)

            assertFalse(viewModel.uiState.value.isAnalyzing)
            assertEquals(
                "Backend returned HTTP 502: Bad Gateway",
                viewModel.uiState.value.errorMessage
            )

            viewModel.onIntent(CoachingUiIntent.ClearError)
            assertNull(viewModel.uiState.value.errorMessage)

            collectorJob.cancel()
        }
    }
}
