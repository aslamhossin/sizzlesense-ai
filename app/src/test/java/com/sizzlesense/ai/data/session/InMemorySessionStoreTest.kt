package com.sizzlesense.ai.data.session

import com.sizzlesense.ai.di.module.NetworkModule
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
        assertNull(current.latestCoachingResponse)
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
