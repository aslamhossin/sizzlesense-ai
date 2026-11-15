"""Shared pytest fixtures: isolated tmp_path SQLite DB and Gemini REST boundary mock."""

from __future__ import annotations

import json
import re
from typing import Any, Callable

import httpx
import pytest

from app.api.dependencies import get_memory_store
from app.config import SETTINGS
from app.engine.chef_knowledge import CHEF_PRINCIPLES_REGISTRY, ONION_LEARNER_ARCHETYPES
from app.engine.learner_store import LearnerMemoryStore
from app.main import app


def _extract_json_block(prompt_text: str, header: str) -> dict[str, Any] | None:
    pattern = rf"{re.escape(header)}\n(.+?)(?:\n\n|\Z)"
    match = re.search(pattern, prompt_text, flags=re.DOTALL)
    if not match:
        return None
    raw = match.group(1).strip()
    if raw.startswith("null"):
        return None
    try:
        return json.loads(raw)
    except json.JSONDecodeError:
        return None


def build_realistic_gemini_response_json(request_json: dict[str, Any]) -> str:
    """
    Simulates Gemini API structured JSON output at the HTTP transport boundary by
    parsing ONLY what the backend actually sent in the HTTP request (`parts`, `inlineData`).
    """
    parts = request_json["contents"][0]["parts"]
    prompt_text = parts[0]["text"]

    # 1. Scene Gate request
    if "active_cooking_present" in json.dumps(
        request_json.get("generationConfig", {}).get("responseJsonSchema", {})
    ) or "physical scene validator" in prompt_text.lower():
        return json.dumps(
            {
                "has_cooking_vessel": True,
                "active_cooking_present": True,
                "scene_observation": "Stainless skillet with chopped onions cooking on stovetop burner.",
            }
        )

    # 2. Coaching request
    session_input = _extract_json_block(prompt_text, "Session Input:") or {}
    cv_metrics = _extract_json_block(prompt_text, "Measured Computer Vision Metrics:")
    acoustic_metrics = _extract_json_block(prompt_text, "Measured Acoustic FFT Metrics:")
    prior_history = _extract_json_block(prompt_text, "Prior Attempt History:")

    learner_id = session_input.get("learner_id", "learner_default")
    dish_id = session_input.get("dish_id", "sauteed_onions")
    attempt_num = int(session_input.get("attempt_number", 1))
    reflection = str(session_input.get("learner_reflection", ""))
    reflection_lower = reflection.lower()

    has_image_inline = any(
        p.get("inlineData", {}).get("mimeType") == "image/jpeg" for p in parts[1:]
    )
    has_audio_inline = any(
        p.get("inlineData", {}).get("mimeType") == "audio/wav" for p in parts[1:]
    )

    acoustic_sig = (
        acoustic_metrics.get("inferred_acoustic_signature")
        if acoustic_metrics
        else None
    )
    visual_regime = (
        cv_metrics.get("dominant_visual_regime") if cv_metrics else None
    )

    # Determine archetype honestly from longitudinal history + reflection + physical state
    if prior_history and attempt_num > 1:
        archetype_id = prior_history.get("prior_archetype_id", "moisture_blindness")
    elif any(
        p in reflection_lower
        for p in ("browned really nicely", "looked done", "just like the recipe said")
    ) and (visual_regime == "PALE_STEAMING_OR_RAW" or acoustic_sig == "WET_STEAM_BUBBLE"):
        archetype_id = "perception_blindspot"
    elif any(
        p in reflection_lower
        for p in ("burn", "heat way down", "heat down", "steaming a lot", "afraid", "smoke")
    ):
        archetype_id = "heat_anxiety"
    else:
        archetype_id = "moisture_blindness"

    archetype = ONION_LEARNER_ARCHETYPES.get(
        archetype_id, ONION_LEARNER_ARCHETYPES["moisture_blindness"]
    )
    primary_principle = CHEF_PRINCIPLES_REGISTRY[archetype.primary_principle_id]

    is_positive_outcome = (
        visual_regime == "GOLDEN_MAILLARD_BROWNING"
        or acoustic_sig == "STEADY_APPLAUSE_SIZZLE"
    )
    blind_timer = any(
        k in reflection_lower
        for k in ("without looking", "phone timer", "only used a timer")
    )
    sensory_aware = any(
        k in reflection_lower
        for k in ("sizzle", "steam", "golden", "amber", "sound", "shimmer", "glaze", "fond", "smell")
    )

    if attempt_num == 1 or not prior_history:
        transfer_status = "INITIAL_BASELINE"
        conceptual_shift = False
    elif is_positive_outcome and sensory_aware and not blind_timer:
        transfer_status = "CONCEPT_ADOPTED"
        conceptual_shift = True
    elif is_positive_outcome:
        transfer_status = "PARTIAL_EXECUTION_ONLY"
        conceptual_shift = False
    else:
        transfer_status = "REGRESSED_OR_OVERCORRECTED"
        conceptual_shift = False

    if blind_timer:
        gap_level = "MODERATE_DRIFT"
        gap_exp = "Learner cooked solely by phone timer without visually or auditorily observing the pan."
    elif archetype_id == "perception_blindspot" and attempt_num == 1:
        gap_level = "CRITICAL_BLINDSPOT"
        gap_exp = "Learner claims onions browned nicely while physical telemetry shows steaming/boiling (~100°C)."
    elif archetype_id == "heat_anxiety" and attempt_num == 1:
        gap_level = "MODERATE_DRIFT"
        gap_exp = "Learner mistook white steam vapor for burning smoke and cut heat too low."
    else:
        gap_level = "ALIGNED"
        gap_exp = "Learner perception matches physical pan telemetry."

    if conceptual_shift:
        selected_principle = CHEF_PRINCIPLES_REGISTRY["fond_glazing_and_aroma_timing"]
        omitted_principle = CHEF_PRINCIPLES_REGISTRY["cut_geometry_and_surface_area"]
    else:
        selected_principle = primary_principle
        omitted_principle = CHEF_PRINCIPLES_REGISTRY["cut_geometry_and_surface_area"]

    timeline_acoustic = acoustic_sig or (
        "STEADY_APPLAUSE_SIZZLE" if is_positive_outcome else "WET_STEAM_BUBBLE"
    )
    visual_obs = (
        f"Observed {visual_regime} (frame attached={has_image_inline}, audio attached={has_audio_inline})."
        if visual_regime
        else f"No camera frame; acoustic signature is {timeline_acoustic} (audio attached={has_audio_inline})."
    )
    coaching_msg = (
        f"Watch the pan edges carefully: {selected_principle.visual_cue} "
        f"And listen closely: {selected_principle.auditory_cue}"
    )

    response_obj = {
        "session_id": "sess_gemini_mock",
        "learner_id": learner_id,
        "dish_id": dish_id,
        "attempt_number": attempt_num,
        "reasoning_model_used": "gemini-3-flash-preview",
        "vision_model_used": "gemini-3-flash-preview",
        "five_core_questions": {
            "what_is_happening_now": visual_obs,
            "what_they_understand_vs_not": (
                f"Understands: {archetype.latent_understanding_baseline} | "
                f"Gap: {archetype.latent_misconception_baseline}"
            ),
            "why_they_made_that_decision": archetype.decision_root_cause_baseline,
            "what_single_thing_to_teach_now": selected_principle.title,
            "how_to_communicate_to_shift_judgment": coaching_msg,
        },
        "stage1_physical_observation": {
            "observed_timeline_facts": [
                {
                    "timestamp": "00:12" if attempt_num == 1 else "00:18",
                    "visual_observation": visual_obs,
                    "acoustic_signature": timeline_acoustic,
                }
            ],
            "inferred_physical_states": [
                "Pan temperature is >140°C in active Maillard zone."
                if is_positive_outcome
                else "Pan temperature is thermally constrained near <=100°C."
            ],
            "environmental_uncertainties": ["Exact burner BTU rating is unmeasured."],
            "cv_metrics": cv_metrics,
            "acoustic_metrics": acoustic_metrics,
            "learner_stated_belief": reflection,
            "perception_gap_level": gap_level,
            "perception_gap_explanation": gap_exp,
        },
        "stage2_learner_state": {
            "learner_archetype_id": archetype_id,
            "demonstrated_understandings": [archetype.latent_understanding_baseline],
            "latent_misconceptions": [
                {
                    "concept_id": archetype.primary_principle_id,
                    "description": archetype.latent_misconception_baseline,
                    "confidence": 0.2 if conceptual_shift else 0.9,
                }
            ],
            "decision_root_cause": (
                "Used sensory cues to control pan temperature."
                if conceptual_shift
                else archetype.decision_root_cause_baseline
            ),
            "transfer_status": transfer_status,
            "conceptual_shift_verified": conceptual_shift,
            "longitudinal_delta_summary": (
                f"Attempt {attempt_num} evaluated against prior attempt."
                if prior_history
                else "Attempt 1 baseline established."
            ),
        },
        "stage3_teaching_decision": {
            "candidate_issues_detected": [
                selected_principle.title,
                omitted_principle.title,
            ],
            "selected_primary_issue": selected_principle.title,
            "causal_priority_rationale": selected_principle.physics_explanation,
            "deliberately_omitted_issues": [
                {
                    "issue": omitted_principle.title,
                    "suppression_reason": "Held back to avoid cognitive overload.",
                }
            ],
            "target_chef_principle_id": selected_principle.id,
        },
        "stage4_sensory_coaching": {
            "visual_cue_to_observe": selected_principle.visual_cue,
            "auditory_cue_to_listen": selected_principle.auditory_cue,
            "olfactory_or_tactile_cue": selected_principle.olfactory_tactile_cue,
            "coaching_message": coaching_msg,
            "next_attempt_micro_experiment": (
                f"Observe {selected_principle.visual_cue} and listen for {selected_principle.auditory_cue}"
            ),
        },
    }
    return json.dumps(response_obj)


@pytest.fixture(autouse=True)
def isolate_sqlite_and_api_key(tmp_path, monkeypatch):
    """Ensures pytest NEVER touches production SQLite DB or real .env secrets."""
    test_db = tmp_path / "test_sizzlesense_learners.db"
    monkeypatch.setattr(SETTINGS, "sqlite_db_path", str(test_db))
    monkeypatch.setenv("GEMINI_API_KEY", "test-mock-gemini-key")
    monkeypatch.setattr(
        "app.config._read_dotenv_value",
        lambda key: "test-mock-gemini-key" if key == "GEMINI_API_KEY" else None,
    )
    get_memory_store.cache_clear()
    test_store = LearnerMemoryStore(db_path=test_db)
    app.dependency_overrides[get_memory_store] = lambda: test_store
    yield test_store
    app.dependency_overrides.pop(get_memory_store, None)
    get_memory_store.cache_clear()


@pytest.fixture
def mock_gemini_http(monkeypatch):
    """
    Mocks Gemini REST calls at the `httpx.AsyncClient.post` boundary and records
    every outgoing HTTP request for assertions.
    """
    recorded_calls: list[dict[str, Any]] = []
    custom_handler: Callable[[str, dict[str, Any]], httpx.Response] | None = None

    async def fake_post(self, url: str, headers: dict | None = None, json: dict | None = None, **kwargs):
        payload = json or {}
        recorded_calls.append({"url": url, "headers": headers or {}, "json": payload})
        if custom_handler is not None:
            return custom_handler(url, payload)
        text = build_realistic_gemini_response_json(payload)
        return httpx.Response(
            status_code=200,
            json={"candidates": [{"content": {"parts": [{"text": text}]}}]},
        )

    monkeypatch.setattr(httpx.AsyncClient, "post", fake_post)

    class Controller:
        calls = recorded_calls

        def set_handler(self, fn: Callable[[str, dict[str, Any]], httpx.Response]) -> None:
            nonlocal custom_handler
            custom_handler = fn

    return Controller()
