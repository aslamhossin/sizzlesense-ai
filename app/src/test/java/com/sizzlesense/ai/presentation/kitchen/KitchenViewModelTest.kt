package com.sizzlesense.ai.presentation.kitchen

import com.sizzlesense.ai.core.result.AppError
import com.sizzlesense.ai.core.result.AppResult
import com.sizzlesense.ai.data.local.LocalFallbackCognitiveDataSource
import com.sizzlesense.ai.data.session.InMemorySessionStore
import com.sizzlesense.ai.domain.model.camera.CameraDiscoveryInfo
import com.sizzlesense.ai.domain.model.coaching.AcousticSizzleSignature
import com.sizzlesense.ai.domain.model.session.LearnerArchetypeOption
import com.sizzlesense.ai.domain.usecase.AnalyzeCookingSessionUseCase
import com.sizzlesense.ai.domain.usecase.CaptureLiveTapoSessionUseCase
import com.sizzlesense.ai.domain.usecase.DiscoverCameraAndServerUseCase
import com.sizzlesense.ai.presentation.kitchen.component.PcmWavSizzleCodec
import com.sizzlesense.ai.testing.MainDispatcherRule
import java.util.Base64
import kotlinx.coroutines.CompletableDeferred
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

    private fun offlineDiscoveryUseCase() = DiscoverCameraAndServerUseCase { _, _ ->
        AppResult.Failure(AppError.NetworkUnavailable("Discovery offline in test"))
    }

    @Test
    fun onIntent_updatesSessionStoreAndSynchronizesKitchenUiState() {
        runTest(mainDispatcherRule.testDispatcher) {
            val sessionStore = InMemorySessionStore()
            val viewModel = KitchenViewModel(
                sessionStore = sessionStore,
                analyzeCookingSession = AnalyzeCookingSessionUseCase { _, req ->
                    AppResult.Success(fallbackDataSource.buildFallbackCoachingResponse(req))
                },
                captureLiveTapoSession = CaptureLiveTapoSessionUseCase { _, _, _, _ ->
                    error("Unused in state sync test")
                },
                discoverCameraAndServer = offlineDiscoveryUseCase()
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
        runTest(mainDispatcherRule.testDispatcher) {
            val sessionStore = InMemorySessionStore()
            val viewModel = KitchenViewModel(
                sessionStore = sessionStore,
                analyzeCookingSession = AnalyzeCookingSessionUseCase { _, req ->
                    AppResult.Success(fallbackDataSource.buildFallbackCoachingResponse(req))
                },
                captureLiveTapoSession = CaptureLiveTapoSessionUseCase { _, _, _, _ ->
                    error("Unused")
                },
                discoverCameraAndServer = offlineDiscoveryUseCase()
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
        runTest(mainDispatcherRule.testDispatcher) {
            val sessionStore = InMemorySessionStore()
            val viewModel = KitchenViewModel(
                sessionStore = sessionStore,
                analyzeCookingSession = AnalyzeCookingSessionUseCase { _, req ->
                    AppResult.Success(fallbackDataSource.buildFallbackCoachingResponse(req))
                },
                captureLiveTapoSession = CaptureLiveTapoSessionUseCase { _, _, _, _ ->
                    AppResult.Failure(AppError.NetworkUnavailable("Should not reach gateway"))
                },
                discoverCameraAndServer = offlineDiscoveryUseCase()
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

    @Test
    fun onAudioSampleCaptured_encodesValidRiffWavAndForwardsAudioToAnalyzeGateway() {
        runTest(mainDispatcherRule.testDispatcher) {
            var forwardedAudioB64: String? = null
            val sessionStore = InMemorySessionStore()
            val viewModel = KitchenViewModel(
                sessionStore = sessionStore,
                analyzeCookingSession = AnalyzeCookingSessionUseCase { _, req ->
                    forwardedAudioB64 = req.audioWavBase64
                    AppResult.Success(fallbackDataSource.buildFallbackCoachingResponse(req))
                },
                captureLiveTapoSession = CaptureLiveTapoSessionUseCase { _, _, _, _ ->
                    error("Unused")
                },
                discoverCameraAndServer = offlineDiscoveryUseCase()
            )

            val collectorJob = viewModel.uiState.onEach {}.launchIn(backgroundScope)

            val wavB64 = PcmWavSizzleCodec.synthesizePresetWavBase64(
                signature = AcousticSizzleSignature.STEADY_APPLAUSE_SIZZLE,
                durationMs = 200
            )
            val decodedBytes = Base64.getDecoder().decode(wavB64)
            assertEquals("RIFF", String(decodedBytes, 0, 4, Charsets.US_ASCII))
            assertEquals("WAVE", String(decodedBytes, 8, 4, Charsets.US_ASCII))

            viewModel.onIntent(
                KitchenUiIntent.OnAudioSampleCaptured(
                    base64Wav = wavB64,
                    timestampLabel = "Preset WAV: 155°C Maillard Sizzle"
                )
            )
            assertEquals("Preset WAV: 155°C Maillard Sizzle", viewModel.uiState.value.capturedAudioLabel)

            viewModel.onIntent(KitchenUiIntent.AnalyzeCurrentSession)
            val effect = viewModel.effects.first()
            assertEquals(KitchenUiEffect.NavigateToCoaching, effect)
            assertEquals(wavB64, forwardedAudioB64)

            collectorJob.cancel()
        }
    }

    @Test
    fun analyzeCurrentSession_ignoresDuplicateRequestWhileAnalysisIsInFlight() {
        runTest(mainDispatcherRule.testDispatcher) {
            val sessionStore = InMemorySessionStore()
            val gate = CompletableDeferred<Unit>()
            var invocationCount = 0

            val viewModel = KitchenViewModel(
                sessionStore = sessionStore,
                analyzeCookingSession = AnalyzeCookingSessionUseCase { _, req ->
                    invocationCount += 1
                    gate.await()
                    AppResult.Success(fallbackDataSource.buildFallbackCoachingResponse(req))
                },
                captureLiveTapoSession = CaptureLiveTapoSessionUseCase { _, _, _, _ ->
                    error("Unused")
                },
                discoverCameraAndServer = offlineDiscoveryUseCase()
            )

            val collectorJob = viewModel.uiState.onEach {}.launchIn(backgroundScope)

            viewModel.onIntent(KitchenUiIntent.AnalyzeCurrentSession)
            viewModel.onIntent(KitchenUiIntent.AnalyzeCurrentSession)

            assertTrue(viewModel.uiState.value.isAnalyzing)
            assertEquals(1, invocationCount)

            gate.complete(Unit)
            assertEquals(KitchenUiEffect.NavigateToCoaching, viewModel.effects.first())
            assertFalse(viewModel.uiState.value.isAnalyzing)

            collectorJob.cancel()
        }
    }

    @Test
    fun startupDiscovery_silentlySyncsLiveCameraAndBackendWithoutErrorBannerWhenOfflineFirst() {
        runTest(mainDispatcherRule.testDispatcher) {
            val sessionStore = InMemorySessionStore()
            var callCount = 0
            val viewModel = KitchenViewModel(
                sessionStore = sessionStore,
                analyzeCookingSession = AnalyzeCookingSessionUseCase { _, req ->
                    AppResult.Success(fallbackDataSource.buildFallbackCoachingResponse(req))
                },
                captureLiveTapoSession = CaptureLiveTapoSessionUseCase { _, _, _, _ ->
                    error("Unused")
                },
                discoverCameraAndServer = DiscoverCameraAndServerUseCase { _, _ ->
                    callCount += 1
                    if (callCount == 1) {
                        AppResult.Failure(AppError.NetworkUnavailable("Server offline"))
                    } else {
                        AppResult.Success(
                            CameraDiscoveryInfo(
                                resolvedBackendUrl = "http://10.0.2.2:8000",
                                advertisedBackendUrl = "http://192.168.11.7:8000",
                                cameraIp = "192.168.11.14",
                                cameraPort = 554,
                                cameraReachable = true,
                                hasBackendCredentials = true,
                                discoverySource = "lan_scan"
                            )
                        )
                    }
                }
            )

            val collectorJob = viewModel.uiState.onEach {}.launchIn(backgroundScope)

            // Startup failure stays silent
            assertNull(viewModel.uiState.value.errorMessage)
            assertFalse(viewModel.uiState.value.isDiscoveringSettings)

            // Explicit AutoDetect updates backend URL + Tapo IP
            viewModel.onIntent(KitchenUiIntent.AutoDetectSettings)
            assertEquals("http://10.0.2.2:8000", viewModel.uiState.value.backendBaseUrl)
            assertEquals("192.168.11.14", viewModel.uiState.value.tapoConfig.ipAddress)
            assertEquals(
                "Auto-synced • Camera 192.168.11.14 online",
                viewModel.uiState.value.setupStatusMessage
            )

            collectorJob.cancel()
        }
    }
}
