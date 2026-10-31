# Technical Direction RFC: Applied AI Engineering for Human Culinary Learning (`SizzleSense AI`)

**Author:** Md Aslam Hossin  
**Status:** Active Reference Architecture  
**Scope:** Backend (`Python / FastAPI / Computer Vision / LLM Evals`) + Edge Client (`Android / Kotlin Strict MVI / Tapo C100 RTSP`)

---

## 1. Problem Formulation: Why Naive Multimodal AI Fails at Human Skill Coaching

Standard multimodal LLM wrappers treat cooking assistance as a **visual defect listing problem**: given a video or photo, they emit 5–10 generic tips (*"chop more evenly, raise the heat, add salt earlier"*).

In a real home kitchen, this fails for three fundamental reasons:
1. **Partial Observability of the Physical World:** Overhead cameras and microphones capture noisy, incomplete signals. Worse, a learner's post-cooking reflection frequently contradicts what physically happened in the pan (**Perception Gap**).
2. **Identical Visual Failures Have Different Latent Causes:** Three learners can produce the exact same pale, under-sautéed onions for three completely different cognitive reasons:
   - *Learner A (Moisture Blindness):* Does not understand that pooled liquid water pins pan temperature at 100°C and physically blocks Maillard browning (>140°C).
   - *Learner B (Heat Anxiety):* Confuses harmless white water vapor (steam) with burning smoke and turns the burner down too far.
   - *Learner C (Perception Blindspot):* Follows a 5-minute recipe timer and believes soft, translucent onions are "nicely browned."
3. **Cognitive Overload Destroys Transfer:** Telling a learner 4 mistakes at once prevents them from recalibrating their sensory judgment on their next attempt.

---

## 2. The 4-Stage Applied AI Cognitive Architecture

Instead of a single prompt, `SizzleSense AI` decomposes human learning coaching into four typed, inspectable stages enforced via `Pydantic v2` (`ai-backend/app/schemas/domain.py`) and Kotlin `@Serializable` models:

| Stage | Module | Input | Output Schema | Core Engineering Invariant |
| :--- | :--- | :--- | :--- | :--- |
| **Stage 1: Physical World Understanding** | `KitchenFrameAnalyzer` (CV) + `gemini-3.8-flash` | Overhead RTSP keyframes + acoustic sizzle signature + learner reflection | `PhysicalObservationReport` | Explicitly separates `observed_timeline_facts`, `inferred_physical_states`, `environmental_uncertainties`, and `perception_gap_level`. |
| **Stage 2: Learner State Estimation** | `LearnerStateEstimator` (`gemini-4-argon`) + `LearnerMemoryStore` | Stage 1 report + prior attempts (`Attempt 1 → 2 → 3`) | `LearnerStateEstimate` | Infers latent misconceptions and distinguishes **lucky timer execution (`PARTIAL_EXECUTION_ONLY`)** from verified sensory mastery (`CONCEPT_ADOPTED`). |
| **Stage 3: Expert Reasoning & Single-Issue Selection** | `ChefKnowledgeGraph` | Stage 2 state + Causal Prerequisite Graph (`causal_rank 1..5`) | `ExpertTeachingDecision` | **Pedagogical Restraint Validator:** Selects strictly **1** primary issue and requires non-empty `deliberately_omitted_issues` whenever multiple faults exist. |
| **Stage 4: Sensory Coaching Communication** | `SensoryCoachCommunicator` | Stage 3 decision | `SensoryCoachingOutput` + `FiveCoreQuestionsSummary` | Grounds feedback in **Visual**, **Auditory (Sizzle)**, and **Olfactory** cues; prohibits rigid timer rules. |

---

## 3. Evaluation Framework (`EvalBench`): Defining "Good" When There Is No Single Right Answer

Because culinary coaching has no single ground-truth string, `ai-backend/app/evals/human_learning_eval.py` evaluates the system across **10 multi-session trajectories** (3 learner archetypes × 3 attempts + 1 negative-control "lucky timer" trajectory) using 5 quantitative rubrics:

1. **Perception Gap Detection Recall (`perception_gap_detection_score`):** Accuracy in flagging contradictions between camera CV metrics and learner self-assessment.
2. **Latent Root-Cause Precision (`latent_root_cause_accuracy`):** Accuracy in distinguishing *Moisture Blindness*, *Heat Anxiety*, and *Perception Blindspot* given identical visual failure frames.
3. **Single-Issue Pedagogical Restraint (`single_issue_restraint_score`):** 100% enforcement of selecting 1 highest-priority causal bottleneck while logging suppressed secondary issues.
4. **Sensory Grounding & Anti-Pattern Penalty (`sensory_grounding_score`):** Rewards visual/auditory cue anchoring and actively penalizes (`-0.75`) prescriptive clock-timer instructions (`"cook for X minutes"`).
5. **Cross-Attempt Behavioral Transfer (`cross_attempt_transfer_score`):** Verifies longitudinal state tracking across Attempts `1 → 2 → 3`, including rejecting false-positive transfer when a learner gets a golden pan via a blind timer.
