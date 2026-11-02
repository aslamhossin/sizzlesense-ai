"""Configuration for Google Gemini Multimodal & Reasoning Models (`gemini-3-flash-preview`)."""

from __future__ import annotations

import os
from pathlib import Path
from pydantic import BaseModel

ENV_FILE_PATH = Path(__file__).resolve().parent.parent / ".env"


def _read_dotenv_value(key: str) -> str | None:
    """Reads a key dynamically from `ai-backend/.env` so changes take effect immediately without restarting."""
    if not ENV_FILE_PATH.exists():
        return None
    for raw_line in ENV_FILE_PATH.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        k, v = line.split("=", 1)
        if k.strip() == key:
            cleaned = v.strip().strip('"').strip("'")
            return cleaned if cleaned else None
    return None


class AiEngineConfig(BaseModel):
    """Runtime model configuration for SizzleSense AI."""

    default_reasoning_model: str = "gemini-3-flash-preview"
    default_vision_model: str = "gemini-3-flash-preview"
    gemini_api_base_url: str = "https://generativelanguage.googleapis.com/v1beta"

    @property
    def env_api_key(self) -> str | None:
        key = (
            _read_dotenv_value("GEMINI_API_KEY")
            or _read_dotenv_value("GOOGLE_API_KEY")
            or os.environ.get("GEMINI_API_KEY")
            or os.environ.get("GOOGLE_API_KEY")
        )
        if key and key.lower() != "string":
            return key
        return None


SETTINGS = AiEngineConfig()
