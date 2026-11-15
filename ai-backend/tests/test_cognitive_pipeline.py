"""Tests for 4-Stage Live Gemini Cognitive Pipeline, Scene Gate, Fail-Fast Errors & Longitudinal Store."""

import json
import httpx
import pytest

from app.engine.cognitive_pipeline import CognitiveCoachingPipeline
from app.engine.errors import (
    GeminiQuotaExceededError,
    GeminiUpstreamError,
    InvalidMediaInputError,
    MissingGeminiApiKeyError,
    NoActiveCookingSceneError,
)
from app.engine.gemini_client import GeminiStructuredClient
from app.engine.learner_store import LearnerMemoryStore
from app.schemas.domain import (
    BehavioralTransferStatus,
    PerceptionGapLevel,
    SessionAnalyzeRequest,
)
from tests.conftest import build_realistic_gemini_response_json
from tests.test_helpers import generate_synthetic_kitchen_frame_base64


@pytest.mark.asyncio
async def test_same_visual_failure_yields_three_different_diagnoses_for_archetypes(
    mock_gemini_http,
):
    store = LearnerMemoryStore()
    pipeline = CognitiveCoachingPipeline(memory_store=store)
    pale_frame = generate_synthetic_kitchen_frame_base64("pale_steaming")

    resp_a = await pipeline.analyze_session(
        SessionAnalyzeRequest(
            learner_id="learner_a",
            dish_id="sauteed_onions",
            attempt_number=1,
            learner_reflection="I kept stirring the onions for 8 minutes on medium heat, but they stayed pale and watery.",
            keyframe_base64_list=[pale_frame],
        )
    )
    resp_b = await pipeline.analyze_session(
        SessionAnalyzeRequest(
            learner_id="learner_b",
            dish_id="sauteed_onions",
            attempt_number=1,
            learner_reflection="It started steaming a lot right away so I turned the heat way down so it wouldn't burn.",
            keyframe_base64_list=[pale_frame],
        )
    )
    resp_c = await pipeline.analyze_session(
        SessionAnalyzeRequest(
            learner_id="learner_c",
            dish_id="sauteed_onions",
            attempt_number=1,
            learner_reflection="I sautéed the onions for 5 minutes just like the recipe said and they browned really nicely!",
            keyframe_base64_list=[pale_frame],
        )
    )

    assert resp_a.stage2_learner_state.learner_archetype_id == "moisture_blindness"
    assert resp_b.stage2_learner_state.learner_archetype_id == "heat_anxiety"
    assert resp_c.stage2_learner_state.learner_archetype_id == "perception_blindspot"
    assert (
        resp_c.stage1_physical_observation.perception_gap_level
        == PerceptionGapLevel.CRITICAL_BLINDSPOT
    )
    assert (
        resp_a.stage3_teaching_decision.target_chef_principle_id
        == "moisture_evaporation_gate"
    )
    assert (
        resp_b.stage3_teaching_decision.target_chef_principle_id
        == "thermal_recovery_and_heat_control"
    )
    assert (
        resp_c.stage3_teaching_decision.target_chef_principle_id
        == "visual_and_acoustic_doneness_calibration"
    )


@pytest.mark.asyncio
async def test_longitudinal_transfer_across_three_attempts(mock_gemini_http):
    store = LearnerMemoryStore()
    pipeline = CognitiveCoachingPipeline(memory_store=store)

    pale_frame = generate_synthetic_kitchen_frame_base64("pale_steaming")
    golden_frame = generate_synthetic_kitchen_frame_base64("golden_maillard")

    r1 = await pipeline.analyze_session(
        SessionAnalyzeRequest(
            learner_id="learner_growth",
            dish_id="sauteed_onions",
            attempt_number=1,
            learner_reflection="It steamed a lot so I turned the heat down low out of fear of burning.",
            keyframe_base64_list=[pale_frame],
        )
    )
    assert r1.stage2_learner_state.transfer_status == BehavioralTransferStatus.INITIAL_BASELINE
    assert r1.stage2_learner_state.conceptual_shift_verified is False

    r2 = await pipeline.analyze_session(
        SessionAnalyzeRequest(
            learner_id="learner_growth",
            dish_id="sauteed_onions",
            attempt_number=2,
            learner_reflection="I recognized the white vapor was harmless steam, kept the sizzle steady, and watched the edges turn golden!",
            keyframe_base64_list=[golden_frame],
        )
    )
    assert r2.stage2_learner_state.transfer_status == BehavioralTransferStatus.CONCEPT_ADOPTED
    assert r2.stage2_learner_state.conceptual_shift_verified is True
    assert r2.stage2_learner_state.learner_archetype_id == "heat_anxiety"
    assert len(store.get_history("learner_growth")) == 2


@pytest.mark.asyncio
async def test_lucky_execution_without_sensory_understanding_is_flagged(mock_gemini_http):
    store = LearnerMemoryStore()
    pipeline = CognitiveCoachingPipeline(memory_store=store)

    pale_frame = generate_synthetic_kitchen_frame_base64("pale_steaming")
    golden_frame = generate_synthetic_kitchen_frame_base64("golden_maillard")

    await pipeline.analyze_session(
        SessionAnalyzeRequest(
            learner_id="learner_lucky",
            dish_id="sauteed_onions",
            attempt_number=1,
            learner_reflection="I cooked for 5 minutes on the timer and they looked done.",
            keyframe_base64_list=[pale_frame],
        )
    )

    r2_lucky = await pipeline.analyze_session(
        SessionAnalyzeRequest(
            learner_id="learner_lucky",
            dish_id="sauteed_onions",
            attempt_number=2,
            learner_reflection="I just set my phone timer for 11 minutes instead of 5 minutes without looking at the pan.",
            keyframe_base64_list=[golden_frame],
        )
    )
    assert (
        r2_lucky.stage2_learner_state.transfer_status
        == BehavioralTransferStatus.PARTIAL_EXECUTION_ONLY
    )
    assert r2_lucky.stage2_learner_state.conceptual_shift_verified is False


@pytest.mark.asyncio
async def test_missing_api_key_raises_503_without_fallback(monkeypatch):
    monkeypatch.delenv("GEMINI_API_KEY", raising=False)
    monkeypatch.delenv("GOOGLE_API_KEY", raising=False)
    monkeypatch.setattr("app.config._read_dotenv_value", lambda key: None)

    pipeline = CognitiveCoachingPipeline(memory_store=LearnerMemoryStore())
    with pytest.raises(MissingGeminiApiKeyError) as exc_info:
        await pipeline.analyze_session(
            SessionAnalyzeRequest(
                learner_id="no_key",
                attempt_number=1,
                learner_reflection="Test reflection",
            )
        )
    assert exc_info.value.status_code == 503


@pytest.mark.asyncio
async def test_empty_stove_scene_gate_rejects_with_422_and_does_not_persist(
    mock_gemini_http,
):
    def empty_stove_handler(url: str, payload: dict) -> httpx.Response:
        return httpx.Response(
            status_code=200,
            json={
                "candidates": [
                    {
                        "content": {
                            "parts": [
                                {
                                    "text": json.dumps(
                                        {
                                            "has_cooking_vessel": False,
                                            "active_cooking_present": False,
                                            "scene_observation": "Empty glass stovetop burner with no pan.",
                                        }
                                    )
                                }
                            ]
                        }
                    }
                ]
            },
        )

    mock_gemini_http.set_handler(empty_stove_handler)
    store = LearnerMemoryStore()
    pipeline = CognitiveCoachingPipeline(memory_store=store)
    frame = generate_synthetic_kitchen_frame_base64("pale_steaming")

    with pytest.raises(NoActiveCookingSceneError) as exc_info:
        await pipeline.analyze_session(
            SessionAnalyzeRequest(
                learner_id="empty_stove_user",
                attempt_number=1,
                learner_reflection="Cooking onions",
                keyframe_base64_list=[frame],
            )
        )
    assert exc_info.value.status_code == 422
    assert "No pan or active cooking detected on the stove" in exc_info.value.detail
    assert len(mock_gemini_http.calls) == 1
    assert store.get_history("empty_stove_user") == []


@pytest.mark.asyncio
async def test_gemini_429_does_not_strip_schema_and_raises_503(mock_gemini_http):
    def quota_handler(url: str, payload: dict) -> httpx.Response:
        return httpx.Response(
            status_code=429,
            json={"error": {"code": 429, "status": "RESOURCE_EXHAUSTED", "message": "Quota exceeded"}},
        )

    mock_gemini_http.set_handler(quota_handler)
    pipeline = CognitiveCoachingPipeline(memory_store=LearnerMemoryStore())

    with pytest.raises(GeminiQuotaExceededError) as exc_info:
        await pipeline.analyze_session(
            SessionAnalyzeRequest(
                learner_id="quota_user",
                attempt_number=1,
                learner_reflection="Test",
            )
        )
    assert exc_info.value.status_code == 503
    # 2 candidate models (gemini-3-flash-preview, gemini-3.1-pro-preview), 1 call each (no schema-strip retry on 429)
    assert len(mock_gemini_http.calls) == 2
    assert all("responseJsonSchema" in c["json"]["generationConfig"] for c in mock_gemini_http.calls)


@pytest.mark.asyncio
async def test_gemini_401_aborts_immediately_without_model_failover(mock_gemini_http):
    def auth_handler(url: str, payload: dict) -> httpx.Response:
        return httpx.Response(
            status_code=401,
            json={"error": {"code": 401, "status": "UNAUTHENTICATED", "message": "API key invalid"}},
        )

    mock_gemini_http.set_handler(auth_handler)
    pipeline = CognitiveCoachingPipeline(memory_store=LearnerMemoryStore())

    with pytest.raises(GeminiUpstreamError) as exc_info:
        await pipeline.analyze_session(
            SessionAnalyzeRequest(
                learner_id="auth_user",
                attempt_number=1,
                learner_reflection="Test",
            )
        )
    assert exc_info.value.status_code == 502
    assert len(mock_gemini_http.calls) == 1


@pytest.mark.asyncio
async def test_gemini_400_schema_error_retries_json_only_and_succeeds(mock_gemini_http):
    def schema_400_handler(url: str, payload: dict) -> httpx.Response:
        if "responseJsonSchema" in payload.get("generationConfig", {}):
            return httpx.Response(
                status_code=400,
                json={"error": {"code": 400, "status": "INVALID_ARGUMENT", "message": "Unsupported responseJsonSchema $defs"}},
            )
        text = build_realistic_gemini_response_json(payload)
        return httpx.Response(
            status_code=200,
            json={"candidates": [{"content": {"parts": [{"text": text}]}}]},
        )

    mock_gemini_http.set_handler(schema_400_handler)
    pipeline = CognitiveCoachingPipeline(memory_store=LearnerMemoryStore())
    resp = await pipeline.analyze_session(
        SessionAnalyzeRequest(
            learner_id="schema_retry_user",
            attempt_number=1,
            learner_reflection="Stayed pale and watery",
        )
    )
    assert resp.reasoning_model_used == "gemini-3-flash-preview (Live Gemini API)"
    assert len(mock_gemini_http.calls) == 2


@pytest.mark.asyncio
async def test_authoritative_pinning_overrides_gemini_model_and_metric_hallucinations(
    mock_gemini_http,
):
    def tampered_handler(url: str, payload: dict) -> httpx.Response:
        if "physical scene validator" in payload["contents"][0]["parts"][0]["text"].lower():
            return httpx.Response(
                status_code=200,
                json={
                    "candidates": [
                        {
                            "content": {
                                "parts": [
                                    {
                                        "text": json.dumps(
                                            {
                                                "has_cooking_vessel": True,
                                                "active_cooking_present": True,
                                                "scene_observation": "Onions in skillet",
                                            }
                                        )
                                    }
                                ]
                            }
                        }
                    ]
                },
            )
        data = json.loads(build_realistic_gemini_response_json(payload))
        data["vision_model_used"] = "gpt-4o-vision"
        data["reasoning_model_used"] = "fake-model"
        data["stage1_physical_observation"]["cv_metrics"] = None
        return httpx.Response(
            status_code=200,
            json={"candidates": [{"content": {"parts": [{"text": json.dumps(data)}]}}]},
        )

    mock_gemini_http.set_handler(tampered_handler)
    pipeline = CognitiveCoachingPipeline(memory_store=LearnerMemoryStore())
    frame = generate_synthetic_kitchen_frame_base64("pale_steaming")
    resp = await pipeline.analyze_session(
        SessionAnalyzeRequest(
            learner_id="pinning_user",
            attempt_number=1,
            learner_reflection="Watery onions",
            keyframe_base64_list=[frame],
        )
    )
    assert resp.reasoning_model_used == "gemini-3-flash-preview (Live Gemini API)"
    assert resp.vision_model_used == "gemini-3-flash-preview + CV-Pixel-Analyzer"
    assert resp.stage1_physical_observation.cv_metrics is not None
    assert resp.stage1_physical_observation.cv_metrics.dominant_visual_regime == "PALE_STEAMING_OR_RAW"


def test_resolve_candidate_models_respects_preferred_and_excludes_retired():
    models = GeminiStructuredClient.resolve_candidate_models("gemini-3.1-pro-preview")
    assert models[0] == "gemini-3.1-pro-preview"
    assert "gemini-2.5-pro" not in models
    assert "gemini-2.5-flash" not in models
