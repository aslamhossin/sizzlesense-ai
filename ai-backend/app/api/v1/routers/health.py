"""Health & readiness check router (`/api/v1/health`)."""

from __future__ import annotations

from fastapi import APIRouter

from app.config import SETTINGS
from app.engine.chef_knowledge import CHEF_PRINCIPLES_REGISTRY, ONION_LEARNER_ARCHETYPES

router = APIRouter(tags=["System Health"])


@router.get("/health")
async def get_health() -> dict:
    """Returns server status and configured Google Gemini models."""
    return {
        "status": "healthy",
        "service": "sizzlesense-ai-backend",
        "default_reasoning_model": SETTINGS.default_reasoning_model,
        "default_vision_model": SETTINGS.default_vision_model,
        "chef_principles_loaded": len(CHEF_PRINCIPLES_REGISTRY),
        "learner_archetypes_loaded": list(ONION_LEARNER_ARCHETYPES.keys()),
    }
