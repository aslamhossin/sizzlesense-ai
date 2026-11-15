"""Structured Google Gemini API Client enforcing Pydantic v2 `responseJsonSchema` outputs."""

from __future__ import annotations

import json
import re
from typing import Optional, Type, TypeVar

import httpx
from pydantic import BaseModel, Field

from app.config import SETTINGS
from app.engine.chef_knowledge import ChefKnowledgeGraph
from app.engine.errors import (
    GeminiQuotaExceededError,
    GeminiUpstreamError,
    PipelineError,
    sanitize_error_text,
)
from app.schemas.domain import (
    AcousticSpectralMetrics,
    CognitiveCoachingResponse,
    ComputerVisionMetrics,
    SessionAnalyzeRequest,
)

SchemaT = TypeVar("SchemaT", bound=BaseModel)

_CODE_FENCE_RE = re.compile(r"^```(?:json)?\s*|\s*```$", re.IGNORECASE)


class ActiveCookingSceneCheck(BaseModel):
    """Structured preflight check confirming a pan/pot and active cooking exist in the frame."""

    has_cooking_vessel: bool = Field(
        ...,
        description="True only if a pan, skillet, pot, wok, or cooking vessel is visible on a stove/cooktop.",
    )
    active_cooking_present: bool = Field(
        ...,
        description="True only if food, oil, or active heat cooking is present (false for empty burners, sinks, counters, or empty rooms).",
    )
    scene_observation: str = Field(
        ...,
        description="One concise factual sentence describing what is visible in the frame.",
    )


class GeminiStructuredClient:
    """
    Executes multimodal Gemini API requests with structured JSON schema enforcement,
    active-cooking scene verification, and clean fail-fast error handling.
    """

    SCENE_GATE_INSTRUCTION = (
        "Inspect this kitchen camera image strictly as a physical scene validator. "
        "Determine whether BOTH: (1) a cooking vessel (pan, skillet, pot, or wok) is present on a stove/burner, "
        "AND (2) active cooking or food/oil cooking is occurring in the pan. "
        "Return has_cooking_vessel=false or active_cooking_present=false if the frame shows an empty stovetop/burners, "
        "empty countertop, sink, wall, person without a stove/pan, or an empty unused pan. "
        "Respond ONLY with valid JSON matching ActiveCookingSceneCheck."
    )

    COACHING_SYSTEM_INSTRUCTION = (
        "You are the Applied AI Human Learning Engine for SizzleSense AI. "
        "Perform first-principles multimodal coaching grounded in whatever camera frame(s), "
        "pan audio WAV, Computer Vision pixel metrics, Acoustic FFT spectral metrics, "
        "Chef Causal Knowledge Graph principles, longitudinal prior-attempt history, and learner reflection are provided.\n\n"
        "STRICT RULES:\n"
        "1. Stage 1 (Physical World Understanding): Separate directly observed timeline facts from inferred physical states "
        "and environmental uncertainties. Reconcile visual evidence, CV metrics, Audio FFT metrics, and learner belief. "
        "If audio and visual telemetry contradict each other OR the learner's stated belief contradicts physical evidence "
        "(e.g. scorched pan claimed golden, pale/bubbling pan claimed browned, or cooking blind by a timer without looking), "
        "explicitly classify perception_gap_level as MODERATE_DRIFT or CRITICAL_BLINDSPOT and explain the discrepancy. "
        "If no camera frame was provided (`cv_metrics` is None), set `stage1_physical_observation.cv_metrics` to null.\n"
        "2. Stage 2 (Learner Latent State & Longitudinal Transfer): Infer `learner_archetype_id` "
        "(primary culinary archetypes: `moisture_blindness`, `heat_anxiety`, `perception_blindspot`, or a concise accurate slug if distinct), "
        "demonstrated understandings, latent misconceptions, and decision root cause. "
        "Maintain longitudinal continuity of `learner_archetype_id` from `prior_session` unless a genuinely different new bottleneck occurred.\n"
        "   - Attempt 1 or no prior session -> `transfer_status = INITIAL_BASELINE`, `conceptual_shift_verified = false`.\n"
        "   - Attempt 2 or 3 with prior session:\n"
        "     * `CONCEPT_ADOPTED` (`conceptual_shift_verified = true`) ONLY when BOTH physical pan outcome AND learner reflection show sensory-grounded judgment.\n"
        "     * `PARTIAL_EXECUTION_ONLY` (`conceptual_shift_verified = false`) when pan outcome improved/succeeded BUT learner credited a blind timer, guessing, or luck without sensory observation.\n"
        "     * `REGRESSED_OR_OVERCORRECTED` (`conceptual_shift_verified = false`) when pan outcome failed, stayed undercooked/watery, went cold, or scorched/burned.\n"
        "3. Stage 3 (Single-Issue Pedagogical Restraint): Identify at least 2 genuinely relevant `candidate_issues_detected`, "
        "select strictly ONE highest-leverage `selected_primary_issue` (referencing the Chef Knowledge Graph `target_chef_principle_id` "
        "with lowest unresolved `causal_rank` when the primary bottleneck is unsolved), and document EVERY non-selected candidate "
        "inside `deliberately_omitted_issues`.\n"
        "4. Stage 4 & Five Core Questions: Deliver sensory coaching anchored on visual, auditory, and olfactory/tactile cues "
        "tailored to `dish_id` and the real pan state. Never prescribe rigid minute-based rules like 'cook for X minutes'.\n"
        "Return ONLY a single valid JSON object matching the CognitiveCoachingResponse schema."
    )

    @classmethod
    def resolve_candidate_models(cls, preferred_model: str | None) -> list[str]:
        preferred = (preferred_model or "").strip()
        candidates: list[str] = []
        if preferred and preferred.lower() != "string":
            candidates.append(preferred)
        candidates.extend(["gemini-3-flash-preview", "gemini-3.1-pro-preview"])
        return list(dict.fromkeys(candidates))

    @staticmethod
    def _clean_b64(raw_b64: str) -> str:
        return re.sub(r"\s+", "", raw_b64.split(",")[-1])

    @classmethod
    def _select_keyframes_for_gemini(cls, frames: list[str]) -> list[str]:
        if len(frames) <= 3:
            return frames
        return [frames[0], frames[len(frames) // 2], frames[-1]]

    @staticmethod
    def _extract_error_detail(resp: httpx.Response) -> tuple[str, str]:
        """Returns sanitized `(error_status, error_message)` from a Gemini HTTP error response."""
        try:
            body = resp.json()
            err = body.get("error", {}) if isinstance(body, dict) else {}
            status = str(err.get("status") or "")
            msg = str(err.get("message") or resp.text[:300])
        except Exception:
            status = ""
            msg = resp.text[:300]
        return status, sanitize_error_text(msg)[:300]

    @staticmethod
    def _is_schema_rejection(status: str, message: str) -> bool:
        combined = f"{status} {message}".lower()
        return any(
            token in combined
            for token in ("schema", "responsejsonschema", "invalid_argument")
        )

    @classmethod
    def _parse_structured_response(
        cls,
        resp: httpx.Response,
        schema_cls: Type[SchemaT],
    ) -> SchemaT:
        data = resp.json()
        candidates = data.get("candidates") if isinstance(data, dict) else None
        if not candidates:
            raise ValueError("Gemini response contained no candidates.")
        parts = candidates[0].get("content", {}).get("parts", [])
        text_parts = [p.get("text", "") for p in parts if isinstance(p, dict) and p.get("text")]
        if not text_parts:
            finish = candidates[0].get("finishReason", "UNKNOWN")
            raise ValueError(f"Gemini candidate contained no text (finishReason={finish}).")
        cleaned = _CODE_FENCE_RE.sub("", "\n".join(text_parts).strip()).strip()
        raw_obj = json.loads(cleaned)
        if isinstance(raw_obj, dict) and "baseline_reference" in raw_obj:
            raw_obj = raw_obj["baseline_reference"]
        return schema_cls.model_validate(raw_obj)

    @classmethod
    async def _request_structured_json(
        cls,
        *,
        parts: list[dict],
        schema_cls: Type[SchemaT],
        candidate_models: list[str],
        api_key: str,
        timeout_sec: float = 45.0,
    ) -> tuple[SchemaT, str]:
        """Posts multimodal content to Gemini and enforces strict structured schema validation."""
        payload_with_schema = {
            "contents": [{"parts": parts}],
            "generationConfig": {
                "responseMimeType": "application/json",
                "responseJsonSchema": schema_cls.model_json_schema(),
            },
        }
        payload_json_only = {
            "contents": [{"parts": parts}],
            "generationConfig": {
                "responseMimeType": "application/json",
            },
        }
        headers = {
            "x-goog-api-key": api_key,
            "Content-Type": "application/json",
        }

        quota_err: GeminiQuotaExceededError | None = None
        last_err: PipelineError | None = None

        async with httpx.AsyncClient(timeout=timeout_sec) as client:
            for model_id in candidate_models:
                url = f"{SETTINGS.gemini_api_base_url}/models/{model_id}:generateContent"
                try:
                    resp = await client.post(url, headers=headers, json=payload_with_schema)
                except httpx.TimeoutException:
                    last_err = GeminiUpstreamError(
                        f"Gemini API request timed out for model '{model_id}'."
                    )
                    continue
                except httpx.HTTPError as exc:
                    last_err = GeminiUpstreamError(
                        f"Gemini API transport failure for model '{model_id}': {type(exc).__name__}"
                    )
                    continue

                if resp.status_code == 400:
                    err_status, err_msg = cls._extract_error_detail(resp)
                    if cls._is_schema_rejection(err_status, err_msg):
                        try:
                            resp = await client.post(
                                url, headers=headers, json=payload_json_only
                            )
                        except httpx.TimeoutException:
                            last_err = GeminiUpstreamError(
                                f"Gemini API request timed out for model '{model_id}'."
                            )
                            continue
                        except httpx.HTTPError as exc:
                            last_err = GeminiUpstreamError(
                                f"Gemini API transport failure for model '{model_id}': {type(exc).__name__}"
                            )
                            continue

                if resp.status_code in (401, 403):
                    _, err_msg = cls._extract_error_detail(resp)
                    raise GeminiUpstreamError(
                        f"Gemini API authentication failed (HTTP {resp.status_code}): {err_msg}"
                    )

                if resp.status_code in (429, 503):
                    _, err_msg = cls._extract_error_detail(resp)
                    quota_err = quota_err or GeminiQuotaExceededError(
                        f"Gemini API quota/rate limit reached on '{model_id}' (HTTP {resp.status_code}): {err_msg}"
                    )
                    last_err = quota_err
                    continue

                if resp.status_code == 404:
                    _, err_msg = cls._extract_error_detail(resp)
                    last_err = GeminiUpstreamError(
                        f"Gemini model '{model_id}' is unavailable (HTTP 404): {err_msg}"
                    )
                    continue

                if resp.status_code != 200:
                    _, err_msg = cls._extract_error_detail(resp)
                    last_err = GeminiUpstreamError(
                        f"Gemini API error on '{model_id}' (HTTP {resp.status_code}): {err_msg}"
                    )
                    continue

                try:
                    parsed = cls._parse_structured_response(resp, schema_cls)
                    return parsed, model_id
                except Exception as exc:
                    last_err = GeminiUpstreamError(
                        f"Gemini returned invalid structured JSON for {schema_cls.__name__} "
                        f"on '{model_id}': {sanitize_error_text(str(exc))}"
                    )
                    continue

        if quota_err is not None:
            raise quota_err
        if last_err is not None:
            raise last_err
        raise GeminiUpstreamError("No Gemini model endpoint succeeded.")

    @classmethod
    async def verify_active_cooking_scene(
        cls,
        *,
        frames: list[str],
        api_key: str,
        preferred_model: str | None = None,
    ) -> tuple[ActiveCookingSceneCheck, str]:
        """Verifies via Gemini Vision that the image depicts an active cooking pan/pot on a stove."""
        parts: list[dict] = [
            {"text": cls.SCENE_GATE_INSTRUCTION},
            {
                "inlineData": {
                    "mimeType": "image/jpeg",
                    "data": cls._clean_b64(frames[-1]),
                }
            },
        ]
        candidate_models = cls.resolve_candidate_models(preferred_model)
        return await cls._request_structured_json(
            parts=parts,
            schema_cls=ActiveCookingSceneCheck,
            candidate_models=candidate_models,
            api_key=api_key,
            timeout_sec=30.0,
        )

    @classmethod
    def _build_prior_context(
        cls, prior_session: Optional[CognitiveCoachingResponse]
    ) -> str:
        if prior_session is None:
            return "None (Attempt 1 baseline — no prior attempt recorded)."
        summary = {
            "prior_attempt_number": prior_session.attempt_number,
            "prior_learner_belief": prior_session.stage1_physical_observation.learner_stated_belief,
            "prior_perception_gap": prior_session.stage1_physical_observation.perception_gap_level.value,
            "prior_archetype_id": prior_session.stage2_learner_state.learner_archetype_id,
            "prior_transfer_status": prior_session.stage2_learner_state.transfer_status.value,
            "prior_conceptual_shift_verified": prior_session.stage2_learner_state.conceptual_shift_verified,
            "prior_primary_issue_taught": prior_session.stage3_teaching_decision.selected_primary_issue,
            "prior_target_principle_id": prior_session.stage3_teaching_decision.target_chef_principle_id,
            "prior_micro_experiment": prior_session.stage4_sensory_coaching.next_attempt_micro_experiment,
        }
        return json.dumps(summary)

    @classmethod
    async def generate_structured_coaching(
        cls,
        *,
        request: SessionAnalyzeRequest,
        cv_metrics: ComputerVisionMetrics | None,
        acoustic_metrics: AcousticSpectralMetrics | None,
        prior_session: CognitiveCoachingResponse | None,
        api_key: str,
    ) -> tuple[CognitiveCoachingResponse, str]:
        """Calls Gemini multimodal API directly without any biased deterministic baseline."""
        cv_block = json.dumps(cv_metrics.model_dump()) if cv_metrics else "null (No camera frame attached)"
        acoustic_block = (
            json.dumps(acoustic_metrics.model_dump())
            if acoustic_metrics
            else "null (No pan audio WAV attached)"
        )
        session_context = {
            "learner_id": request.learner_id,
            "dish_id": request.dish_id,
            "attempt_number": request.attempt_number,
            "learner_reflection": request.learner_reflection,
            "optional_archetype_hint": request.archetype_hint,
            "optional_acoustic_hint": (
                request.acoustic_hint.value if request.acoustic_hint else None
            ),
        }

        prompt_text = (
            f"{cls.COACHING_SYSTEM_INSTRUCTION}\n\n"
            f"Chef Causal Knowledge Graph Principles:\n"
            f"{json.dumps(ChefKnowledgeGraph.catalog_for_prompt())}\n\n"
            f"Session Input:\n{json.dumps(session_context)}\n\n"
            f"Measured Computer Vision Metrics:\n{cv_block}\n\n"
            f"Measured Acoustic FFT Metrics:\n{acoustic_block}\n\n"
            f"Prior Attempt History:\n{cls._build_prior_context(prior_session)}"
        )

        parts: list[dict] = [{"text": prompt_text}]
        for frame_b64 in cls._select_keyframes_for_gemini(request.keyframe_base64_list):
            parts.append(
                {
                    "inlineData": {
                        "mimeType": "image/jpeg",
                        "data": cls._clean_b64(frame_b64),
                    }
                }
            )

        if request.audio_wav_base64 and request.audio_wav_base64.strip():
            parts.append(
                {
                    "inlineData": {
                        "mimeType": "audio/wav",
                        "data": cls._clean_b64(request.audio_wav_base64),
                    }
                }
            )

        candidate_models = cls.resolve_candidate_models(
            request.preferred_reasoning_model
        )
        return await cls._request_structured_json(
            parts=parts,
            schema_cls=CognitiveCoachingResponse,
            candidate_models=candidate_models,
            api_key=api_key,
            timeout_sec=45.0,
        )
