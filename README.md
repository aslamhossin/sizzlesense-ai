# SizzleSense AI — Applied AI Human Learning & Culinary Perception Coach

**Full-Stack Applied AI Engineering Portfolio (`Android Kotlin Strict MVI` + `TP-Link Tapo C100 RTSP Hardware` + `Python FastAPI & Computer Vision` + `Google Gemini 3 Multimodal Pipeline & EvalBench`)**

---

## 1. Overview & Motivation

Most culinary AI wrappers merely list surface-level mistakes or repeat recipe timers (*"cook for 5 more minutes"*). True culinary mastery is not about memorizing recipes—it is about **accurately observing dynamic physical states in the pan and making real-time sensory judgments**.

**SizzleSense AI** is an end-to-end Applied AI system built across **Hardware (Tapo C100 Overhead RTSP Camera)**, **Product Client (Android Jetpack Compose + Strict MVI)**, and **AI Backend (Python FastAPI + Computer Vision + Gemini 3 Structured Reasoning + Automated Evaluation Suite)** to solve four foundational challenges in human skill acquisition:

1. **Physical World Understanding (`Stage 1`):** Separates directly observed facts (visual Maillard browning ratio, moisture index, acoustic sizzle signature) from inferred physical states (pan surface temperature pinned at 100°C due to latent heat of vaporization) and explicitly detects **Perception Gaps** where a learner's self-assessment contradicts camera evidence.
2. **Learner State Estimation (`Stage 2`):** Infers latent understandings, misconceptions, and decision root causes across a **3-Attempt Mastery Loop (`Attempt 1 → 2 → 3`)**. Distinguishes three learners who all fail to sauté onions sufficiently (same pale visual outcome) for three distinct latent reasons:
   - **Learner A (`moisture_blindness`):** Does not know pooled water locks pan temperature at 100°C and blocks browning.
   - **Learner B (`heat_anxiety`):** Mistakes harmless white steam for burning smoke and dials the burner down too far.
   - **Learner C (`perception_blindspot`):** Follows a 5-minute clock timer and believes translucent steamed onions are browned.
3. **Expert Chef Reasoning & Pedagogical Restraint (`Stage 3`):** Encodes chef tacit knowledge as a **Causal Prerequisite Graph (`causal_rank 1..5`)** and enforces a strict guardrail: **select strictly ONE highest-leverage concept to teach right now**, while explicitly logging suppressed secondary issues (`deliberately_omitted_issues`) to prevent cognitive overload.
4. **Evaluation of Human Learning (`Stage 4` & `EvalBench`):** Evaluates both single-session diagnostic/coaching quality (penalizing rigid clock-timer rules in favor of visual, auditory sizzle, and aroma cues) and **cross-attempt behavioral transfer** (verifying whether the learner's sensory judgment actually changed in subsequent attempts, including rejecting "lucky timer" false positives).

---

## 2. Repository Structure

```text
.
├── ai-backend/                     # Python Applied AI Backend (FastAPI + Pydantic v2 + Pillow/NumPy CV + Pytest Evals)
│   ├── app/
│   │   ├── main.py                 # FastAPI REST endpoints & Swagger UI (/docs)
│   │   ├── schemas/domain.py       # Strict Pydantic schemas for the 4 Pillars & 5 Core Questions
│   │   ├── vision/frame_analyzer.py# Pixel CV analyzer & Tapo C100 RTSP live ffmpeg frame grabber
│   │   ├── engine/
│   │   │   ├── chef_knowledge.py   # Chef Causal Knowledge Graph & 3 JD Onion Learner Archetypes
│   │   │   ├── cognitive_pipeline.py # 4-Stage Cognitive Pipeline (Gemini 3 Live + Deterministic Engine)
│   │   │   └── learner_store.py    # Longitudinal Attempt 1->2->3 session memory store
│   │   └── evals/human_learning_eval.py # 10-Session Automated Human Learning Evaluation Suite
│   └── tests/                      # 22 Pytest unit & integration tests
├── app/                            # Android Client (Gradle 9.7.1, AGP 9.4.1, Kotlin 2.4.20, Compose BOM 2026.09.00)
│   ├── src/main/java/com/sizzlesense/ai/
│   │   ├── domain/model/           # Kotlinx.Serialization models & TapoRtspConfig
│   │   ├── data/camera/            # Media3 1.11.1 ExoPlayer RTSP live stream & keyframe grabber
│   │   ├── data/repository/        # Backend + On-Device Cognitive Repository
│   │   └── presentation/mvi/       # Strict MVI Contract, Pure Reducer, ViewModel & 4-Tab Compose UI
│   └── src/test/                   # JUnit tests for RTSP URL encoding & MVI Reducer
├── TECHNICAL_DIRECTION.md          # Senior Principal Architecture & Evaluation RFC
└── render.yaml                     # 1-Click Cloud Deployment Blueprint
```

---

## 3. Quick Start

### A. Run the Python AI Backend & Evaluation Suite
```bash
cd ai-backend
uv run pytest -v
uv run uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
# Open Interactive Swagger UI: http://127.0.0.1:8000/docs
```

### B. Build & Test the Android MVI App
```bash
./gradlew testDebugUnitTest assembleDebug
```
