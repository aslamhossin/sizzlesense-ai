"""Tests for FFT Acoustic Sizzle Spectral Analyzer & Multimodal Audio+Vision Session Analysis."""

from fastapi.testclient import TestClient
import pytest

from app.audio.sizzle_analyzer import (
    AcousticSizzleAnalyzer,
    generate_synthetic_sizzle_wav_base64,
)
from app.main import app
from app.schemas.domain import AcousticSizzleSignature
from tests.test_helpers import generate_synthetic_kitchen_frame_base64

client = TestClient(app)


def test_fft_analyzer_classifies_silent_cold_regime():
    wav_b64 = generate_synthetic_sizzle_wav_base64("silent_cold")
    metrics = AcousticSizzleAnalyzer.analyze_wav_base64(wav_b64)
    assert metrics.inferred_acoustic_signature == AcousticSizzleSignature.SILENT_COLD
    assert metrics.rms_energy_db < -44.0


def test_fft_analyzer_classifies_wet_steam_bubble_regime():
    wav_b64 = generate_synthetic_sizzle_wav_base64("wet_steam_bubble")
    metrics = AcousticSizzleAnalyzer.analyze_wav_base64(wav_b64)
    assert metrics.inferred_acoustic_signature == AcousticSizzleSignature.WET_STEAM_BUBBLE
    assert metrics.low_freq_bubble_ratio > 0.70
    assert metrics.spectral_centroid_hz < 800.0


def test_fft_analyzer_classifies_steady_applause_sizzle_regime():
    wav_b64 = generate_synthetic_sizzle_wav_base64("steady_applause_sizzle")
    metrics = AcousticSizzleAnalyzer.analyze_wav_base64(wav_b64)
    assert (
        metrics.inferred_acoustic_signature
        == AcousticSizzleSignature.STEADY_APPLAUSE_SIZZLE
    )
    assert metrics.high_freq_sizzle_ratio > 0.50
    assert metrics.spectral_centroid_hz > 2000.0


def test_fft_analyzer_classifies_aggressive_popping_burn_regime():
    wav_b64 = generate_synthetic_sizzle_wav_base64("aggressive_popping_burn")
    metrics = AcousticSizzleAnalyzer.analyze_wav_base64(wav_b64)
    assert (
        metrics.inferred_acoustic_signature
        == AcousticSizzleSignature.AGGRESSIVE_POPPING_BURN
    )
    assert metrics.high_freq_sizzle_ratio > 0.35


def test_fft_analyzer_rejects_malformed_wav():
    with pytest.raises(ValueError):
        AcousticSizzleAnalyzer.analyze_wav_base64("AAAA")


def test_analyze_session_audio_only_has_null_cv_and_no_synthetic_frame(mock_gemini_http):
    sizzle_wav = generate_synthetic_sizzle_wav_base64("steady_applause_sizzle")
    resp = client.post(
        "/api/v1/sessions/analyze",
        json={
            "learner_id": "multimodal_audio_only",
            "dish_id": "sauteed_onions",
            "attempt_number": 2,
            "learner_reflection": "I waited for the steady sizzle sound before stirring.",
            "audio_wav_base64": sizzle_wav,
        },
    )
    assert resp.status_code == 200
    body = resp.json()
    assert body["vision_model_used"] == "No Camera Frame"
    assert body["reasoning_model_used"] == "gemini-3-flash-preview (Live Gemini API)"
    assert body["stage1_physical_observation"]["cv_metrics"] is None
    acoustic_metrics = body["stage1_physical_observation"]["acoustic_metrics"]
    assert acoustic_metrics is not None
    assert acoustic_metrics["inferred_acoustic_signature"] == "STEADY_APPLAUSE_SIZZLE"
    assert acoustic_metrics["spectral_centroid_hz"] > 2000.0

    # Audio-only: 1 Gemini coaching call (no scene gate, no fake image attached)
    assert len(mock_gemini_http.calls) == 1
    parts = mock_gemini_http.calls[0]["json"]["contents"][0]["parts"]
    assert any(p.get("inlineData", {}).get("mimeType") == "audio/wav" for p in parts[1:])
    assert not any(p.get("inlineData", {}).get("mimeType") == "image/jpeg" for p in parts[1:])


def test_analyze_session_fuses_audio_fft_with_vision_pipeline(mock_gemini_http):
    sizzle_wav = generate_synthetic_sizzle_wav_base64("steady_applause_sizzle")
    golden_frame = generate_synthetic_kitchen_frame_base64("golden_maillard")
    resp = client.post(
        "/api/v1/sessions/analyze",
        json={
            "learner_id": "multimodal_audio_vision",
            "dish_id": "sauteed_onions",
            "attempt_number": 2,
            "learner_reflection": "I waited for the steady sizzle sound and golden edges before stirring.",
            "keyframe_base64_list": [golden_frame],
            "audio_wav_base64": sizzle_wav,
        },
    )
    assert resp.status_code == 200
    body = resp.json()
    assert body["vision_model_used"] == "gemini-3-flash-preview + CV-Pixel + Audio-FFT"
    assert body["stage1_physical_observation"]["cv_metrics"] is not None
    assert body["stage1_physical_observation"]["acoustic_metrics"] is not None
    # Scene gate call + Coaching call = 2 Gemini REST calls
    assert len(mock_gemini_http.calls) == 2
