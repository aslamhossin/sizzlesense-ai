# SizzleSense AI — Applied AI Human Learning Coach (Android + MVI + Tapo C100 RTSP + 4-Stage Cognitive Engine)

**Date:** 2026-10-31  
**Target Role:** Cookpad — Applied AI Engineer, Human Learning (Senior Principal Level) — *moment by cook*  
**Architecture:** Clean Architecture + Strict MVI (Model-View-Intent) in Kotlin / Jetpack Compose  
**AI Frontier Models:** Google `gemini-4-argon` (Flagship Reasoning for Learner State & Chef Pedagogy) + `gemini-3.8-flash` (Fast Multimodal Observation) + Dynamic `v1beta/models` Discovery

---

## 1. Executive Summary & Alignment with Cookpad's "moment"

`moment by cookpad` is not a recipe app or a naive "AI chat wrapper" that lists surface-level cooking mistakes. Its core philosophy is **Human Learning**: transforming how a home cook **observes, thinks, and judges** dynamic kitchen situations across a **3-Attempt Mastery Loop** (cooking the same dish 3 times).

**Moment Sensei** is a production-grade Android portfolio system built specifically to demonstrate mastery of the four core pillars required by the Cookpad Applied AI Engineer role:

1. **Physical World Understanding (Observed Fact vs. User Belief):** Captures overhead kitchen video from a real **TP-Link Tapo C100 Wi-Fi Camera via RTSP (`rtsp://...:554/stream1`)** (or built-in 3-attempt simulation clips) alongside the learner's post-cooking voice/text reflection, explicitly detecting **Perception Gaps** where the learner's self-assessment contradicts visual/acoustic reality.
2. **Learner State Estimation (Latent Mental Model):** Infers *why* the learner acted as they did (e.g., *Heat Anxiety*: turning down heat out of fear of burning vs. *Moisture Blindness*: not realizing water prevents Maillard browning) and updates a persistent longitudinal `LearnerProfile` across Attempts 1, 2, and 3.
3. **Expert Reasoning & Single-Issue Teaching Decision:** Structures world-class chef tacit principles into an explicit `ChefReasoningFramework` and enforces a strict pedagogical constraint: **select only the SINGLE highest-leverage concept to teach right now**, deliberately suppressing secondary mistakes to avoid cognitive overload, and framing the advice around **sensory cues (sight, sizzle sound, aroma)** rather than rigid timer rules.
4. **Evaluation of Human Learning (Automated Eval Suite):** Includes an in-app and unit-testable **Applied AI Evaluation Bench** that runs synthetic multi-attempt learner personas through the pipeline and scores **Diagnostic Accuracy**, **Single-Issue Pedagogical Restraint**, **Sensory Grounding**, and **Cross-Session Behavioral Transfer (Attempt 1 → 2 → 3)**.

---

## 2. System Architecture (Strict MVI + 4-Stage Applied AI Pipeline)

### 2.1 High-Level Data Flow

```
[Kitchen Video Source]                      [Learner Audio/Text Reflection]
 ├── Tapo C100 RTSP (Media3 ExoPlayer)       └── SpeechRecognizer / Audio + Transcript
 └── Pre-loaded 3-Attempt Scenarios
                │                                           │
                └────────────────────┬──────────────────────┘
                                     ▼
                       [MVI Intent: SubmitSessionIntent]
                                     │
                                     ▼
            ┌────────────────────────────────────────────────┐
            │     4-STAGE APPLIED AI COGNITIVE PIPELINE      │
            │  (Gemini 2.5 Flash Structured JSON + Offline   │
            │   Deterministic Fallback Engine for Evals)     │
            ├────────────────────────────────────────────────┤
            │ Stage 1: PhysicalWorldObserver                 │
            │   → Video Facts + Timeline vs. Learner Claim   │
            │   → Explicit PerceptionGap Detection           │
            │                                                │
            │ Stage 2: LearnerStateEstimator                 │
            │   → Latent Mental Model (Understands vs.       │
            │     Misunderstands + Decision Root Cause)      │
            │   → Longitudinal Delta across Attempts 1→2→3   │
            │                                                │
            │ Stage 3: ExpertTeachingSelector                │
            │   → Matches against Chef Principle Graph       │
            │   → Ranks candidate issues, picks ONLY ONE,    │
            │     logs DeliberatelyOmittedIssues             │
            │                                                │
            │ Stage 4: SensoryCoachCommunicator              │
            │   → Crafts sensory-first coaching (Visual,     │
            │     Auditory Sizzle, Olfactory cues)           │
            │   → Defines observable NextAttemptHypothesis   │
            └────────────────────────────────────────────────┘
                                     │
                                     ▼
                 [Pure MVI Reducer → Immutable MomentUiState]
                                     │
        ┌────────────────────────────┼────────────────────────────┐
        ▼                            ▼                            ▼
[1. Kitchen & RTSP Tab]   [2. 3-Attempt Coaching Tab]   [3. AI X-Ray & Evals Tab]
```

### 2.2 Strict MVI Contract (`presentation/mvi`)

- **`MomentUiState` (Single Immutable State):**
  - `activeTab`: `KitchenCapture`, `CoachingLoop`, `AiXRayInspector`, `EvaluationSuite`
  - `tapoCameraConfig`: IP address, username, password, stream quality (`stream1` 1080p / `stream2` 360p), connection status, captured keyframes.
  - `selectedCurriculum`: Dish & core culinary principle (e.g., *Caramelized & Sautéed Onions — Moisture Evaporation & Maillard Control*, *Crispy Skin Chicken Thigh — Rendering & Cold-Pan Start*, *French Omelette — Curd Emulsion & Pan Agitation*).
  - `currentAttemptNumber`: `1`, `2`, or `3`
  - `learnerReflectionText`: Post-cooking reflection captured via voice or preset scenario.
  - `pipelineExecutionState`: Idle, RunningStage(1..4), Completed(`CognitiveTrace`), Error.
  - `attemptHistory`: Map of Attempt 1, 2, 3 `SessionRecord`s showing longitudinal skill transfer.
  - `learnerProfile`: Persistent latent skill & misconception graph across sessions.
  - `evalSuiteState`: Results of running the automated Human Learning Evaluation Benchmark.

- **`MomentIntent` (Sealed Interface):**
  - `SelectTab(tab: AppTab)`
  - `UpdateTapoCredentials(ip: String, user: String, pass: String, useSubStream: Boolean)`
  - `ToggleTapoStream(connect: Boolean)`
  - `CaptureKeyframeFromStream(bitmapBytes: ByteArray?, timestampLabel: String)`
  - `SelectDishCurriculum(dishId: String)`
  - `SelectAttemptNumber(attempt: Int)`
  - `LoadPresetScenario(dishId: String, attempt: Int, personaId: String)`
  - `UpdateReflectionText(text: String)`
  - `RunCognitiveCoachingPipeline`
  - `UpdateGeminiApiKey(apiKey: String)`
  - `RunHumanLearningEvalSuite`

- **`MomentEffect` (Sealed Interface for One-Off Side Effects):**
  - `ShowSnackbar(message: String)`
  - `NavigateToTab(tab: AppTab)`

---

## 3. Domain Models for the 4 Applied AI Pillars

### Pillar 1: Physical World Understanding (`PhysicalObservationReport`)
- `observedTimelineFacts`: List of timestamped visual/acoustic facts (`00:08 - Pale translucent onions pooling in liquid; zero browning on pan surface; weak bubble rate`).
- `learnerStatedBelief`: What the learner claimed in their post-cooking reflection.
- `perceptionGapLevel`: `NONE`, `MODERATE`, `CRITICAL_BLINDSPOT`.
- `perceptionGapExplanation`: Clear articulation of the discrepancy between video evidence and learner self-assessment.
- `environmentalUncertainties`: What the camera could *not* directly observe (e.g., exact burner BTU, prior salt timing) that must be treated as inferred rather than fact.

### Pillar 2: Learner State Estimation (`LearnerStateEstimate`)
- `demonstratedUnderstandings`: Concepts the learner already grasps.
- `latentMisconceptions`: Ranked hypotheses of what the learner does *not* yet understand (with confidence 0.0–1.0).
- `decisionRootCause`: Why the learner took that action (e.g., *"Learner lowered heat prematurely because they mistook rapid steam evaporation for imminent burning"*).
- `behavioralTransferFromPreviousAttempt`: Explicit comparison with Attempt `N-1` (e.g., `INITIAL_BASELINE`, `CONCEPT_ADOPTED`, `PARTIAL_TRANSFER`, `REGRESSED`).

### Pillar 3: Expert Reasoning & Single-Issue Selection (`ExpertTeachingDecision`)
- `candidateIssuesDetected`: All culinary issues detected in the session (typically 3–4 issues).
- `selectedPrimaryIssue`: The **single** highest-leverage issue chosen for coaching now.
- `deliberatelyOmittedIssues`: The remaining 2–3 issues explicitly suppressed for this session, with pedagogical justification for why teaching them now would cause cognitive overload.
- `targetChefPrinciple`: The underlying culinary science principle from the Chef Knowledge Graph.

### Pillar 4: Sensory Coaching & Evaluation (`SensoryCoachingOutput` & `EvalSuiteResult`)
- `sensoryShiftPrompt`: How to perceive the situation differently next time using **Visual**, **Auditory (Sizzle/Pan Sound)**, and **Tactile/Olfactory** cues.
- `coachMessage`: Empathetic, chef-level coaching message that never says "just follow step 3" but changes how the learner observes the pan.
- `nextAttemptExperiment`: A concrete, observable micro-experiment for the learner's next attempt.
- `evalMetrics`:
  1. `perceptionGapDetectionScore` (Did the AI catch the contradiction between video and user audio?)
  2. `latentRootCauseAccuracy` (Did the AI infer *why* the user failed rather than just *what* failed?)
  3. `singleIssueFocusScore` (Did the AI exercise pedagogical restraint and teach only 1 primary concept?)
  4. `sensoryGroundingScore` (Did the coaching teach sensory observation cues rather than rigid timer rules?)
  5. `longitudinalTransferDetectionScore` (Across Attempts 1 → 2 → 3, did the AI accurately track whether the learner's judgment changed?)

---

## 4. Hardware Integration: TP-Link Tapo C100 Overhead Kitchen Camera

- Uses `androidx.media3:media3-exoplayer` + `androidx.media3:media3-exoplayer-rtsp`.
- Connects directly over local Wi-Fi to:
  - Main 1080p stream: `rtsp://<user>:<pass>@<ip>:554/stream1`
  - Sub 360p low-latency AI stream: `rtsp://<user>:<pass>@<ip>:554/stream2`
- Includes an interactive **Tapo C100 Setup Wizard & Frame Grabber** right inside the Kitchen Tab so tomorrow, when the camera arrives from Amazon, entering the IP + Camera Account credentials immediately streams the live stove view into the app and captures timestamped keyframes for Gemini Multimodal analysis.
- Works 100% out-of-the-box without the physical camera via **Rich Simulated Kitchen Streams** so Cookpad interviewers can evaluate the entire system in 1 click.

---

## 5. Testing & Verification Strategy

- **Unit Tests (`app/src/test`):**
  - MVI Reducer & State transition tests (`MomentReducerTest`).
  - 4-Stage Applied AI Pipeline & Pedagogical Restraint tests (`CognitivePipelineTest`).
  - Automated Human Learning Evaluation Suite (`HumanLearningEvalSuiteTest`) verifying all 5 rubric metrics across multi-attempt learner personas.
  - RTSP URL builder & Tapo C100 configuration validation tests (`TapoRtspUrlBuilderTest`).
