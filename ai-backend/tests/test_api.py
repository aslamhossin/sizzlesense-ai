"""Tests for Atomic Step 6: FastAPI REST Endpoints (`/analyze`, `/archetypes/compare-onion-learners`, `/evals/run`, `/camera/tapo-rtsp-url`)."""

from fastapi.testclient import TestClient

from app.api.dependencies import get_eval_runner, get_memory_store
from app.engine.cognitive_pipeline import CognitiveCoachingPipeline
from app.engine.gemini_client import GeminiStructuredClient
from app.engine.learner_store import LearnerMemoryStore
from app.schemas.domain import CognitiveCoachingResponse, SessionAnalyzeRequest
from app.main import app

client = TestClient(app)


def test_health_endpoint_returns_frontier_models():
    resp = client.get("/api/v1/health")
    assert resp.status_code == 200
    data = resp.json()
    assert data["status"] == "healthy"
    assert data["default_reasoning_model"] == "gemini-3-flash-preview"
    assert data["default_vision_model"] == "gemini-3-flash-preview"


def test_analyze_session_endpoint_and_learner_history():
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
    assert body["stage2_learner_state"]["learner_archetype_id"] == "heat_anxiety"
    assert (
        body["stage3_teaching_decision"]["target_chef_principle_id"]
        == "thermal_recovery_and_heat_control"
    )

    # Verify history endpoint recorded Attempt 1
    hist_resp = client.get("/api/v1/learners/api_test_learner/history")
    assert hist_resp.status_code == 200
    history = hist_resp.json()
    assert len(history["sessions"]) == 1
    assert history["sessions"][0]["attempt_number"] == 1

    # Verify delete endpoint clears learner history
    del_resp = client.delete("/api/v1/learners/api_test_learner/history")
    assert del_resp.status_code == 200
    assert del_resp.json()["deleted_sessions"] == 1
    assert client.get("/api/v1/learners/api_test_learner/history").json()["sessions"] == []


def test_compare_three_onion_learners_endpoint():
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


def test_eval_suite_endpoint_returns_benchmark_report():
    resp = client.post("/api/v1/evals/run")
    assert resp.status_code == 200
    report = resp.json()
    assert report["total_sessions_evaluated"] == 10
    assert report["overall_human_learning_score"] >= 0.95


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
    schema = CognitiveCoachingResponse.model_json_schema()
    assert "stage1_physical_observation" in schema["properties"]
    assert "stage3_teaching_decision" in schema["properties"]
    assert get_memory_store() is get_memory_store()
    assert get_eval_runner() is get_eval_runner()


async def test_sqlite_learner_memory_store_persists_across_restarts(tmp_path):
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

    # Simulate server restart by opening a brand new LearnerMemoryStore on the same SQLite file
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
