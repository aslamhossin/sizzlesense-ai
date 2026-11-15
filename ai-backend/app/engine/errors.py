"""Typed HTTP-aware domain errors and secret-redaction helpers for SizzleSense AI."""

from __future__ import annotations

import re
from fastapi import HTTPException

_RTSP_CREDS_RE = re.compile(r"rtsp://[^@\s]+@", re.IGNORECASE)
_GOOGLE_KEY_RE = re.compile(r"AIza[0-9A-Za-z_-]{8,}")
_QUERY_KEY_RE = re.compile(r"([?&]key=)[^&\s\"']+", re.IGNORECASE)


def sanitize_error_text(text: str) -> str:
    """Redacts embedded RTSP credentials or API keys from error messages before surfacing."""
    cleaned = _RTSP_CREDS_RE.sub("rtsp://***:***@", text or "")
    cleaned = _GOOGLE_KEY_RE.sub("AIza***", cleaned)
    cleaned = _QUERY_KEY_RE.sub(r"\1***", cleaned)
    return cleaned.strip()


class PipelineError(HTTPException):
    """Base error raised by the cognitive pipeline with an explicit HTTP status code."""

    def __init__(self, status_code: int, detail: str) -> None:
        super().__init__(status_code=status_code, detail=sanitize_error_text(detail))


class MissingGeminiApiKeyError(PipelineError):
    """Raised when no valid Gemini API key is configured."""

    def __init__(
        self,
        detail: str = (
            "Gemini API key is not configured on the server (GEMINI_API_KEY). "
            "Configure a valid API key to run live AI coaching."
        ),
    ) -> None:
        super().__init__(status_code=503, detail=detail)


class GeminiQuotaExceededError(PipelineError):
    """Raised when Gemini API rejects requests due to quota/rate limits or overload."""

    def __init__(self, detail: str) -> None:
        super().__init__(status_code=503, detail=detail)


class GeminiUpstreamError(PipelineError):
    """Raised when Gemini API authentication, model lookup, transport, or JSON validation fails."""

    def __init__(self, detail: str) -> None:
        super().__init__(status_code=502, detail=detail)


class NoActiveCookingSceneError(PipelineError):
    """Raised when a submitted camera frame shows no pan/pot or active cooking on the stove."""

    def __init__(self, observation: str) -> None:
        cleaned = sanitize_error_text(observation) or "empty stove or non-cooking scene"
        super().__init__(
            status_code=422,
            detail=(
                f"No pan or active cooking detected on the stove ({cleaned}). "
                "Place a pan on the burner and try again."
            ),
        )


class InvalidMediaInputError(PipelineError):
    """Raised when uploaded base64 JPEG frames or WAV audio cannot be decoded cleanly."""

    def __init__(self, detail: str) -> None:
        super().__init__(status_code=422, detail=detail)
