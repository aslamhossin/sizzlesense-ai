"""Structured Google Gemini API Client enforcing Pydantic v2 `responseJsonSchema` outputs."""

from __future__ import annotations

import json
import httpx

from app.config import SETTINGS
from app.schemas.domain import (
    AcousticSpectralMetrics,
    CognitiveCoachingResponse,
    ComputerVisionMetrics,
    SessionAnalyzeRequest,
)


class GeminiStructuredClient:
    """
    Executes multimodal Gemini API requests with structured JSON schema enforcement
    and automatic model tier failover (`gemini-3-flash-preview` -> `gemini-2.5-flash`).
    """

    SYSTEM_INSTRUCTION = (
        "You are the Applied AI Human Learning Engine for SizzleSense AI. "
        "Inspect the attached kitchen camera image and the learner's reflection. "
        "Return ONLY a single valid JSON object matching the CognitiveCoachingResponse schema "
        "(`session_id`, `learner_id`, `dish_id`, `attempt_number`, `reasoning_model_used`, `vision_model_used`, "
        "`five_core_questions`, `stage1_physical_observation`, `stage2_learner_state`, `stage3_teaching_decision`, "
        "`stage4_sensory_coaching`). "
        "Update `observed_timeline_facts[0].visual_observation`, `inferred_physical_states`, "
        "`five_core_questions`, and `stage4_sensory_coaching.coaching_message` based on what you actually "
        "see in the attached camera image and learner reflection, while keeping at least 1 item in `deliberately_omitted_issues`."
    )

    @classmethod
    def resolve_candidate_models(cls, preferred_model: str | None) -> list[str]:
        preferred = preferred_model or SETTINGS.default_reasoning_model
        return list(
            dict.fromkeys(
                [
                    "gemini-3-flash-preview",
                    "gemini-3.1-pro-preview",
                    preferred,
                    "gemini-2.5-pro",
                    "gemini-2.5-flash",
                ]
            )
        )

    @classmethod
    async def generate_structured_coaching(
        cls,
        request: SessionAnalyzeRequest,
        cv_metrics: ComputerVisionMetrics,
        baseline: CognitiveCoachingResponse,
        api_key: str,
        acoustic_metrics: AcousticSpectralMetrics | None = None,
    ) -> CognitiveCoachingResponse:
        candidate_models = cls.resolve_candidate_models(
            request.preferred_reasoning_model
        )
        acoustic_line = (
            f"Acoustic FFT Metrics: {json.dumps(acoustic_metrics.model_dump())}\n"
            if acoustic_metrics
            else ""
        )
        parts: list[dict] = [
            {
                "text": (
                    f"{cls.SYSTEM_INSTRUCTION}\n\n"
                    f"Learner Reflection: {request.learner_reflection}\n"
                    f"Computer Vision Metrics: {json.dumps(cv_metrics.model_dump())}\n"
                    f"{acoustic_line}"
                    f"Baseline Reference JSON:\n{json.dumps(baseline.model_dump())}"
                )
            }
        ]

        if request.keyframe_base64_list:
            clean_b64 = request.keyframe_base64_list[-1].split(",")[-1].strip()
            parts.append(
                {
                    "inlineData": {
                        "mimeType": "image/jpeg",
                        "data": clean_b64,
                    }
                }
            )

        if request.audio_wav_base64:
            clean_audio_b64 = request.audio_wav_base64.split(",")[-1].strip()
            parts.append(
                {
                    "inlineData": {
                        "mimeType": "audio/wav",
                        "data": clean_audio_b64,
                    }
                }
            )

        payload = {
            "contents": [{"parts": parts}],
            "generationConfig": {
                "responseMimeType": "application/json",
                "responseJsonSchema": CognitiveCoachingResponse.model_json_schema(),
            },
        }

        last_err: Exception | None = None
        async with httpx.AsyncClient(timeout=30.0) as client:
            for model_id in candidate_models:
                url = f"{SETTINGS.gemini_api_base_url}/models/{model_id}:generateContent"
                try:
                    resp = await client.post(
                        url,
                        headers={
                            "x-goog-api-key": api_key,
                            "Content-Type": "application/json",
                        },
                        json=payload,
                    )
                    if resp.status_code != 200:
                        # Retry without responseJsonSchema if model endpoint rejects complex $defs schema
                        fallback_payload = {
                            "contents": [{"parts": parts}],
                            "generationConfig": {
                                "responseMimeType": "application/json",
                            },
                        }
                        resp = await client.post(
                            url,
                            headers={
                                "x-goog-api-key": api_key,
                                "Content-Type": "application/json",
                            },
                            json=fallback_payload,
                        )
                        if resp.status_code != 200:
                            continue

                    data = resp.json()
                    text_json = data["candidates"][0]["content"]["parts"][0]["text"]
                    raw_obj = json.loads(text_json)
                    if isinstance(raw_obj, dict) and "baseline_reference" in raw_obj:
                        raw_obj = raw_obj["baseline_reference"]
                    parsed = CognitiveCoachingResponse.model_validate(raw_obj)
                    parsed.reasoning_model_used = f"{model_id} (Live Gemini API)"
                    return parsed
                except Exception as exc:
                    last_err = exc
                    continue

        if last_err:
            raise last_err
        raise RuntimeError("No Gemini model endpoint succeeded.")
