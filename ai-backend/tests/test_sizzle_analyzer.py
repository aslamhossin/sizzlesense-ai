"""Tests for FFT Acoustic Sizzle Spectral Analyzer & Multimodal Audio+Vision Session Analysis."""

from fastapi.testclient import TestClient

from app.audio.sizzle_analyzer import (
    AcousticSizzleAnalyzer,
    generate_synthetic_sizzle_wav_base64,
)
from app.main import app
from app.schemas.domain import AcousticSizzleSignature

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


def test_analyze_session_fuses_audio_fft_with_vision_pipeline():
    sizzle_wav = generate_synthetic_sizzle_wav_base64("steady_applause_sizzle")
    resp = client.post(
        "/api/v1/sessions/analyze",
        json={
            "learner_id": "multimodal_audio_tester",
            "dish_id": "sauteed_onions",
            "attempt_number": 2,
            "learner_reflection": "I waited for the steady sizzle sound and golden edges before stirring.",
            "audio_wav_base64": sizzle_wav,
        },
    )
    assert resp.status_code == 200
    body = resp.json()
    assert "Audio-FFT" in body["vision_model_used"]
    acoustic_metrics = body["stage1_physical_observation"]["acoustic_metrics"]
    assert acoustic_metrics is not None
    assert acoustic_metrics["inferred_acoustic_signature"] == "STEADY_APPLAUSE_SIZZLE"
    assert acoustic_metrics["spectral_centroid_hz"] > 2000.0
    assert any(
        "Acoustic FFT measured" in s
        for s in body["stage1_physical_observation"]["inferred_physical_states"]
    )
