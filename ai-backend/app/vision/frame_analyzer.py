"""Computer Vision kitchen frame analyzer & TP-Link Tapo C100 RTSP URL builder."""

from __future__ import annotations

import base64
import io
from typing import List
from urllib.parse import quote

import numpy as np
from PIL import Image, ImageDraw

from app.schemas.domain import ComputerVisionMetrics


class TapoRtspUrlBuilder:
    """Builds safe, RFC-3986 encoded RTSP URLs for TP-Link Tapo C100 / C110 / C200 cameras."""

    @staticmethod
    def build_url(
        ip_address: str,
        username: str,
        password: str,
        use_substream: bool = False,
        port: int = 554,
    ) -> str:
        clean_ip = ip_address.strip()
        if not clean_ip:
            raise ValueError("Tapo camera IP address must not be empty.")
        encoded_user = quote(username.strip(), safe="")
        encoded_pass = quote(password, safe="")
        stream_path = "stream2" if use_substream else "stream1"
        return f"rtsp://{encoded_user}:{encoded_pass}@{clean_ip}:{port}/{stream_path}"


class KitchenFrameAnalyzer:
    """Extracts objective culinary pixel metrics (Maillard browning, moisture/steam glare, char) from overhead frames."""

    @staticmethod
    def analyze_image(image: Image.Image) -> ComputerVisionMetrics:
        rgb = image.convert("RGB").resize((128, 128))
        arr = np.asarray(rgb, dtype=np.float32)

        r = arr[:, :, 0]
        g = arr[:, :, 1]
        b = arr[:, :, 2]

        total_pixels = float(arr.shape[0] * arr.shape[1])
        mean_luminance = float(np.mean(0.299 * r + 0.587 * g + 0.114 * b))

        # 1. Golden-brown Maillard zone: warm amber/brown (R > G > B with strong warm separation)
        maillard_mask = (
            (r > 110.0)
            & (r < 235.0)
            & (g > 55.0)
            & (g < 175.0)
            & (b < 95.0)
            & ((r - b) > 55.0)
            & ((r - g) > 20.0)
        )
        maillard_ratio = float(np.sum(maillard_mask)) / total_pixels

        # 2. High-luminance pale/translucent & specular water glare (steaming onions / pooling liquid)
        moisture_mask = (
            (r > 195.0)
            & (g > 195.0)
            & (b > 180.0)
            & (np.abs(r - g) < 25.0)
        )
        moisture_index = float(np.sum(moisture_mask)) / total_pixels

        # 3. Carbonized / scorched pixels (very low RGB luminance)
        char_mask = (r < 42.0) & (g < 38.0) & (b < 38.0)
        char_ratio = float(np.sum(char_mask)) / total_pixels

        if char_ratio >= 0.35:
            regime = "SCORCHED_CARBONIZED"
        elif maillard_ratio >= 0.35:
            regime = "GOLDEN_MAILLARD_BROWNING"
        elif moisture_index >= 0.30 or mean_luminance > 185.0:
            regime = "PALE_STEAMING_OR_RAW"
        else:
            regime = "TRANSITIONAL_SAUTE"

        return ComputerVisionMetrics(
            maillard_browning_ratio=round(min(max(maillard_ratio, 0.0), 1.0), 3),
            surface_moisture_index=round(min(max(moisture_index, 0.0), 1.0), 3),
            char_burn_ratio=round(min(max(char_ratio, 0.0), 1.0), 3),
            mean_luminance=round(mean_luminance, 2),
            dominant_visual_regime=regime,
        )

    @classmethod
    def analyze_base64_frame(cls, b64_string: str) -> ComputerVisionMetrics:
        clean_b64 = b64_string.split(",")[-1].strip()
        raw_bytes = base64.b64decode(clean_b64)
        img = Image.open(io.BytesIO(raw_bytes))
        return cls.analyze_image(img)

    @classmethod
    def analyze_frame_sequence(cls, b64_frames: List[str]) -> ComputerVisionMetrics:
        """Aggregates CV metrics across multiple keyframes (using the final frame as primary outcome state)."""
        if not b64_frames:
            raise ValueError("At least one frame is required for sequence analysis.")
        return cls.analyze_base64_frame(b64_frames[-1])


def generate_synthetic_kitchen_frame_base64(regime: str = "pale_steaming") -> str:
    """Generates a deterministic overhead skillet image for testing & zero-camera simulation."""
    img = Image.new("RGB", (128, 128), color=(90, 90, 95))
    draw = ImageDraw.Draw(img)

    if regime == "pale_steaming":
        # Pale translucent onions + bright specular water pool
        draw.rectangle([10, 10, 118, 118], fill=(225, 224, 212))
        draw.ellipse([28, 28, 100, 100], fill=(242, 242, 235))
    elif regime == "golden_maillard":
        # Rich amber-gold caramelized onions
        draw.rectangle([10, 10, 118, 118], fill=(182, 112, 42))
        draw.ellipse([25, 25, 103, 103], fill=(196, 124, 48))
    elif regime == "scorched_burnt":
        # Carbonized black scorching in skillet center
        draw.rectangle([10, 10, 118, 118], fill=(22, 18, 18))
        draw.ellipse([25, 25, 103, 103], fill=(15, 12, 12))
    else:
        draw.rectangle([10, 10, 118, 118], fill=(150, 135, 105))

    buf = io.BytesIO()
    img.save(buf, format="JPEG", quality=90)
    return base64.b64encode(buf.getvalue()).decode("utf-8")
