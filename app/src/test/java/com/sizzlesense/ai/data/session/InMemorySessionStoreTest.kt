package com.sizzlesense.ai.data.session

import com.sizzlesense.ai.data.remote.dto.coaching.toDto
import com.sizzlesense.ai.data.remote.dto.eval.toDto
import com.sizzlesense.ai.di.module.NetworkModule
import com.sizzlesense.ai.testing.TestFixtures
import com.sizzlesense.ai.domain.model.camera.TapoCameraConfig
import com.sizzlesense.ai.domain.model.coaching.AcousticSizzleSignature
import com.sizzlesense.ai.domain.model.coaching.BehavioralTransferStatus
import com.sizzlesense.ai.domain.model.coaching.CognitiveCoachingResponse
import com.sizzlesense.ai.domain.model.coaching.ExpertTeachingDecision
import com.sizzlesense.ai.domain.model.coaching.FiveCoreQuestionsSummary
import com.sizzlesense.ai.domain.model.session.LearnerArchetypeOption
import com.sizzlesense.ai.domain.model.coaching.LearnerStateEstimate
import com.sizzlesense.ai.domain.model.coaching.MisconceptionHypothesis
import com.sizzlesense.ai.domain.model.coaching.OmittedIssue
import com.sizzlesense.ai.domain.model.coaching.PerceptionGapLevel
import com.sizzlesense.ai.domain.model.coaching.PhysicalObservationReport
import com.sizzlesense.ai.domain.model.coaching.SensoryCoachingOutput
import com.sizzlesense.ai.domain.model.coaching.TimelineFact
import com.sizzlesense.ai.presentation.navigation.SizzleDestination
import com.sizzlesense.ai.presentation.navigation.SizzleRoute
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemorySessionStoreTest {

    @Test
    fun selectArchetype_updatesReflectionTextAndClearsPriorAttemptHistory() {
        val store = InMemorySessionStore()
        store.selectArchetype(LearnerArchetypeOption.HEAT_ANXIETY)

        val current = store.sessionState.value
        assertEquals(LearnerArchetypeOption.HEAT_ANXIETY, current.selectedArchetype)
        assertTrue(current.learnerReflectionText.contains("steaming a lot"))
        assertTrue(current.attemptHistory.isEmpty())
        assertTrue(current.attemptReflections.isEmpty())
        assertNull(current.latestCoachingResponse)
    }

    @Test
    fun selectAttemptNumber_clearsUnrunCoachingResponseAndPreservesCustomReflectionsPerAttempt() {
        val store = InMemorySessionStore()
        val attempt1Response = com.sizzlesense.ai.testing.TestFixtures.buildCoachingResponse(
            attemptNumber = 1
        )
        store.updateReflectionText("Custom reflection for attempt 1")
        store.recordCoachingResponse(attempt1Response)

        store.selectAttemptNumber(2)
        val attempt2State = store.sessionState.value
        assertEquals(2, attempt2State.currentAttemptNumber)
        assertNull(attempt2State.latestCoachingResponse)
        assertTrue(attempt2State.learnerReflectionText.contains("crisp sizzle"))

        store.updateReflectionText("Custom reflection for attempt 2")
        store.selectAttemptNumber(1)
        val restoredAttempt1 = store.sessionState.value
        assertEquals(attempt1Response, restoredAttempt1.latestCoachingResponse)
        assertEquals("Custom reflection for attempt 1", restoredAttempt1.learnerReflectionText)

        store.selectAttemptNumber(2)
        val restoredAttempt2 = store.sessionState.value
        assertNull(restoredAttempt2.latestCoachingResponse)
        assertEquals("Custom reflection for attempt 2", restoredAttempt2.learnerReflectionText)

        store.selectArchetype(LearnerArchetypeOption.PERCEPTION_BLINDSPOT)
        val resetState = store.sessionState.value
        assertTrue(resetState.attemptHistory.isEmpty())
        assertTrue(resetState.attemptReflections.isEmpty())
        assertNull(resetState.latestCoachingResponse)
    }

    @Test
    fun recordCoachingResponse_storesSessionInAttemptHistoryAndUpdatesLatest() {
        val store = InMemorySessionStore()
        val dummyResponse = CognitiveCoachingResponse(
            sessionId = "sess_01",
            learnerId = "android_learner",
            dishId = "sauteed_onions",
            attemptNumber = 1,
            reasoningModelUsed = "gemini-3-flash-preview (Live Gemini API)",
            visionModelUsed = "gemini-3.8-flash + CV",
            fiveCoreQuestions = FiveCoreQuestionsSummary(
                whatIsHappeningNow = "Sweating at 100C",
                whatTheyUnderstandVsNot = "Understands prep, misses moisture gate",
                whyTheyMadeThatDecision = "Crowded pan",
                whatSingleThingToTeachNow = "Moisture Evaporation Gate",
                howToCommunicateToShiftJudgment = "Listen for applause sizzle"
            ),
            stage1PhysicalObservation = PhysicalObservationReport(
                observedTimelineFacts = listOf(
                    TimelineFact(
                        timestamp = "00:12",
                        visualObservation = "Pale onions",
                        acousticSignature = AcousticSizzleSignature.WET_STEAM_BUBBLE
                    )
                ),
                inferredPhysicalStates = listOf("Pan pinned at 100C"),
                learnerStatedBelief = "Watery",
                perceptionGapLevel = PerceptionGapLevel.ALIGNED,
                perceptionGapExplanation = "Aligned"
            ),
            stage2LearnerState = LearnerStateEstimate(
                learnerArchetypeId = "moisture_blindness",
                demonstratedUnderstandings = listOf("Prep"),
                latentMisconceptions = listOf(
                    MisconceptionHypothesis(
                        conceptId = "moisture",
                        description = "Water blocks browning",
                        confidence = 0.9
                    )
                ),
                decisionRootCause = "Crowded pan",
                transferStatus = BehavioralTransferStatus.INITIAL_BASELINE
            ),
            stage3TeachingDecision = ExpertTeachingDecision(
                candidateIssuesDetected = listOf("Moisture Gate", "Cut Geometry"),
                selectedPrimaryIssue = "Moisture Gate",
                causalPriorityRationale = "Rank 1",
                deliberatelyOmittedIssues = listOf(
                    OmittedIssue(
                        issue = "Cut Geometry",
                        suppressionReason = "Cognitive overload"
                    )
                ),
                targetChefPrincipleId = "moisture_evaporation_gate"
            ),
            stage4SensoryCoaching = SensoryCoachingOutput(
                visualCueToObserve = "Shimmering oil",
                auditoryCueToListen = "Crisp sizzle",
                olfactoryOrTactileCue = "Sweet aroma",
                coachingMessage = "Wait for water to evaporate",
                nextAttemptMicroExperiment = "Pause stirring for 15s"
            )
        )

        store.recordCoachingResponse(dummyResponse)

        val current = store.sessionState.value
        assertEquals(dummyResponse, current.latestCoachingResponse)
        assertEquals(dummyResponse, current.attemptHistory[1])

        // Verify disk persistence across separate store instances
        val tempFile = Files.createTempFile("sizzlesense_test_", ".json").toFile()
        try {
            val persistence = SessionPersistenceDataSource(
                storageFile = tempFile,
                json = NetworkModule.provideJson()
            )
            val writerStore = InMemorySessionStore(persistence)
            writerStore.updateBackendUrl("http://192.168.11.6:8000")
            writerStore.updateTapoConfig(
                TapoCameraConfig(
                    ipAddress = "192.168.11.16",
                    username = "chef_tapo",
                    useSubStream = true
                )
            )
            writerStore.recordCoachingResponse(dummyResponse)

            val restoredStore = InMemorySessionStore(persistence)
            val restored = restoredStore.sessionState.value
            assertEquals("http://192.168.11.6:8000", restored.backendBaseUrl)
            assertEquals("chef_tapo", restored.tapoConfig.username)
            assertTrue(restored.tapoConfig.useSubStream)
            assertEquals(dummyResponse, restored.latestCoachingResponse)
            assertEquals(dummyResponse, restored.attemptHistory[1])
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun loadSnapshot_scrubsLegacyV1AndFallbackCachesWhilePreservingSettingsAndV2LiveSessions() {
        val tempFile = Files.createTempFile("sizzlesense_migration_test_", ".json").toFile()
        val json = NetworkModule.provideJson()
        val persistence = SessionPersistenceDataSource(storageFile = tempFile, json = json)
        try {
            val v1Dto = PersistedSessionSnapshotDto(
                schemaVersion = 1,
                backendBaseUrl = "http://192.168.11.7:8000",
                tapoConfig = PersistedTapoCameraConfigDto(
                    ipAddress = "192.168.11.14",
                    username = "chef_saved",
                    password = "pw",
                    useSubStream = true
                ),
                selectedArchetype = LearnerArchetypeOption.HEAT_ANXIETY.name,
                latestCoachingResponse = TestFixtures.buildCoachingResponse(
                    reasoningModelUsed = "gemini-3-flash-preview (Live Gemini API)"
                ).toDto(),
                attemptHistory = mapOf(
                    1 to TestFixtures.buildCoachingResponse(
                        reasoningModelUsed = "gemini-3-flash-preview (Live Gemini API)"
                    ).toDto()
                ),
                evalSuiteReport = TestFixtures.buildEvalReport().toDto()
            )
            // Also strip explicit schema_version to simulate pre-schemaVersion disk JSON
            val rawWithoutVersion = json.encodeToString(
                PersistedSessionSnapshotDto.serializer(),
                v1Dto
            ).replace("\"schema_version\":1,", "")
            tempFile.writeText(rawWithoutVersion, Charsets.UTF_8)

            val upgradedV1 = InMemorySessionStore(persistence).sessionState.value
            assertNull(upgradedV1.latestCoachingResponse)
            assertTrue(upgradedV1.attemptHistory.isEmpty())
            assertNull(upgradedV1.evalSuiteReport)
            assertEquals("http://192.168.11.7:8000", upgradedV1.backendBaseUrl)
            assertEquals("192.168.11.14", upgradedV1.tapoConfig.ipAddress)
            assertEquals("chef_saved", upgradedV1.tapoConfig.username)
            assertEquals(LearnerArchetypeOption.HEAT_ANXIETY, upgradedV1.selectedArchetype)

            // Schema v2 with deterministic/offline fallback markers in attempt history must also be scrubbed
            val pollutedV2 = v1Dto.copy(
                schemaVersion = SessionPersistenceDataSource.CURRENT_SCHEMA_VERSION,
                latestCoachingResponse = TestFixtures.buildCoachingResponse(
                    reasoningModelUsed = "Offline Demo Simulation (Backend Unreachable)"
                ).toDto(),
                attemptHistory = mapOf(
                    1 to TestFixtures.buildCoachingResponse(
                        reasoningModelUsed = "gemini-3-flash-preview (Deterministic-Verified)"
                    ).toDto()
                )
            )
            tempFile.writeText(
                json.encodeToString(PersistedSessionSnapshotDto.serializer(), pollutedV2),
                Charsets.UTF_8
            )
            val scrubbedV2 = InMemorySessionStore(persistence).sessionState.value
            assertNull(scrubbedV2.latestCoachingResponse)
            assertTrue(scrubbedV2.attemptHistory.isEmpty())
            assertNull(scrubbedV2.evalSuiteReport)

            // Clean v2 live sessions & eval report persist intact with schema_version = 2
            tempFile.delete()
            val liveStore = InMemorySessionStore(persistence)
            val liveResponse = TestFixtures.buildCoachingResponse(
                reasoningModelUsed = "gemini-3-flash-preview (Live Gemini API)"
            )
            val liveEval = TestFixtures.buildEvalReport()
            liveStore.recordCoachingResponse(liveResponse)
            liveStore.recordEvalSuiteReport(liveEval)

            assertTrue(tempFile.readText(Charsets.UTF_8).contains("\"schema_version\":2"))
            val restoredLive = InMemorySessionStore(persistence).sessionState.value
            assertEquals(liveResponse, restoredLive.latestCoachingResponse)
            assertEquals(liveResponse, restoredLive.attemptHistory[1])
            assertEquals(liveEval, restoredLive.evalSuiteReport)
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun sizzleRoute_mapsRoundTripWithUserDestinationsAndExcludesEvalBenchTab() {
        assertEquals(
            listOf(
                SizzleDestination.KITCHEN,
                SizzleDestination.COACHING,
                SizzleDestination.XRAY
            ),
            SizzleDestination.entries
        )
        SizzleDestination.entries.forEach { destination ->
            val route = SizzleRoute.fromDestination(destination)
            assertEquals(destination, route.toDestinationOrNull())
        }
        assertNull(SizzleRoute.EvalBench.toDestinationOrNull())
    }
}
