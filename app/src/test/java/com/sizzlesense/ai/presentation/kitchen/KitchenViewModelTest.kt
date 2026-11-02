package com.sizzlesense.ai.presentation.kitchen

import com.sizzlesense.ai.core.result.AppError
import com.sizzlesense.ai.core.result.AppResult
import com.sizzlesense.ai.data.local.LocalFallbackCognitiveDataSource
import com.sizzlesense.ai.data.session.InMemorySessionStore
import com.sizzlesense.ai.domain.model.session.LearnerArchetypeOption
import com.sizzlesense.ai.domain.usecase.AnalyzeCookingSessionUseCase
import com.sizzlesense.ai.domain.usecase.CaptureLiveTapoSessionUseCase
import com.sizzlesense.ai.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class KitchenViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fallbackDataSource = LocalFallbackCognitiveDataSource()

    @Test
    fun onIntent_updatesSessionStoreAndSynchronizesKitchenUiState() {
        runTest {
            val sessionStore = InMemorySessionStore()
            val viewModel = KitchenViewModel(
                sessionStore = sessionStore,
                analyzeCookingSession = AnalyzeCookingSessionUseCase { _, req ->
                    AppResult.Success(fallbackDataSource.buildFallbackCoachingResponse(req))
                },
                captureLiveTapoSession = CaptureLiveTapoSessionUseCase { _, _, _, _ ->
                    error("Unused in state sync test")
                }
            )

            // Activate WhileSubscribed collector as Compose collectAsStateWithLifecycle does
            val collectorJob = viewModel.uiState.onEach {}.launchIn(backgroundScope)

            viewModel.onIntent(KitchenUiIntent.UpdateBackendUrl(" http://10.0.2.2:8000 "))
            assertEquals("http://10.0.2.2:8000", viewModel.uiState.value.backendBaseUrl)

            viewModel.onIntent(
                KitchenUiIntent.UpdateTapoConfig(
                    ipAddress = "192.168.11.16",
                    username = "aslam",
                    password = "secret",
                    useSubStream = true
                )
            )
            assertEquals("aslam", viewModel.uiState.value.tapoConfig.username)
            assertTrue(viewModel.uiState.value.tapoConfig.useSubStream)

            viewModel.onIntent(
                KitchenUiIntent.OnLiveFrameCaptured(
                    base64Jpeg = "BASE64_JPEG_PAYLOAD",
                    timestampLabel = "Captured at 09:05:00"
                )
            )
            assertEquals("Captured at 09:05:00", viewModel.uiState.value.capturedFrameLabel)
            assertEquals("BASE64_JPEG_PAYLOAD", sessionStore.sessionState.value.capturedFrameBase64)

            viewModel.onIntent(KitchenUiIntent.SelectArchetype(LearnerArchetypeOption.HEAT_ANXIETY))
            assertEquals(
                LearnerArchetypeOption.HEAT_ANXIETY,
                viewModel.uiState.value.selectedArchetype
            )

            collectorJob.cancel()
        }
    }

    @Test
    fun analyzeCurrentSession_recordsCoachingResponseAndEmitsNavigateToCoachingEffect() {
        runTest {
            val sessionStore = InMemorySessionStore()
            val viewModel = KitchenViewModel(
                sessionStore = sessionStore,
                analyzeCookingSession = AnalyzeCookingSessionUseCase { _, req ->
                    AppResult.Success(fallbackDataSource.buildFallbackCoachingResponse(req))
                },
                captureLiveTapoSession = CaptureLiveTapoSessionUseCase { _, _, _, _ ->
                    error("Unused")
                }
            )

            val collectorJob = viewModel.uiState.onEach {}.launchIn(backgroundScope)

            viewModel.onIntent(KitchenUiIntent.AnalyzeCurrentSession)

            val effect = viewModel.effects.first()
            assertEquals(KitchenUiEffect.NavigateToCoaching, effect)
            assertFalse(viewModel.uiState.value.isAnalyzing)
            assertNull(viewModel.uiState.value.errorMessage)
            assertNotNull(sessionStore.sessionState.value.latestCoachingResponse)

            collectorJob.cancel()
        }
    }

    @Test
    fun captureFromBackendTapoAndAnalyze_setsErrorMessageWhenCameraIpIsBlank() {
        runTest {
            val sessionStore = InMemorySessionStore()
            val viewModel = KitchenViewModel(
                sessionStore = sessionStore,
                analyzeCookingSession = AnalyzeCookingSessionUseCase { _, req ->
                    AppResult.Success(fallbackDataSource.buildFallbackCoachingResponse(req))
                },
                captureLiveTapoSession = CaptureLiveTapoSessionUseCase { _, _, _, _ ->
                    AppResult.Failure(AppError.NetworkUnavailable("Should not reach gateway"))
                }
            )

            val collectorJob = viewModel.uiState.onEach {}.launchIn(backgroundScope)

            viewModel.onIntent(
                KitchenUiIntent.UpdateTapoConfig(
                    ipAddress = "   ",
                    username = "chef",
                    password = "pw",
                    useSubStream = false
                )
            )
            viewModel.onIntent(KitchenUiIntent.CaptureFromBackendTapoAndAnalyze)

            assertFalse(viewModel.uiState.value.isAnalyzing)
            assertEquals(
                "Tapo C100 IP address cannot be blank.",
                viewModel.uiState.value.errorMessage
            )

            viewModel.onIntent(KitchenUiIntent.ClearError)
            assertNull(viewModel.uiState.value.errorMessage)

            collectorJob.cancel()
        }
    }
}
