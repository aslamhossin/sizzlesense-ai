package com.sizzlesense.ai.presentation.mvi

import com.sizzlesense.ai.domain.model.AcousticSizzleSignature
import com.sizzlesense.ai.domain.model.BehavioralTransferStatus
import com.sizzlesense.ai.domain.model.CognitiveCoachingResponse
import com.sizzlesense.ai.domain.model.ExpertTeachingDecision
import com.sizzlesense.ai.domain.model.FiveCoreQuestionsSummary
import com.sizzlesense.ai.domain.model.LearnerStateEstimate
import com.sizzlesense.ai.domain.model.MisconceptionHypothesis
import com.sizzlesense.ai.domain.model.OmittedIssue
import com.sizzlesense.ai.domain.model.PerceptionGapLevel
import com.sizzlesense.ai.domain.model.PhysicalObservationReport
import com.sizzlesense.ai.domain.model.SensoryCoachingOutput
import com.sizzlesense.ai.domain.model.TimelineFact
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SizzleReducerTest {

    @Test
    fun `reduce ArchetypeSelected updates reflection text and clears prior attempt history`() {
        val initial = SizzleUiState()
        val next = SizzleReducer.reduce(
            initial,
            SizzleReductionResult.ArchetypeSelected(LearnerArchetypeOption.HEAT_ANXIETY)
        )
        assertEquals(LearnerArchetypeOption.HEAT_ANXIETY, next.selectedArchetype)
        assertTrue(next.learnerReflectionText.contains("steaming a lot"))
        assertTrue(next.attemptHistory.isEmpty())
    }

    @Test
    fun `reduce AnalysisCompleted stores session in attemptHistory and switches to COACHING_LOOP tab`() {
        val initial = SizzleUiState(isAnalyzing = true, activeTab = AppTab.KITCHEN_LIVE)
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
                howToCommunicateToShiftJudgment = "Listen for applause sizzle",
            ),
            stage1PhysicalObservation = PhysicalObservationReport(
                observedTimelineFacts = listOf(
                    TimelineFact("00:12", "Pale onions", AcousticSizzleSignature.WET_STEAM_BUBBLE)
                ),
                inferredPhysicalStates = listOf("Pan pinned at 100C"),
                learnerStatedBelief = "Watery",
                perceptionGapLevel = PerceptionGapLevel.ALIGNED,
                perceptionGapExplanation = "Aligned",
            ),
            stage2LearnerState = LearnerStateEstimate(
                learnerArchetypeId = "moisture_blindness",
                demonstratedUnderstandings = listOf("Prep"),
                latentMisconceptions = listOf(MisconceptionHypothesis("moisture", "Water blocks browning", 0.9)),
                decisionRootCause = "Crowded pan",
                transferStatus = BehavioralTransferStatus.INITIAL_BASELINE,
            ),
            stage3TeachingDecision = ExpertTeachingDecision(
                candidateIssuesDetected = listOf("Moisture Gate", "Cut Geometry"),
                selectedPrimaryIssue = "Moisture Gate",
                causalPriorityRationale = "Rank 1",
                deliberatelyOmittedIssues = listOf(OmittedIssue("Cut Geometry", "Cognitive overload")),
                targetChefPrincipleId = "moisture_evaporation_gate",
            ),
            stage4SensoryCoaching = SensoryCoachingOutput(
                visualCueToObserve = "Shimmering oil",
                auditoryCueToListen = "Crisp sizzle",
                olfactoryOrTactileCue = "Sweet aroma",
                coachingMessage = "Wait for water to evaporate",
                nextAttemptMicroExperiment = "Pause stirring for 15s",
            ),
        )

        val reduced = SizzleReducer.reduce(
            initial,
            SizzleReductionResult.AnalysisCompleted(dummyResponse)
        )
        assertFalse(reduced.isAnalyzing)
        assertEquals(AppTab.COACHING_LOOP, reduced.activeTab)
        assertEquals(dummyResponse, reduced.attemptHistory[1])
    }
}
