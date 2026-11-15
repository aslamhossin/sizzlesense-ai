"""TP-Link Tapo C100 RTSP camera URL builder, live snapshot, and live coaching router."""

from __future__ import annotations

import base64
import os
from fastapi import APIRouter, Depends, HTTPException, Query, Response

from app.api.dependencies import get_cognitive_pipeline
from app.config import _read_dotenv_value
from app.engine.cognitive_pipeline import CognitiveCoachingPipeline
from app.schemas.api_models import (
    CameraDiscoveryResponse,
    TapoLiveAnalyzeRequest,
    TapoRtspRequest,
    TapoRtspResponse,
)
from app.schemas.domain import CognitiveCoachingResponse, SessionAnalyzeRequest
from app.vision.frame_analyzer import (
    TapoLanDiscovery,
    TapoRtspUrlBuilder,
    sanitize_rtsp_text,
)

router = APIRouter(prefix="/camera", tags=["Tapo C100 RTSP Camera"])


def _read_env_or_dotenv(key: str) -> str:
    return (_read_dotenv_value(key) or os.environ.get(key) or "").strip()


@router.get("/discover", response_model=CameraDiscoveryResponse)
async def discover_camera_and_server(
    preferred_ip: str | None = Query(default=None),
) -> CameraDiscoveryResponse:
    """Auto-discovers the active LAN backend URL and reachable Tapo RTSP camera IP."""
    lan_ip = TapoLanDiscovery.detect_local_lan_ip()
    configured_ip = _read_env_or_dotenv("TAPO_CAMERA_IP")
    has_creds = bool(
        _read_env_or_dotenv("TAPO_CAMERA_USERNAME")
        and _read_env_or_dotenv("TAPO_CAMERA_PASSWORD")
    )
    camera_ip, reachable, source = await TapoLanDiscovery.discover_tapo_camera(
        preferred_ip=preferred_ip,
        configured_ip=configured_ip,
        lan_ip=lan_ip,
        port=TapoLanDiscovery.RTSP_PORT,
    )
    return CameraDiscoveryResponse(
        backend_url=f"http://{lan_ip}:8000",
        camera_ip=camera_ip,
        camera_port=TapoLanDiscovery.RTSP_PORT,
        camera_reachable=reachable,
        has_backend_credentials=has_creds,
        discovery_source=source,
    )


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
    username: str | None = None,
    password: str | None = None,
    ip_address: str | None = None,
) -> Response:
    """Captures and displays a live JPEG frame directly from the Tapo C100 camera via RTSP."""
    resolved_user = (username or "").strip() or _read_env_or_dotenv("TAPO_CAMERA_USERNAME")
    resolved_pass = password or _read_env_or_dotenv("TAPO_CAMERA_PASSWORD")
    resolved_ip, _, _ = await TapoLanDiscovery.discover_tapo_camera(
        preferred_ip=ip_address,
        configured_ip=_read_env_or_dotenv("TAPO_CAMERA_IP"),
    )
    target_ip = resolved_ip or (ip_address or "").strip() or TapoRtspUrlBuilder.DEFAULT_TAPO_IP
    try:
        live_b64 = TapoRtspUrlBuilder.capture_live_frame_base64(
            ip_address=target_ip,
            username=resolved_user,
            password=resolved_pass,
            use_substream=False,
        )
        jpeg_bytes = base64.b64decode(live_b64)
        return Response(content=jpeg_bytes, media_type="image/jpeg")
    except Exception as exc:
        raise HTTPException(
            status_code=502,
            detail=f"Could not capture live snapshot from Tapo camera at {target_ip}: {sanitize_rtsp_text(str(exc))}",
        ) from exc


@router.post("/tapo-live-analyze", response_model=CognitiveCoachingResponse)
async def capture_from_tapo_and_analyze(
    req: TapoLiveAnalyzeRequest,
    pipeline: CognitiveCoachingPipeline = Depends(get_cognitive_pipeline),
) -> CognitiveCoachingResponse:
    """Captures a live frame from the discovered Tapo RTSP camera and runs the 4-stage AI pipeline."""
    resolved_user = req.username.strip() or _read_env_or_dotenv("TAPO_CAMERA_USERNAME")
    resolved_pass = req.password or _read_env_or_dotenv("TAPO_CAMERA_PASSWORD")
    discovered_ip, _, _ = await TapoLanDiscovery.discover_tapo_camera(
        preferred_ip=req.ip_address,
        configured_ip=_read_env_or_dotenv("TAPO_CAMERA_IP"),
    )
    target_ip = discovered_ip or req.ip_address.strip() or TapoRtspUrlBuilder.DEFAULT_TAPO_IP

    try:
        live_b64 = TapoRtspUrlBuilder.capture_live_frame_base64(
            ip_address=target_ip,
            username=resolved_user,
            password=resolved_pass,
            use_substream=True,
        )
    except Exception as exc:
        raise HTTPException(
            status_code=502,
            detail=f"Could not capture frame from Tapo camera at {target_ip}: {sanitize_rtsp_text(str(exc))}",
        ) from exc

    reflection = req.learner_reflection.strip() or (
        "I am cooking live under the Tapo C100 camera. Please analyze the pan state."
    )

    return await pipeline.analyze_session(
        SessionAnalyzeRequest(
            learner_id=req.learner_id,
            dish_id=req.dish_id,
            attempt_number=req.attempt_number,
            learner_reflection=reflection,
            archetype_hint=req.archetype_hint,
            keyframe_base64_list=[live_b64],
            audio_wav_base64=req.audio_wav_base64,
            gemini_api_key=req.gemini_api_key,
        )
    )
