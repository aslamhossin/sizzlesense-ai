"""Tests for Automated Human Learning Evaluation Suite (`EvalBench`)."""

import json
import httpx
import pytest

from app.engine.errors import GeminiQuotaExceededError
from app.evals.human_learning_eval import (
    HumanLearningEvalRunner,
    score_sensory_grounding_with_penalty,
)
from tests.conftest import build_realistic_gemini_response_json


@pytest.mark.asyncio
async def test_human_learning_eval_suite_runs_all_archetypes_with_wav_audio_and_no_hint_leakage(
    mock_gemini_http,
):
    runner = HumanLearningEvalRunner()
    report = await runner.run_full_benchmark()

    assert report.total_sessions_evaluated == 10
    assert report.perception_gap_detection_score >= 0.95
    assert report.latent_root_cause_accuracy >= 0.95
    assert report.single_issue_restraint_score == 1.0
    assert report.sensory_grounding_score >= 0.95
    assert report.cross_attempt_transfer_score >= 0.95
    assert report.overall_human_learning_score >= 0.95
    assert len(report.case_results) == 10

    # Verify EvalBench never leaks archetype_hint / acoustic_hint or generates synthetic skillet frames
    assert len(mock_gemini_http.calls) == 11  # 9 archetype attempts + 2 negative control attempts
    for call in mock_gemini_http.calls:
        parts = call["json"]["contents"][0]["parts"]
        prompt = parts[0]["text"]
        assert '"optional_archetype_hint": null' in prompt
        assert '"optional_acoustic_hint": null' in prompt
        assert any(p.get("inlineData", {}).get("mimeType") == "audio/wav" for p in parts[1:])
        assert not any(p.get("inlineData", {}).get("mimeType") == "image/jpeg" for p in parts[1:])


@pytest.mark.asyncio
async def test_eval_case10_does_not_hardcode_true_when_archetype_wrong(mock_gemini_http):
    """Proves Case #10 genuinely checks Gemini's returned archetype/perception instead of hardcoding True."""

    def wrong_case10_handler(url: str, payload: dict) -> httpx.Response:
        text = build_realistic_gemini_response_json(payload)
        obj = json.loads(text)
        if obj["learner_id"] == "eval_negative_control" and obj["attempt_number"] == 2:
            # Force a misclassification back to moisture_blindness + ALIGNED
            obj["stage2_learner_state"]["learner_archetype_id"] = "moisture_blindness"
            obj["stage1_physical_observation"]["perception_gap_level"] = "ALIGNED"
        return httpx.Response(
            status_code=200,
            json={"candidates": [{"content": {"parts": [{"text": json.dumps(obj)}]}}]},
        )

    mock_gemini_http.set_handler(wrong_case10_handler)
    runner = HumanLearningEvalRunner()
    report = await runner.run_full_benchmark()

    case10 = report.case_results[-1]
    assert case10.case_id == "negative_control_lucky_timer_attempt_2"
    assert case10.root_cause_passed is False
    assert case10.perception_gap_passed is False
    assert report.latent_root_cause_accuracy == 0.9
    assert report.perception_gap_detection_score == 0.9


@pytest.mark.asyncio
async def test_eval_suite_surfaces_gemini_errors_instead_of_faking_100(mock_gemini_http):
    mock_gemini_http.set_handler(
        lambda url, payload: httpx.Response(
            status_code=429,
            json={"error": {"code": 429, "status": "RESOURCE_EXHAUSTED", "message": "Quota exceeded"}},
        )
    )
    runner = HumanLearningEvalRunner()
    with pytest.raises(GeminiQuotaExceededError):
        await runner.run_full_benchmark()


def test_sensory_grounding_penalizes_rigid_timer_rules():
    good_coaching = (
        "Watch the milky liquid shrink into clear oil shimmer and listen for a crisp applause sizzle."
    )
    bad_timer_coaching = (
        "Next time, cook for exactly 7 minutes on medium heat and then stop."
    )

    assert score_sensory_grounding_with_penalty(good_coaching) == 1.0
    assert score_sensory_grounding_with_penalty(bad_timer_coaching) == 0.0
