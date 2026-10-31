"""Configuration for Google Gemini Frontier Models (`gemini-4-argon` and `gemini-3.8-flash`)."""

from __future__ import annotations

import os
from pydantic import BaseModel


class AiEngineConfig(BaseModel):
    """Runtime model configuration for SizzleSense AI."""

    default_reasoning_model: str = "gemini-4-argon"
    default_vision_model: str = "gemini-3.8-flash"
    gemini_api_base_url: str = "https://generativelanguage.googleapis.com/v1beta"

    @property
    def env_api_key(self) -> str | None:
        return os.environ.get("GEMINI_API_KEY") or os.environ.get("GOOGLE_API_KEY")


SETTINGS = AiEngineConfig()
