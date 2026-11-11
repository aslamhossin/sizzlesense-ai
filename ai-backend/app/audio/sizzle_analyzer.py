"""Real-time FFT Acoustic Sizzle Spectral Analyzer for culinary pan state perception."""

from __future__ import annotations

import base64
import io
import math
import wave

import numpy as np

from app.schemas.domain import AcousticSizzleSignature, AcousticSpectralMetrics


class AcousticSizzleAnalyzer:
    """
    Extracts objective physical acoustic metrics from 16-bit PCM WAV audio clips using Hann-windowed FFT:
      - Low-frequency boiling/bubbling band (80 Hz - 700 Hz): water cavitation near <=100C
      - High-frequency Maillard crackle band (2,000 Hz - 7,500 Hz): steam micro-jet sizzle at 140C-165C
      - Crest-factor & transient pop detection: high-temperature oil spitting / scorching (>185C)
    """

    SILENCE_RMS_DB_THRESHOLD = -44.0

    @classmethod
    def analyze_wav_bytes(cls, wav_bytes: bytes) -> AcousticSpectralMetrics:
        with wave.open(io.BytesIO(wav_bytes), "rb") as wf:
            n_channels = wf.getnchannels()
            sampwidth = wf.getsampwidth()
            sample_rate = wf.getframerate()
            n_frames = wf.getnframes()
            raw_pcm = wf.readframes(n_frames)

        if sampwidth != 2 or n_frames == 0:
            raise ValueError("Expected non-empty 16-bit PCM WAV audio.")

        samples_i16 = np.frombuffer(raw_pcm, dtype=np.int16).astype(np.float32)
        if n_channels > 1:
            samples_i16 = samples_i16.reshape(-1, n_channels).mean(axis=1)

        normalized = samples_i16 / 32768.0
        rms = float(np.sqrt(np.mean(np.square(normalized))) + 1e-9)
        rms_db = float(20.0 * math.log10(rms))
        peak = float(np.max(np.abs(normalized)) + 1e-9)
        crest_factor = peak / rms

        window = np.hanning(len(normalized))
        spectrum = np.fft.rfft(normalized * window)
        power = np.square(np.abs(spectrum))
        freqs = np.fft.rfftfreq(len(normalized), d=1.0 / float(sample_rate))

        # Ignore DC / sub-40Hz rumble
        valid_mask = freqs >= 40.0
        valid_freqs = freqs[valid_mask]
        valid_power = power[valid_mask]
        total_power = float(np.sum(valid_power))

        if total_power <= 1e-12 or rms_db < cls.SILENCE_RMS_DB_THRESHOLD:
            return AcousticSpectralMetrics(
                dominant_frequency_hz=0.0,
                spectral_centroid_hz=0.0,
                high_freq_sizzle_ratio=0.0,
                low_freq_bubble_ratio=0.0,
                rms_energy_db=round(rms_db, 2),
                inferred_acoustic_signature=AcousticSizzleSignature.SILENT_COLD,
            )

        dominant_idx = int(np.argmax(valid_power))
        dominant_freq = float(valid_freqs[dominant_idx])
        centroid_hz = float(np.sum(valid_freqs * valid_power) / total_power)

        low_mask = (valid_freqs >= 80.0) & (valid_freqs <= 700.0)
        high_mask = (valid_freqs >= 2000.0) & (valid_freqs <= 7500.0)

        low_ratio = float(np.sum(valid_power[low_mask]) / total_power)
        high_ratio = float(np.sum(valid_power[high_mask]) / total_power)

        if high_ratio >= 0.35 and crest_factor >= 5.2:
            signature = AcousticSizzleSignature.AGGRESSIVE_POPPING_BURN
        elif high_ratio >= 0.32 and centroid_hz >= 1700.0:
            signature = AcousticSizzleSignature.STEADY_APPLAUSE_SIZZLE
        else:
            signature = AcousticSizzleSignature.WET_STEAM_BUBBLE

        return AcousticSpectralMetrics(
            dominant_frequency_hz=round(dominant_freq, 1),
            spectral_centroid_hz=round(centroid_hz, 1),
            high_freq_sizzle_ratio=round(min(max(high_ratio, 0.0), 1.0), 3),
            low_freq_bubble_ratio=round(min(max(low_ratio, 0.0), 1.0), 3),
            rms_energy_db=round(rms_db, 2),
            inferred_acoustic_signature=signature,
        )

    @classmethod
    def analyze_wav_base64(cls, wav_b64: str) -> AcousticSpectralMetrics:
        clean_b64 = wav_b64.split(",")[-1].strip()
        raw_bytes = base64.b64decode(clean_b64)
        return cls.analyze_wav_bytes(raw_bytes)


def generate_synthetic_sizzle_wav_base64(
    regime: str = "wet_steam_bubble",
    duration_sec: float = 0.5,
    sample_rate: int = 16000,
) -> str:
    """
    Synthesizes deterministic 16kHz mono 16-bit PCM WAV waveforms for the 4 culinary acoustic regimes:
      - `silent_cold`: low ambient kitchen floor (-58 dBFS)
      - `wet_steam_bubble`: 180 Hz + 340 Hz boiling water cavitation harmonics
      - `steady_applause_sizzle`: broadband 2.4 kHz - 5.2 kHz continuous Maillard sizzle
      - `aggressive_popping_burn`: sparse high-crest-factor 3.8 kHz + 5.8 kHz impulse pops
    """
    rng = np.random.default_rng(seed=42)
    n_samples = int(duration_sec * sample_rate)
    t = np.linspace(0.0, duration_sec, n_samples, endpoint=False)

    if regime == "silent_cold":
        signal = 0.0008 * rng.standard_normal(n_samples)
    elif regime == "wet_steam_bubble":
        signal = (
            0.22 * np.sin(2.0 * np.pi * 185.0 * t)
            + 0.14 * np.sin(2.0 * np.pi * 340.0 * t)
            + 0.03 * rng.standard_normal(n_samples)
        )
    elif regime == "steady_applause_sizzle":
        signal = (
            0.16 * np.sin(2.0 * np.pi * 2650.0 * t)
            + 0.14 * np.sin(2.0 * np.pi * 3850.0 * t)
            + 0.11 * np.sin(2.0 * np.pi * 5100.0 * t)
            + 0.12 * rng.standard_normal(n_samples)
        )
    elif regime == "aggressive_popping_burn":
        signal = 0.015 * rng.standard_normal(n_samples)
        # Inject sharp high-frequency pyrolysis pop impulses (high crest factor)
        pop_indices = np.arange(800, n_samples - 200, 1600)
        for idx in pop_indices:
            burst_t = np.arange(90) / float(sample_rate)
            envelope = np.exp(-burst_t * 140.0)
            burst = 0.92 * envelope * (
                0.6 * np.sin(2.0 * np.pi * 3900.0 * burst_t)
                + 0.4 * np.sin(2.0 * np.pi * 5900.0 * burst_t)
            )
            signal[idx : idx + 90] += burst
    else:
        raise ValueError(f"Unknown synthetic acoustic regime: {regime}")

    clipped = np.clip(signal, -0.98, 0.98)
    pcm_i16 = (clipped * 32767.0).astype(np.int16)

    buf = io.BytesIO()
    with wave.open(buf, "wb") as wf:
        wf.setnchannels(1)
        wf.setsampwidth(2)
        wf.setframerate(sample_rate)
        wf.writeframes(pcm_i16.tobytes())

    return base64.b64encode(buf.getvalue()).decode("utf-8")
