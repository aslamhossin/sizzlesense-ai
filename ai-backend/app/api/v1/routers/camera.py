"""TP-Link Tapo C100 RTSP camera URL builder, live snapshot, and live coaching router."""

from __future__ import annotations

import base64
from fastapi import APIRouter, Depends, HTTPException, Response

from app.api.dependencies import get_cognitive_pipeline
from app.engine.cognitive_pipeline import CognitiveCoachingPipeline
from app.schemas.api_models import (
    TapoLiveAnalyzeRequest,
    TapoRtspRequest,
    TapoRtspResponse,
)
from app.schemas.domain import CognitiveCoachingResponse, SessionAnalyzeRequest
from app.vision.frame_analyzer import TapoRtspUrlBuilder

router = APIRouter(prefix="/camera", tags=["Tapo C100 RTSP Camera"])


@router.post("/tapo-rtsp-url", response_model=TapoRtspResponse)
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


@router.get(
    "/tapo-live-snapshot",
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


@router.post("/tapo-live-analyze", response_model=CognitiveCoachingResponse)
async def capture_from_tapo_and_analyze(
    req: TapoLiveAnalyzeRequest,
    pipeline: CognitiveCoachingPipeline = Depends(get_cognitive_pipeline),
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

    return await pipeline.analyze_session(
        SessionAnalyzeRequest(
            learner_id=req.learner_id,
            dish_id=req.dish_id,
            attempt_number=req.attempt_number,
            learner_reflection=req.learner_reflection,
            keyframe_base64_list=[live_b64],
            gemini_api_key=req.gemini_api_key,
        )
    )
