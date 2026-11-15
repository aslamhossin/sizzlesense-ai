"""Tests for FastAPI REST Endpoints (`/sessions/analyze`, `/camera/tapo-live-analyze`, `/evals/run`, etc.)."""

import json
import httpx
from fastapi.testclient import TestClient

from app.api.dependencies import get_eval_runner, get_memory_store
from app.audio.sizzle_analyzer import generate_synthetic_sizzle_wav_base64
from app.engine.cognitive_pipeline import CognitiveCoachingPipeline
from app.engine.gemini_client import GeminiStructuredClient
from app.engine.learner_store import LearnerMemoryStore
from app.main import app
from app.schemas.domain import CognitiveCoachingResponse, SessionAnalyzeRequest
from app.vision.frame_analyzer import (
    TapoLanDiscovery,
    TapoRtspUrlBuilder,
    generate_synthetic_kitchen_frame_base64,
)

client = TestClient(app)


def test_health_endpoint_returns_frontier_models():
    resp = client.get("/api/v1/health")
    assert resp.status_code == 200
    data = resp.json()
    assert data["status"] == "healthy"
    assert data["default_reasoning_model"] == "gemini-3-flash-preview"
    assert data["default_vision_model"] == "gemini-3-flash-preview"


def test_analyze_session_endpoint_and_learner_history(mock_gemini_http):
    client.delete("/api/v1/learners/api_test_learner/history")
    payload = {
        "learner_id": "api_test_learner",
        "dish_id": "sauteed_onions",
        "attempt_number": 1,
        "learner_reflection": "It started steaming a lot so I turned the heat way down so it wouldn't burn.",
    }
    resp = client.post("/api/v1/sessions/analyze", json=payload)
    assert resp.status_code == 200
    body = resp.json()
    assert body["reasoning_model_used"] == "gemini-3-flash-preview (Live Gemini API)"
    assert body["vision_model_used"] == "No Camera Frame"
    assert body["stage1_physical_observation"]["cv_metrics"] is None
    assert body["stage2_learner_state"]["learner_archetype_id"] == "heat_anxiety"
    assert (
        body["stage3_teaching_decision"]["target_chef_principle_id"]
        == "thermal_recovery_and_heat_control"
    )

    hist_resp = client.get("/api/v1/learners/api_test_learner/history")
    assert hist_resp.status_code == 200
    history = hist_resp.json()
    assert len(history["sessions"]) == 1
    assert history["sessions"][0]["attempt_number"] == 1

    del_resp = client.delete("/api/v1/learners/api_test_learner/history")
    assert del_resp.status_code == 200
    assert del_resp.json()["deleted_sessions"] == 1
    assert client.get("/api/v1/learners/api_test_learner/history").json()["sessions"] == []


def test_analyze_session_rejects_empty_stove_with_422(mock_gemini_http):
    mock_gemini_http.set_handler(
        lambda url, payload: httpx.Response(
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
                                            "scene_observation": "Empty sink and countertop.",
                                        }
                                    )
                                }
                            ]
                        }
                    }
                ]
            },
        )
    )
    frame = generate_synthetic_kitchen_frame_base64("pale_steaming")
    resp = client.post(
        "/api/v1/sessions/analyze",
        json={
            "learner_id": "empty_sink_learner",
            "attempt_number": 1,
            "learner_reflection": "Cooking",
            "keyframe_base64_list": [frame],
        },
    )
    assert resp.status_code == 422
    assert "No pan or active cooking detected" in resp.json()["detail"]
    assert client.get("/api/v1/learners/empty_sink_learner/history").json()["sessions"] == []


def test_analyze_session_rejects_malformed_media_with_422(mock_gemini_http):
    bad_img = client.post(
        "/api/v1/sessions/analyze",
        json={
            "learner_id": "bad_media",
            "attempt_number": 1,
            "learner_reflection": "test",
            "keyframe_base64_list": ["not_valid_base64!!!"],
        },
    )
    assert bad_img.status_code == 422

    bad_wav = client.post(
        "/api/v1/sessions/analyze",
        json={
            "learner_id": "bad_media",
            "attempt_number": 1,
            "learner_reflection": "test",
            "audio_wav_base64": "AAAA",
        },
    )
    assert bad_wav.status_code == 422


def test_analyze_session_returns_503_when_api_key_missing(monkeypatch):
    monkeypatch.delenv("GEMINI_API_KEY", raising=False)
    monkeypatch.delenv("GOOGLE_API_KEY", raising=False)
    monkeypatch.setattr("app.config._read_dotenv_value", lambda key: None)

    resp = client.post(
        "/api/v1/sessions/analyze",
        json={
            "learner_id": "no_key_api",
            "attempt_number": 1,
            "learner_reflection": "test",
        },
    )
    assert resp.status_code == 503
    assert "GEMINI_API_KEY" in resp.json()["detail"]


def test_compare_three_onion_learners_endpoint(mock_gemini_http):
    resp = client.get("/api/v1/archetypes/compare-onion-learners")
    assert resp.status_code == 200
    data = resp.json()
    assert len(data["comparisons"]) == 3
    archetypes = {item["archetype_id"] for item in data["comparisons"]}
    assert archetypes == {
        "moisture_blindness",
        "heat_anxiety",
        "perception_blindspot",
    }


def test_eval_suite_endpoint_returns_benchmark_report(mock_gemini_http):
    resp = client.post("/api/v1/evals/run")
    assert resp.status_code == 200
    report = resp.json()
    assert report["total_sessions_evaluated"] == 10
    assert report["overall_human_learning_score"] >= 0.95


def test_eval_suite_endpoint_returns_503_on_gemini_quota_error(mock_gemini_http):
    mock_gemini_http.set_handler(
        lambda url, payload: httpx.Response(
            status_code=429,
            json={"error": {"code": 429, "status": "RESOURCE_EXHAUSTED", "message": "Quota exceeded"}},
        )
    )
    resp = client.post("/api/v1/evals/run")
    assert resp.status_code == 503


def test_tapo_live_analyze_forwards_audio_and_archetype_and_validates_attempt(
    mock_gemini_http, monkeypatch
):
    async def fake_discover(**kwargs):
        return ("192.168.11.14", True, "env_config")

    monkeypatch.setattr(TapoLanDiscovery, "discover_tapo_camera", fake_discover)
    frame_b64 = generate_synthetic_kitchen_frame_base64("pale_steaming")
    monkeypatch.setattr(
        TapoRtspUrlBuilder,
        "capture_live_frame_base64",
        classmethod(lambda cls, **kwargs: frame_b64),
    )

    wav_b64 = generate_synthetic_sizzle_wav_base64("wet_steam_bubble")
    resp = client.post(
        "/api/v1/camera/tapo-live-analyze",
        json={
            "learner_id": "tapo_audio_user",
            "attempt_number": 1,
            "learner_reflection": "",
            "archetype_hint": "moisture_blindness",
            "audio_wav_base64": wav_b64,
        },
    )
    assert resp.status_code == 200
    body = resp.json()
    assert body["vision_model_used"] == "gemini-3-flash-preview + CV-Pixel + Audio-FFT"
    assert body["stage1_physical_observation"]["acoustic_metrics"] is not None

    bad_attempt = client.post(
        "/api/v1/camera/tapo-live-analyze",
        json={"learner_id": "tapo_audio_user", "attempt_number": 99},
    )
    assert bad_attempt.status_code == 422


def test_tapo_rtsp_url_endpoint_encodes_credentials():
    resp = client.post(
        "/api/v1/camera/tapo-rtsp-url",
        json={
            "ip_address": "192.168.1.50",
            "username": "chef",
            "password": "p@ss:word",
            "use_substream": False,
        },
    )
    assert resp.status_code == 200
    data = resp.json()
    assert data["rtsp_url"] == "rtsp://chef:p%40ss%3Aword@192.168.1.50:554/stream1"


def test_structured_gemini_schema_and_di_providers():
    models = GeminiStructuredClient.resolve_candidate_models("gemini-3-flash-preview")
    assert models[0] == "gemini-3-flash-preview"
    assert "gemini-2.5-pro" not in models
    assert "gemini-2.5-flash" not in models
    schema = CognitiveCoachingResponse.model_json_schema()
    assert "stage1_physical_observation" in schema["properties"]
    assert "stage3_teaching_decision" in schema["properties"]
    assert get_memory_store() is get_memory_store()
    assert get_eval_runner() is get_eval_runner()


async def test_sqlite_learner_memory_store_persists_across_restarts(
    tmp_path, mock_gemini_http
):
    db_file = tmp_path / "persistent_learners.db"
    store_before_restart = LearnerMemoryStore(db_path=db_file)
    pipeline_1 = CognitiveCoachingPipeline(memory_store=store_before_restart)

    await pipeline_1.analyze_session(
        SessionAnalyzeRequest(
            learner_id="persistent_chef",
            dish_id="sauteed_onions",
            attempt_number=1,
            learner_reflection="My onions are boiling in liquid and staying pale.",
        )
    )

    store_after_restart = LearnerMemoryStore(db_path=db_file)
    restored_history = store_after_restart.get_history("persistent_chef", "sauteed_onions")
    assert len(restored_history) == 1
    assert restored_history[0].attempt_number == 1

    pipeline_2 = CognitiveCoachingPipeline(memory_store=store_after_restart)
    attempt2 = await pipeline_2.analyze_session(
        SessionAnalyzeRequest(
            learner_id="persistent_chef",
            dish_id="sauteed_onions",
            attempt_number=2,
            learner_reflection="I waited for the bubbling hiss to turn into a sharp crackle before stirring!",
        )
    )
    assert attempt2.attempt_number == 2
    assert len(store_after_restart.get_history("persistent_chef", "sauteed_onions")) == 2


def test_camera_discover_endpoint_returns_lan_backend_and_camera_without_exposing_secrets(
    monkeypatch,
):
    monkeypatch.setattr(TapoLanDiscovery, "detect_local_lan_ip", staticmethod(lambda: "192.168.11.7"))

    async def fake_discover(**kwargs):
        return ("192.168.11.14", True, "lan_scan")

    monkeypatch.setattr(TapoLanDiscovery, "discover_tapo_camera", fake_discover)

    resp = client.get("/api/v1/camera/discover?preferred_ip=192.168.11.16")
    assert resp.status_code == 200
    body = resp.json()
    assert body["backend_url"] == "http://192.168.11.7:8000"
    assert body["camera_ip"] == "192.168.11.14"
    assert body["camera_reachable"] is True
    assert body["discovery_source"] == "lan_scan"
    assert "password" not in body
    assert "username" not in body
