"""Shared test-only synthetic image helpers (kept strictly outside production `app/`)."""

from __future__ import annotations

import base64
import io
from PIL import Image, ImageDraw


def generate_synthetic_kitchen_frame_base64(regime: str = "pale_steaming") -> str:
    """Generates deterministic synthetic skillet frames for automated tests only."""
    img = Image.new("RGB", (128, 128), color=(90, 90, 95))
    draw = ImageDraw.Draw(img)

    if regime == "pale_steaming":
        draw.rectangle([10, 10, 118, 118], fill=(225, 224, 212))
        draw.ellipse([28, 28, 100, 100], fill=(242, 242, 235))
    elif regime == "golden_maillard":
        draw.rectangle([10, 10, 118, 118], fill=(182, 112, 42))
        draw.ellipse([25, 25, 103, 103], fill=(196, 124, 48))
    elif regime == "scorched_burnt":
        draw.rectangle([10, 10, 118, 118], fill=(22, 18, 18))
        draw.ellipse([25, 25, 103, 103], fill=(15, 12, 12))
    else:
        draw.rectangle([10, 10, 118, 118], fill=(150, 135, 105))

    buf = io.BytesIO()
    img.save(buf, format="JPEG", quality=90)
    return base64.b64encode(buf.getvalue()).decode("utf-8")
