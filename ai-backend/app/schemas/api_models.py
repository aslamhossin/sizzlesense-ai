"""Request and response Pydantic schemas for FastAPI REST endpoints."""

from __future__ import annotations

from typing import List
from pydantic import BaseModel

from app.schemas.domain import CognitiveCoachingResponse


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
    pedagogical_scenario_summary: str
    attempt1_diagnosis: CognitiveCoachingResponse


class ArchetypeComparisonResponse(BaseModel):
    shared_visual_input_description: str
    comparisons: List[ArchetypeComparisonItem]


class LearnerHistoryResponse(BaseModel):
    learner_id: str
    sessions: List[CognitiveCoachingResponse]
