"""Computer Vision kitchen frame analyzer & TP-Link Tapo C100 RTSP URL builder."""

from __future__ import annotations

import asyncio
import base64
import io
import ipaddress
import socket
from typing import List
from urllib.parse import quote

import numpy as np
from PIL import Image, ImageDraw

from app.schemas.domain import ComputerVisionMetrics


class TapoLanDiscovery:
    """Discovers the workstation's active LAN IP and reachable Tapo RTSP cameras."""

    RTSP_PORT = 554
    ONVIF_PORT = 2020
    _SCAN_CONCURRENCY = 64

    @staticmethod
    def detect_local_lan_ip() -> str:
        """Resolves the active outbound IPv4 address without sending network traffic."""
        sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        try:
            sock.connect(("1.1.1.1", 80))
            ip = sock.getsockname()[0]
            if ip and not ip.startswith("127."):
                return ip
        except OSError:
            pass
        finally:
            sock.close()
        return "127.0.0.1"

    @staticmethod
    async def is_tcp_port_open(host: str, port: int, timeout_sec: float = 0.35) -> bool:
        """Probes whether a host accepts TCP connections on `port` within `timeout_sec`."""
        try:
            _, writer = await asyncio.wait_for(
                asyncio.open_connection(host, port),
                timeout=timeout_sec,
            )
            writer.close()
            try:
                await writer.wait_closed()
            except Exception:
                pass
            return True
        except Exception:
            return False

    @classmethod
    async def scan_subnet_for_rtsp(
        cls,
        lan_ip: str,
        port: int = RTSP_PORT,
        timeout_sec: float = 0.35,
    ) -> str | None:
        """Scans the local private `/24` subnet for an RTSP server (preferring Tapo ONVIF hosts)."""
        try:
            addr = ipaddress.IPv4Address(lan_ip)
            network = ipaddress.IPv4Network(f"{lan_ip}/24", strict=False)
        except ValueError:
            return None

        if addr.is_loopback or not addr.is_private:
            return None

        semaphore = asyncio.Semaphore(cls._SCAN_CONCURRENCY)

        async def _probe(candidate: str) -> str | None:
            async with semaphore:
                if await cls.is_tcp_port_open(candidate, port, timeout_sec=timeout_sec):
                    return candidate
                return None

        hosts = [str(host) for host in network.hosts() if str(host) != lan_ip]
        results = await asyncio.gather(*(_probe(ip) for ip in hosts))
        open_rtsp_hosts = [ip for ip in results if ip is not None]
        if not open_rtsp_hosts:
            return None

        for ip in open_rtsp_hosts:
            if await cls.is_tcp_port_open(ip, cls.ONVIF_PORT, timeout_sec=timeout_sec):
                return ip
        return open_rtsp_hosts[0]

    @classmethod
    async def discover_tapo_camera(
        cls,
        preferred_ip: str | None = None,
        configured_ip: str | None = None,
        lan_ip: str | None = None,
        port: int = RTSP_PORT,
    ) -> tuple[str | None, bool, str]:
        """Resolves a reachable Tapo camera IP, falling back from preferred -> env -> LAN scan."""
        direct_candidates: list[tuple[str, str]] = []
        for raw_ip, source in ((preferred_ip, "preferred"), (configured_ip, "env_config")):
            cleaned = (raw_ip or "").strip()
            if cleaned and all(existing != cleaned for existing, _ in direct_candidates):
                direct_candidates.append((cleaned, source))

        for candidate_ip, source in direct_candidates:
            if await cls.is_tcp_port_open(candidate_ip, port, timeout_sec=0.35):
                return candidate_ip, True, source

        active_lan_ip = lan_ip or cls.detect_local_lan_ip()
        scanned_ip = await cls.scan_subnet_for_rtsp(active_lan_ip, port=port)
        if scanned_ip:
            return scanned_ip, True, "lan_scan"

        fallback_ip = direct_candidates[0][0] if direct_candidates else None
        return fallback_ip, False, "unavailable"


class TapoRtspUrlBuilder:
    """Builds safe, RFC-3986 encoded RTSP URLs for TP-Link Tapo C100 / C110 / C200 cameras."""

    DEFAULT_TAPO_IP = "192.168.11.16"

    @staticmethod
    def build_url(
        ip_address: str = DEFAULT_TAPO_IP,
        username: str = "",
        password: str = "",
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

    @classmethod
    def capture_live_frame_base64(
        cls,
        ip_address: str = DEFAULT_TAPO_IP,
        username: str = "",
        password: str = "",
        use_substream: bool = True,
        timeout_sec: float = 6.0,
    ) -> str:
        """Captures a single live JPEG frame from the Tapo C100 RTSP stream over TCP using ffmpeg."""
        import subprocess

        rtsp_url = cls.build_url(
            ip_address=ip_address,
            username=username,
            password=password,
            use_substream=use_substream,
        )
        cmd = [
            "ffmpeg",
            "-y",
            "-rtsp_transport",
            "tcp",
            "-i",
            rtsp_url,
            "-frames:v",
            "1",
            "-f",
            "image2pipe",
            "-vcodec",
            "mjpeg",
            "pipe:1",
        ]
        proc = subprocess.run(
            cmd,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            timeout=timeout_sec,
            check=False,
        )
        if proc.returncode != 0 or not proc.stdout:
            err_tail = proc.stderr.decode("utf-8", errors="ignore")[-300:]
            raise RuntimeError(f"Failed to capture RTSP frame from {clean_ip if (clean_ip := ip_address) else ''}: {err_tail}")
        return base64.b64encode(proc.stdout).decode("utf-8")


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
