package com.sizzlesense.ai.presentation.xray

import com.sizzlesense.ai.data.local.LocalFallbackCognitiveDataSource
import com.sizzlesense.ai.data.session.InMemorySessionStore
import com.sizzlesense.ai.domain.model.session.SessionAnalyzeRequest
import com.sizzlesense.ai.presentation.main.MainUiIntent
import com.sizzlesense.ai.presentation.main.MainViewModel
import com.sizzlesense.ai.presentation.navigation.SizzleDestination
import com.sizzlesense.ai.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AiXRayAndMainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fallbackDataSource = LocalFallbackCognitiveDataSource()

    @Test
    fun aiXRayViewModel_togglesSuppressedIssuesAndSyncsCoachingTrace() {
        runTest(mainDispatcherRule.testDispatcher) {
            val sessionStore = InMemorySessionStore()
            val sampleResponse = fallbackDataSource.buildFallbackCoachingResponse(
                SessionAnalyzeRequest(
                    learnerId = "l1",
                    dishId = "sauteed_onions",
                    attemptNumber = 1,
                    learnerReflection = "Test"
                )
            )
            sessionStore.recordCoachingResponse(sampleResponse)

            val viewModel = AiXRayViewModel(sessionStore = sessionStore)
            val collectorJob = viewModel.uiState.onEach {}.launchIn(backgroundScope)

            assertTrue(viewModel.uiState.value.showSuppressedIssues)
            assertNotNull(viewModel.uiState.value.latestCoachingResponse)

            viewModel.onIntent(AiXRayUiIntent.ToggleSuppressedIssues(visible = false))
            assertFalse(viewModel.uiState.value.showSuppressedIssues)

            collectorJob.cancel()
        }
    }

    @Test
    fun mainViewModel_managesNavigationDestinationAndAttemptMasteryHistory() {
        runTest(mainDispatcherRule.testDispatcher) {
            val sessionStore = InMemorySessionStore()
            val viewModel = MainViewModel(
                sessionStore = sessionStore,
                isDeveloperEvalsEnabled = true
            )
            val collectorJob = viewModel.uiState.onEach {}.launchIn(backgroundScope)

            viewModel.onIntent(MainUiIntent.SelectDestination(SizzleDestination.XRAY))
            assertEquals(SizzleDestination.XRAY, viewModel.uiState.value.activeDestination)

            viewModel.onIntent(MainUiIntent.SelectAttemptNumber(3))
            assertEquals(3, viewModel.uiState.value.currentAttemptNumber)

            val attempt3Response = fallbackDataSource.buildFallbackCoachingResponse(
                SessionAnalyzeRequest(
                    learnerId = "l1",
                    dishId = "sauteed_onions",
                    attemptNumber = 3,
                    learnerReflection = "Golden brown edges"
                )
            )
            sessionStore.recordCoachingResponse(attempt3Response)
            assertTrue(viewModel.uiState.value.completedAttemptNumbers.contains(3))

            collectorJob.cancel()
        }
    }

    @Test
    fun mainViewModel_opensAndClosesDeveloperEvalsWhenDebugEnabled() {
        runTest(mainDispatcherRule.testDispatcher) {
            val sessionStore = InMemorySessionStore()
            val viewModel = MainViewModel(
                sessionStore = sessionStore,
                isDeveloperEvalsEnabled = true
            )
            val collectorJob = viewModel.uiState.onEach {}.launchIn(backgroundScope)

            assertTrue(viewModel.uiState.value.isDeveloperEvalsEnabled)
            assertFalse(viewModel.uiState.value.isDeveloperEvalsOpen)

            viewModel.onIntent(MainUiIntent.SelectDestination(SizzleDestination.COACHING))
            viewModel.onIntent(MainUiIntent.OpenDeveloperEvals)
            assertTrue(viewModel.uiState.value.isDeveloperEvalsOpen)
            assertEquals(SizzleDestination.COACHING, viewModel.uiState.value.activeDestination)

            viewModel.onIntent(MainUiIntent.CloseDeveloperEvals)
            assertFalse(viewModel.uiState.value.isDeveloperEvalsOpen)
            assertEquals(SizzleDestination.COACHING, viewModel.uiState.value.activeDestination)

            collectorJob.cancel()
        }
    }

    @Test
    fun mainViewModel_blocksDeveloperEvalsWhenDisabledInRelease() {
        runTest(mainDispatcherRule.testDispatcher) {
            val sessionStore = InMemorySessionStore()
            val viewModel = MainViewModel(
                sessionStore = sessionStore,
                isDeveloperEvalsEnabled = false
            )
            val collectorJob = viewModel.uiState.onEach {}.launchIn(backgroundScope)

            assertFalse(viewModel.uiState.value.isDeveloperEvalsEnabled)
            viewModel.onIntent(MainUiIntent.OpenDeveloperEvals)
            assertFalse(viewModel.uiState.value.isDeveloperEvalsOpen)

            collectorJob.cancel()
        }
    }
}
