"""FastAPI REST Server for SizzleSense AI — Applied AI Human Learning & Culinary Perception Engine."""

from __future__ import annotations

import base64
from typing import List
from fastapi import FastAPI, HTTPException, Response
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

from app.config import SETTINGS
from app.engine.chef_knowledge import CHEF_PRINCIPLES_REGISTRY, JD_ONION_ARCHETYPES
from app.engine.cognitive_pipeline import CognitiveCoachingPipeline
from app.engine.learner_store import LearnerMemoryStore
from app.evals.human_learning_eval import EvalSuiteReport, HumanLearningEvalRunner
from app.schemas.domain import CognitiveCoachingResponse, SessionAnalyzeRequest
from app.vision.frame_analyzer import (
    TapoRtspUrlBuilder,
    generate_synthetic_kitchen_frame_base64,
)

app = FastAPI(
    title="SizzleSense AI — Applied AI Human Learning Engine",
    description=(
        "4-Stage Multimodal Culinary Coaching & Human Learning Evaluation API "
        "(Physical World Understanding -> Learner Latent State -> Single-Issue Chef Selection -> Sensory Coaching)."
    ),
    version="1.0.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

GLOBAL_MEMORY_STORE = LearnerMemoryStore()
GLOBAL_PIPELINE = CognitiveCoachingPipeline(memory_store=GLOBAL_MEMORY_STORE)
GLOBAL_EVAL_RUNNER = HumanLearningEvalRunner()


class TapoRtspRequest(BaseModel):
    ip_address: str = "192.168.11.16"
    username: str
    password: str
    use_substream: bool = False
    port: int = 554


class TapoLiveAnalyzeRequest(BaseModel):
    ip_address: str = "192.168.11.16"
    username: str
    password: str
    learner_id: str = "tapo_live_learner"
    dish_id: str = "sauteed_onions"
    attempt_number: int = 1
    learner_reflection: str = (
        "I am cooking live under the Tapo C100 camera. Please analyze the pan state."
    )
    gemini_api_key: str | None = None


class TapoRtspResponse(BaseModel):
    rtsp_url: str
    stream_profile: str


class ArchetypeComparisonItem(BaseModel):
    archetype_id: str
    display_name: str
    jd_quote_alignment: str
    attempt1_diagnosis: CognitiveCoachingResponse


class ArchetypeComparisonResponse(BaseModel):
    shared_visual_input_description: str
    comparisons: List[ArchetypeComparisonItem]


class LearnerHistoryResponse(BaseModel):
    learner_id: str
    sessions: List[CognitiveCoachingResponse]


@app.get("/api/v1/health")
async def get_health() -> dict:
    """Returns server status and configured Google Gemini Frontier models."""
    return {
        "status": "healthy",
        "service": "sizzlesense-ai-backend",
        "default_reasoning_model": SETTINGS.default_reasoning_model,
        "default_vision_model": SETTINGS.default_vision_model,
        "chef_principles_loaded": len(CHEF_PRINCIPLES_REGISTRY),
        "jd_archetypes_loaded": list(JD_ONION_ARCHETYPES.keys()),
    }


@app.post("/api/v1/sessions/analyze", response_model=CognitiveCoachingResponse)
async def analyze_cooking_session(
    request: SessionAnalyzeRequest,
) -> CognitiveCoachingResponse:
    """Runs the 4-stage Applied AI cognitive pipeline on a kitchen session."""
    return await GLOBAL_PIPELINE.analyze_session(request)


@app.get(
    "/api/v1/learners/{learner_id}/history", response_model=LearnerHistoryResponse
)
async def get_learner_history(learner_id: str) -> LearnerHistoryResponse:
    """Returns longitudinal session history (Attempt 1 -> 2 -> 3) for a learner."""
    sessions = GLOBAL_MEMORY_STORE.get_history(learner_id)
    return LearnerHistoryResponse(learner_id=learner_id, sessions=sessions)


@app.get(
    "/api/v1/archetypes/compare-onion-learners",
    response_model=ArchetypeComparisonResponse,
)
async def compare_three_jd_onion_learners() -> ArchetypeComparisonResponse:
    """
    Demonstrates the core Cookpad JD scenario:
    3 learners all fail to sauté onions sufficiently (identical pale visual frame),
    but receive 3 distinct latent diagnoses and single-issue coaching interventions.
    """
    temp_pipeline = CognitiveCoachingPipeline(memory_store=LearnerMemoryStore())
    pale_frame = generate_synthetic_kitchen_frame_base64("pale_steaming")
    items: List[ArchetypeComparisonItem] = []

    for archetype_id, archetype in JD_ONION_ARCHETYPES.items():
        preset = archetype.attempts[1]
        diagnosis = await temp_pipeline.analyze_session(
            SessionAnalyzeRequest(
                learner_id=f"compare_{archetype_id}",
                dish_id="sauteed_onions",
                attempt_number=1,
                learner_reflection=preset.learner_reflection,
                archetype_hint=archetype_id,
                keyframe_base64_list=[pale_frame],
                acoustic_hint=preset.acoustic_signature,
            )
        )
        items.append(
            ArchetypeComparisonItem(
                archetype_id=archetype_id,
                display_name=archetype.display_name,
                jd_quote_alignment=archetype.jd_quote_alignment,
                attempt1_diagnosis=diagnosis,
            )
        )

    return ArchetypeComparisonResponse(
        shared_visual_input_description=(
            "Identical overhead frame: pale translucent onions pooling in liquid (Maillard browning ratio < 0.20)."
        ),
        comparisons=items,
    )


@app.post("/api/v1/evals/run", response_model=EvalSuiteReport)
async def run_human_learning_eval_suite() -> EvalSuiteReport:
    """Executes the 10-session Automated Human Learning Evaluation Benchmark (`EvalBench`)."""
    return await GLOBAL_EVAL_RUNNER.run_full_benchmark()


@app.post("/api/v1/camera/tapo-rtsp-url", response_model=TapoRtspResponse)
async def build_tapo_rtsp_url(req: TapoRtspRequest) -> TapoRtspResponse:
    """Builds a safely URL-encoded RTSP stream URI for TP-Link Tapo C100 / C110 / C200 cameras."""
    try:
        url = TapoRtspUrlBuilder.build_url(
            ip_address=req.ip_address,
            username=req.username,
            password=req.password,
            use_substream=req.use_substream,
            port=req.port,
        )
        profile = (
            "360p Sub-Stream (Low-Latency AI Frame Sampling)"
            if req.use_substream
            else "1080p Full HD Main Stream"
        )
        return TapoRtspResponse(rtsp_url=url, stream_profile=profile)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc


@app.get(
    "/api/v1/camera/tapo-live-snapshot",
    responses={200: {"content": {"image/jpeg": {}}}},
)
async def get_tapo_live_snapshot(
    username: str,
    password: str,
    ip_address: str = "192.168.11.16",
) -> Response:
    """Captures and displays a live JPEG frame directly from the Tapo C100 camera (`192.168.11.16:554`) in Swagger UI."""
    try:
        live_b64 = TapoRtspUrlBuilder.capture_live_frame_base64(
            ip_address=ip_address,
            username=username,
            password=password,
            use_substream=False,
        )
        jpeg_bytes = base64.b64decode(live_b64)
        return Response(content=jpeg_bytes, media_type="image/jpeg")
    except Exception as exc:
        raise HTTPException(
            status_code=502,
            detail=f"Could not capture live snapshot from Tapo camera at {ip_address}: {exc}",
        ) from exc


@app.post("/api/v1/camera/tapo-live-analyze", response_model=CognitiveCoachingResponse)
async def capture_from_tapo_and_analyze(
    req: TapoLiveAnalyzeRequest,
) -> CognitiveCoachingResponse:
    """Captures a real live frame from the Tapo C100 camera (`192.168.11.16:554`) via RTSP and runs the 4-stage AI pipeline."""
    try:
        live_b64 = TapoRtspUrlBuilder.capture_live_frame_base64(
            ip_address=req.ip_address,
            username=req.username,
            password=req.password,
            use_substream=True,
        )
    except Exception as exc:
        raise HTTPException(
            status_code=502,
            detail=f"Could not capture frame from Tapo camera at {req.ip_address}: {exc}",
        ) from exc

    return await GLOBAL_PIPELINE.analyze_session(
        SessionAnalyzeRequest(
            learner_id=req.learner_id,
            dish_id=req.dish_id,
            attempt_number=req.attempt_number,
            learner_reflection=req.learner_reflection,
            keyframe_base64_list=[live_b64],
            gemini_api_key=req.gemini_api_key,
        )
    )
