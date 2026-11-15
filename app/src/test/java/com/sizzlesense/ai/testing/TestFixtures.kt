package com.sizzlesense.ai.testing

import com.sizzlesense.ai.domain.model.coaching.AcousticSizzleSignature
import com.sizzlesense.ai.domain.model.coaching.BehavioralTransferStatus
import com.sizzlesense.ai.domain.model.coaching.CognitiveCoachingResponse
import com.sizzlesense.ai.domain.model.coaching.ComputerVisionMetrics
import com.sizzlesense.ai.domain.model.coaching.ExpertTeachingDecision
import com.sizzlesense.ai.domain.model.coaching.FiveCoreQuestionsSummary
import com.sizzlesense.ai.domain.model.coaching.LearnerStateEstimate
import com.sizzlesense.ai.domain.model.coaching.MisconceptionHypothesis
import com.sizzlesense.ai.domain.model.coaching.OmittedIssue
import com.sizzlesense.ai.domain.model.coaching.PerceptionGapLevel
import com.sizzlesense.ai.domain.model.coaching.PhysicalObservationReport
import com.sizzlesense.ai.domain.model.coaching.SensoryCoachingOutput
import com.sizzlesense.ai.domain.model.coaching.TimelineFact
import com.sizzlesense.ai.domain.model.eval.EvalCaseScore
import com.sizzlesense.ai.domain.model.eval.EvalSuiteReport
import com.sizzlesense.ai.domain.model.session.SessionAnalyzeRequest

object TestFixtures {

    fun buildCoachingResponse(request: SessionAnalyzeRequest): CognitiveCoachingResponse =
        buildCoachingResponse(
            learnerId = request.learnerId,
            dishId = request.dishId,
            attemptNumber = request.attemptNumber,
            learnerReflection = request.learnerReflection,
            archetypeId = request.archetypeHint ?: "moisture_blindness"
        )

    fun buildCoachingResponse(
        sessionId: String = "sess_test_01",
        learnerId: String = "android_moisture_blindness",
        dishId: String = "sauteed_onions",
        attemptNumber: Int = 1,
        learnerReflection: String = "Why are my onions pale?",
        archetypeId: String = "moisture_blindness",
        reasoningModelUsed: String = "gemini-2.5-pro",
        visionModelUsed: String = "cv-pixel-heuristic",
        cvMetrics: ComputerVisionMetrics? = ComputerVisionMetrics(
            maillardBrowningRatio = 0.14,
            surfaceMoistureIndex = 0.72,
            charBurnRatio = 0.01,
            meanLuminance = 148.0,
            dominantVisualRegime = "PALE_STEAMING_OR_RAW"
        )
    ): CognitiveCoachingResponse = CognitiveCoachingResponse(
        sessionId = sessionId,
        learnerId = learnerId,
        dishId = dishId,
        attemptNumber = attemptNumber,
        reasoningModelUsed = reasoningModelUsed,
        visionModelUsed = visionModelUsed,
        fiveCoreQuestions = FiveCoreQuestionsSummary(
            whatIsHappeningNow = "Surface water is boiling at 100C, stalling browning.",
            whatTheyUnderstandVsNot = "Understands knife prep; misses moisture evaporation gate.",
            whyTheyMadeThatDecision = "Mistook vigorous steam for searing heat.",
            whatSingleThingToTeachNow = "Moisture Evaporation Gate",
            howToCommunicateToShiftJudgment = "Listen for steady dry sizzle instead of wet bubbles."
        ),
        stage1PhysicalObservation = PhysicalObservationReport(
            observedTimelineFacts = listOf(
                TimelineFact(
                    timestamp = "00:12",
                    visualObservation = "Pale translucent onion slices releasing white steam.",
                    acousticSignature = AcousticSizzleSignature.WET_STEAM_BUBBLE
                )
            ),
            inferredPhysicalStates = listOf("Pan temperature pinned at 100C water boiling plateau"),
            environmentalUncertainties = listOf("Burner flame level inferred indirectly from steam"),
            cvMetrics = cvMetrics,
            learnerStatedBelief = learnerReflection,
            perceptionGapLevel = PerceptionGapLevel.CRITICAL_BLINDSPOT,
            perceptionGapExplanation = "Learner attributed lack of browning to heat rather than excess steam."
        ),
        stage2LearnerState = LearnerStateEstimate(
            learnerArchetypeId = archetypeId,
            demonstratedUnderstandings = listOf("Consistent medium dice cut size"),
            latentMisconceptions = listOf(
                MisconceptionHypothesis(
                    conceptId = "moisture_evaporation_gate",
                    description = "Maillard browning cannot start while liquid water covers the surface.",
                    confidence = 0.92
                )
            ),
            decisionRootCause = "Cooked with crowded wet batch instead of letting water evaporate.",
            transferStatus = if (attemptNumber >= 3) {
                BehavioralTransferStatus.CONCEPT_ADOPTED
            } else {
                BehavioralTransferStatus.INITIAL_BASELINE
            },
            conceptualShiftVerified = attemptNumber >= 3,
            longitudinalDeltaSummary = "Attempt $attemptNumber diagnostic state."
        ),
        stage3TeachingDecision = ExpertTeachingDecision(
            candidateIssuesDetected = listOf("Moisture Evaporation Gate", "Stirring Cadence"),
            selectedPrimaryIssue = "Moisture Evaporation Gate",
            causalPriorityRationale = "Water plateau blocks Maillard reaction regardless of stirring.",
            deliberatelyOmittedIssues = listOf(
                OmittedIssue(
                    issue = "Stirring Cadence",
                    suppressionReason = "Prevent cognitive overload on single high-leverage cause."
                )
            ),
            targetChefPrincipleId = "moisture_evaporation_gate"
        ),
        stage4SensoryCoaching = SensoryCoachingOutput(
            visualCueToObserve = "Steam plumes clearing into shimmering oil film.",
            auditoryCueToListen = "Low wet bubbling shifting to a steady crisp hiss.",
            olfactoryOrTactileCue = "Raw sulfurous steam shifting to sweet toasted onion aroma.",
            coachingMessage = "Hold off stirring for 20 seconds so trapped water boils off and browning begins.",
            nextAttemptMicroExperiment = "Spread a thinner layer of sliced onions and wait for steady sizzle."
        )
    )

    fun buildEvalReport(totalSessions: Int = 10): EvalSuiteReport {
        val cases = (1..totalSessions).map { index ->
            EvalCaseScore(
                caseId = if (index == totalSessions) {
                    "negative_control_lucky_timer_attempt_2"
                } else {
                    "moisture_blindness_attempt_${((index - 1) % 3) + 1}"
                },
                archetypeId = "moisture_blindness",
                attemptNumber = ((index - 1) % 3) + 1,
                perceptionGapPassed = true,
                rootCausePassed = true,
                singleIssueRestraintPassed = true,
                sensoryGroundingScore = 0.95,
                transferTrackingPassed = true,
                summaryNote = "Verified trajectory case #$index"
            )
        }
        return EvalSuiteReport(
            totalSessionsEvaluated = totalSessions,
            perceptionGapDetectionScore = 1.0,
            latentRootCauseAccuracy = 1.0,
            singleIssueRestraintScore = 1.0,
            sensoryGroundingScore = 0.95,
            crossAttemptTransferScore = 1.0,
            overallHumanLearningScore = 1.0,
            caseResults = cases
        )
    }
}
