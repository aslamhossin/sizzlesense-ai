"""Tests for Atomic Step 2: Computer Vision Frame Analyzer & Tapo C100 RTSP URL Builder."""

import base64
import io
import pytest
from PIL import Image

from app.vision.frame_analyzer import (
    KitchenFrameAnalyzer,
    TapoRtspUrlBuilder,
    generate_synthetic_kitchen_frame_base64,
)


def test_tapo_rtsp_url_builder_standard_and_substream():
    """Verify official TP-Link Tapo C100 stream1 (1080p) and stream2 (360p) URLs."""
    url_hd = TapoRtspUrlBuilder.build_url(
        ip_address="192.168.11.16",
        username="chef",
        password="password123",
        use_substream=False,
    )
    assert url_hd == "rtsp://chef:password123@192.168.11.16:554/stream1"

    url_sub = TapoRtspUrlBuilder.build_url(
        ip_address="192.168.11.16",
        username="chef",
        password="password123",
        use_substream=True,
    )
    assert url_sub == "rtsp://chef:password123@192.168.11.16:554/stream2"


def test_tapo_rtsp_url_builder_encodes_special_characters():
    """Passwords with '@', ':', or '/' must be URL-encoded so RTSP URI parsing never breaks."""
    url = TapoRtspUrlBuilder.build_url(
        ip_address="192.168.1.88",
        username="cook@home",
        password="p@ss:w/rd!",
        use_substream=False,
    )
    assert url == "rtsp://cook%40home:p%40ss%3Aw%2Frd%21@192.168.1.88:554/stream1"


def test_cv_analyzer_detects_pale_steaming_regime():
    """A pale/high-glare frame (steaming onions at 100°C) should yield high moisture & low browning."""
    b64_pale = generate_synthetic_kitchen_frame_base64(regime="pale_steaming")
    metrics = KitchenFrameAnalyzer.analyze_base64_frame(b64_pale)

    assert metrics.surface_moisture_index > 0.35
    assert metrics.maillard_browning_ratio < 0.20
    assert metrics.dominant_visual_regime == "PALE_STEAMING_OR_RAW"


def test_cv_analyzer_detects_golden_maillard_browning_regime():
    """An amber/golden-brown frame (145°C-165°C Maillard zone) should yield high browning ratio."""
    b64_golden = generate_synthetic_kitchen_frame_base64(regime="golden_maillard")
    metrics = KitchenFrameAnalyzer.analyze_base64_frame(b64_golden)

    assert metrics.maillard_browning_ratio > 0.45
    assert metrics.char_burn_ratio < 0.15
    assert metrics.dominant_visual_regime == "GOLDEN_MAILLARD_BROWNING"


def test_cv_analyzer_detects_scorched_carbonized_regime():
    """A dark carbonized frame (>185°C scorching) should trigger high char_burn_ratio."""
    b64_burnt = generate_synthetic_kitchen_frame_base64(regime="scorched_burnt")
    metrics = KitchenFrameAnalyzer.analyze_base64_frame(b64_burnt)

    assert metrics.char_burn_ratio > 0.40
    assert metrics.dominant_visual_regime == "SCORCHED_CARBONIZED"
