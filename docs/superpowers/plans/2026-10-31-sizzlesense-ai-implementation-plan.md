# SizzleSense AI — Phase 1: Python Applied AI Backend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task in atomic steps, verifying with `pytest` and committing/pushing to `main` after each step.

**Goal:** Build the **SizzleSense AI Python Backend (`ai-backend/`)** implementing all 4 Applied AI Human Learning pillars from the Cookpad job description (Physical World Understanding with Computer Vision, Learner Latent State Estimation across Attempts 1→2→3, Expert Chef Causal Reasoning & Single-Issue Selection, and Automated Human Learning Evaluation Suite), ready for cloud deployment (Render / Cloud Run / Docker) and Android MVI client consumption.

**Architecture:** Modular Python service built with **FastAPI**, **Pydantic v2** (strict JSON schemas answering the 5 JD questions), **Pillow/NumPy Computer Vision** (extracting objective Maillard browning, moisture/steam index, and thermal uniformity from kitchen keyframes), **Google Gemini Frontier Models (`gemini-4-argon` + `gemini-3.8-flash`)** with a deterministic fallback engine, and an automated **`pytest` Human Learning Evaluation Suite**.

**Tech Stack:** Python 3.12+, `uv`, `FastAPI`, `Pydantic v2`, `Pillow`, `NumPy`, `httpx`, `pytest`.

**Spec:** `docs/superpowers/specs/2026-10-30-moment-sensei-applied-ai-design.md`

---

## Atomic Step Breakdown (Each Step = TDD + Verify + Git Commit & Push)

### Atomic Step 1: Git Init, Python Workspace (`uv`), & Strict Pydantic Domain Schemas
- **What you will learn (Android → Python mapping):** How Python's `Pydantic BaseModel` works just like Kotlin's `@Serializable data class`, enforcing strict types and validation for the 5 Core JD Questions and 4 Pillars.
- **Files:**
  - `.gitignore`
  - `ai-backend/pyproject.toml`
  - `ai-backend/app/__init__.py`
  - `ai-backend/app/schemas/domain.py`
  - `ai-backend/tests/test_schemas.py`
- **Verification:** `uv run pytest tests/test_schemas.py -v`

---

### Atomic Step 2: Computer Vision Kitchen Frame Analyzer (`vision/frame_analyzer.py`)
- **What you will learn:** How the `Computer Vision` tag on the JD works in practice—analyzing top-down kitchen camera frames (from your Tapo C100 RTSP stream or uploaded images) to compute objective physical metrics:
  1. `maillard_browning_ratio` (amber/golden-brown pixel ratio vs. raw white/translucent pixels)
  2. `surface_moisture_index` (specular reflection / pooling liquid indicator)
  3. `char_burn_ratio` (blackened carbonized pixel ratio)
  4. `TapoRtspUrlBuilder` with URL-encoded credentials (`@`, `:`, `/`)
- **Files:**
  - `ai-backend/app/vision/frame_analyzer.py`
  - `ai-backend/tests/test_frame_analyzer.py`
- **Verification:** `uv run pytest tests/test_frame_analyzer.py -v`

---

### Atomic Step 3: Chef Causal Knowledge Graph & The 3 JD "Sautéed Onion" Learner Archetypes (`engine/chef_knowledge.py`)
- **What you will learn:** How to encode world-class chefs' tacit knowledge into a **Causal Prerequisite Graph** (where *Moisture Evaporation* MUST precede *Maillard Browning*, which MUST precede *Glazing/Seasoning*) and how to model the 3 exact learner archetypes from the Cookpad JD (*Moisture Blindness*, *Heat Anxiety / Fear of Burning*, and *Perception Blindspot*).
- **Files:**
  - `ai-backend/app/engine/chef_knowledge.py`
  - `ai-backend/tests/test_chef_knowledge.py`
- **Verification:** `uv run pytest tests/test_chef_knowledge.py -v`

---

### Atomic Step 4: 4-Stage Applied AI Cognitive Pipeline (`gemini-4-argon` + `gemini-3.8-flash` + Deterministic Engine)
- **What you will learn:** How a Senior Principal Applied AI Engineer chains 4 specialized reasoning stages instead of one big prompt:
  - **Stage 1 (`PhysicalWorldObserver`):** Combines CV metrics + `gemini-3.8-flash` multimodal observation + learner reflection to separate `ObservedFacts` from `InferredStates` and flag `PerceptionGap`.
  - **Stage 2 (`LearnerStateEstimator`):** Uses `gemini-4-argon` to infer latent understandings, misconceptions, `decision_root_cause`, and longitudinal transfer across Attempts `1 → 2 → 3` (including `conceptual_shift_verified`).
  - **Stage 3 (`ExpertTeachingSelector`):** Applies the Chef Causal Graph to pick **strictly 1 primary issue** and explicitly log `deliberately_omitted_issues`.
  - **Stage 4 (`SensoryCoachCommunicator`):** Generates sensory-grounded coaching (Visual, Auditory Sizzle, Olfactory cues) and a micro-experiment for the next attempt.
- **Files:**
  - `ai-backend/app/config.py`
  - `ai-backend/app/engine/cognitive_pipeline.py`
  - `ai-backend/app/engine/learner_store.py`
  - `ai-backend/tests/test_cognitive_pipeline.py`
- **Verification:** `uv run pytest tests/test_cognitive_pipeline.py -v`

---

### Atomic Step 5: Automated Human Learning Evaluation Suite (`evals/human_learning_eval.py`)
- **What you will learn:** How to evaluate an AI system where "there is no single right answer"—running 3 learner personas across 3 attempts (9 sessions) and scoring:
  1. `perception_gap_detection_score`
  2. `latent_root_cause_accuracy`
  3. `single_issue_restraint_score`
  4. `sensory_grounding_score` (with anti-pattern penalty for rigid timer rules)
  5. `cross_attempt_transfer_score`
- **Files:**
  - `ai-backend/app/evals/human_learning_eval.py`
  - `ai-backend/tests/test_human_learning_evals.py`
- **Verification:** `uv run pytest tests/test_human_learning_evals.py -v`

---

### Atomic Step 6: FastAPI REST Endpoints, Deployment Config (`Dockerfile`, `render.yaml`) & `TECHNICAL_DIRECTION.md`
- **What you will learn:** How to expose the entire AI engine via clean FastAPI REST endpoints (`/api/v1/sessions/analyze`, `/api/v1/archetypes/compare`, `/api/v1/learners/{id}/history`, `/api/v1/evals/run`, `/api/v1/camera/tapo-rtsp-check`) with interactive Swagger docs (`/docs`) and containerization for 1-click cloud deployment.
- **Files:**
  - `ai-backend/app/main.py`
  - `ai-backend/tests/test_api.py`
  - `ai-backend/Dockerfile`
  - `render.yaml`
  - `TECHNICAL_DIRECTION.md`
  - `README.md`
- **Verification:** `uv run pytest -v`
