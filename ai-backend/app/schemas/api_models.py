"""Request and response Pydantic schemas for FastAPI REST endpoints."""

from __future__ import annotations

from typing import List, Optional
from pydantic import BaseModel, Field

from app.schemas.domain import CognitiveCoachingResponse


class TapoRtspRequest(BaseModel):
    ip_address: str = "192.168.11.16"
    username: str
    password: str
    use_substream: bool = False
    port: int = 554


class TapoLiveAnalyzeRequest(BaseModel):
    ip_address: str = ""
    username: str = ""
    password: str = ""
    learner_id: str = "tapo_live_learner"
    dish_id: str = "sauteed_onions"
    attempt_number: int = Field(default=1, ge=1, le=3)
    learner_reflection: str = (
        "I am cooking live under the Tapo C100 camera. Please analyze the pan state."
    )
    archetype_hint: Optional[str] = None
    audio_wav_base64: Optional[str] = None
    gemini_api_key: str | None = None


class TapoRtspResponse(BaseModel):
    rtsp_url: str
    stream_profile: str


class CameraDiscoveryResponse(BaseModel):
    """Auto-discovered LAN backend and Tapo camera metadata (never exposes secrets)."""

    backend_url: str
    camera_ip: str | None = None
    camera_port: int = 554
    camera_reachable: bool = False
    has_backend_credentials: bool = False
    discovery_source: str = "none"


class ArchetypeComparisonItem(BaseModel):
    archetype_id: str
    display_name: str
    pedagogical_scenario_summary: str
    attempt1_diagnosis: CognitiveCoachingResponse


class ArchetypeComparisonResponse(BaseModel):
    shared_visual_input_description: str
    comparisons: List[ArchetypeComparisonItem]


class LearnerHistoryResponse(BaseModel):
    learner_id: str
    sessions: List[CognitiveCoachingResponse]
