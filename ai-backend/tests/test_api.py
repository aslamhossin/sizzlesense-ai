"""Tests for Atomic Step 6: FastAPI REST Endpoints (`/analyze`, `/archetypes/compare-onion-learners`, `/evals/run`, `/camera/tapo-rtsp-url`)."""

from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


def test_health_endpoint_returns_frontier_models():
    resp = client.get("/api/v1/health")
    assert resp.status_code == 200
    data = resp.json()
    assert data["status"] == "healthy"
    assert data["default_reasoning_model"] == "gemini-4-argon"
    assert data["default_vision_model"] == "gemini-3.8-flash"


def test_analyze_session_endpoint_and_learner_history():
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


def test_compare_three_jd_onion_learners_endpoint():
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
