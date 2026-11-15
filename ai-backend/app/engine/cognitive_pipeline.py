"""4-Stage Applied AI Cognitive Pipeline powered strictly by live Google Gemini reasoning."""

from __future__ import annotations

import binascii
import uuid
import wave
from typing import Optional

from PIL import UnidentifiedImageError

from app.audio.sizzle_analyzer import AcousticSizzleAnalyzer
from app.config import SETTINGS
from app.engine.chef_knowledge import ChefKnowledgeGraph
from app.engine.errors import (
    InvalidMediaInputError,
    MissingGeminiApiKeyError,
    NoActiveCookingSceneError,
)
from app.engine.gemini_client import GeminiStructuredClient
from app.engine.learner_store import LearnerMemoryStore
from app.schemas.domain import (
    AcousticSpectralMetrics,
    CognitiveCoachingResponse,
    ComputerVisionMetrics,
    SessionAnalyzeRequest,
)
from app.vision.frame_analyzer import KitchenFrameAnalyzer


class CognitiveCoachingPipeline:
    """
    Orchestrates the 4-Stage Applied AI Human Learning Pipeline with zero silent fallbacks:
      1. Validate API key & decode optional camera frames (CV metrics) / WAV audio (FFT metrics)
      2. If camera frames are present, run Gemini active-pan/active-cooking scene gate (422 if empty stove)
      3. Retrieve longitudinal prior attempt from SQLite LearnerMemoryStore
      4. Execute live multimodal Gemini reasoning across Stages 1-4 & 5 Core Pedagogical Questions
      5. Enforce causal restraint via ChefKnowledgeGraph, pin measured DSP telemetry, and persist session
    """

    def __init__(self, memory_store: Optional[LearnerMemoryStore] = None) -> None:
        self.memory_store = memory_store or LearnerMemoryStore()

    @staticmethod
    def _resolve_active_api_key(request: SessionAnalyzeRequest) -> str:
        """Resolves a non-placeholder Gemini API key or raises HTTP 503."""
        req_key = (
            request.gemini_api_key.strip()
            if request.gemini_api_key
            and request.gemini_api_key.strip().lower() != "string"
            else None
        )
        api_key = req_key or SETTINGS.env_api_key
        if not api_key:
            raise MissingGeminiApiKeyError()
        return api_key

    @staticmethod
    def _extract_cv_metrics(frames: list[str]) -> Optional[ComputerVisionMetrics]:
        if not frames:
            return None
        try:
            return KitchenFrameAnalyzer.analyze_frame_sequence(frames)
        except (ValueError, binascii.Error, UnidentifiedImageError, OSError) as exc:
            raise InvalidMediaInputError(f"Invalid camera frame payload: {exc}") from exc

    @staticmethod
    def _extract_acoustic_metrics(
        audio_wav_base64: Optional[str],
    ) -> Optional[AcousticSpectralMetrics]:
        if not audio_wav_base64 or not audio_wav_base64.strip():
            return None
        try:
            return AcousticSizzleAnalyzer.analyze_wav_base64(audio_wav_base64)
        except (ValueError, binascii.Error, wave.Error, EOFError, OSError) as exc:
            raise InvalidMediaInputError(f"Invalid WAV audio payload: {exc}") from exc

    @staticmethod
    def _pin_authoritative_fields(
        *,
        response: CognitiveCoachingResponse,
        request: SessionAnalyzeRequest,
        model_id: str,
        cv_metrics: Optional[ComputerVisionMetrics],
        acoustic_metrics: Optional[AcousticSpectralMetrics],
    ) -> CognitiveCoachingResponse:
        """Pins measured telemetry, model identity, and Chef Knowledge Graph causal restraint."""
        response.session_id = f"sess_{uuid.uuid4().hex[:8]}"
        response.learner_id = request.learner_id
        response.dish_id = request.dish_id
        response.attempt_number = request.attempt_number
        response.reasoning_model_used = f"{model_id} (Live Gemini API)"

        if cv_metrics is None:
            response.vision_model_used = "No Camera Frame"
        elif acoustic_metrics is not None:
            response.vision_model_used = f"{model_id} + CV-Pixel + Audio-FFT"
        else:
            response.vision_model_used = f"{model_id} + CV-Pixel-Analyzer"

        response.stage1_physical_observation.cv_metrics = cv_metrics
        response.stage1_physical_observation.acoustic_metrics = acoustic_metrics
        response.stage1_physical_observation.learner_stated_belief = (
            request.learner_reflection
        )
        response.stage3_teaching_decision = ChefKnowledgeGraph.enforce_causal_restraint(
            decision=response.stage3_teaching_decision,
            conceptual_shift_verified=response.stage2_learner_state.conceptual_shift_verified,
        )
        return response

    async def analyze_session(
        self, request: SessionAnalyzeRequest
    ) -> CognitiveCoachingResponse:
        api_key = self._resolve_active_api_key(request)
        cv_metrics = self._extract_cv_metrics(request.keyframe_base64_list)
        acoustic_metrics = self._extract_acoustic_metrics(request.audio_wav_base64)

        if request.keyframe_base64_list:
            scene_check, _ = await GeminiStructuredClient.verify_active_cooking_scene(
                frames=request.keyframe_base64_list,
                api_key=api_key,
                preferred_model=request.preferred_vision_model,
            )
            if not (scene_check.has_cooking_vessel and scene_check.active_cooking_present):
                raise NoActiveCookingSceneError(scene_check.scene_observation)

        prior_session = self.memory_store.get_previous_attempt(
            request.learner_id, request.dish_id, request.attempt_number
        )

        raw_response, model_id = (
            await GeminiStructuredClient.generate_structured_coaching(
                request=request,
                cv_metrics=cv_metrics,
                acoustic_metrics=acoustic_metrics,
                prior_session=prior_session,
                api_key=api_key,
            )
        )

        pinned = self._pin_authoritative_fields(
            response=raw_response,
            request=request,
            model_id=model_id,
            cv_metrics=cv_metrics,
            acoustic_metrics=acoustic_metrics,
        )
        self.memory_store.save_session(request.learner_id, pinned)
        return pinned
