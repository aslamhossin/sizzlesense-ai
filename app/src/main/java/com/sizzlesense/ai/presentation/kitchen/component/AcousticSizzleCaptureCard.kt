package com.sizzlesense.ai.presentation.kitchen.component

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import com.sizzlesense.ai.domain.model.coaching.AcousticSizzleSignature
import com.sizzlesense.ai.presentation.common.component.SizzleSectionCard
import com.sizzlesense.ai.presentation.theme.SizzleTheme
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.Base64
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Encodes 16-bit mono PCM audio into RFC-compliant 44-byte RIFF WAV Base64 payloads
 * and synthesizes physical stove-top acoustic waveforms for zero-stove testing.
 */
object PcmWavSizzleCodec {

    const val SAMPLE_RATE_HZ = 16_000

    fun encodePcm16ToWavBase64(
        pcmSamples: ShortArray,
        sampleRate: Int = SAMPLE_RATE_HZ
    ): String {
        val dataByteCount = pcmSamples.size * 2
        val totalChunkSize = 36 + dataByteCount
        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".toByteArray(Charsets.US_ASCII))
            putInt(totalChunkSize)
            put("WAVE".toByteArray(Charsets.US_ASCII))
            put("fmt ".toByteArray(Charsets.US_ASCII))
            putInt(16) // PCM chunk size
            putShort(1) // AudioFormat = 1 (PCM)
            putShort(1) // NumChannels = 1 (Mono)
            putInt(sampleRate)
            putInt(sampleRate * 2) // ByteRate = SampleRate * Channels * BitsPerSample/8
            putShort(2) // BlockAlign = Channels * BitsPerSample/8
            putShort(16) // BitsPerSample = 16
            put("data".toByteArray(Charsets.US_ASCII))
            putInt(dataByteCount)
        }

        val pcmBuffer = ByteBuffer.allocate(dataByteCount).order(ByteOrder.LITTLE_ENDIAN)
        for (sample in pcmSamples) {
            pcmBuffer.putShort(sample)
        }

        val out = ByteArrayOutputStream(44 + dataByteCount)
        out.write(header.array())
        out.write(pcmBuffer.array())
        return Base64.getEncoder().encodeToString(out.toByteArray())
    }

    fun synthesizePresetWavBase64(
        signature: AcousticSizzleSignature,
        durationMs: Int = 450,
        sampleRate: Int = SAMPLE_RATE_HZ
    ): String {
        val numSamples = (sampleRate * durationMs) / 1000
        val rng = Random(42)
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate.toDouble()
            val raw = when (signature) {
                AcousticSizzleSignature.SILENT_COLD -> {
                    0.0007 * (rng.nextDouble(-1.0, 1.0))
                }

                AcousticSizzleSignature.WET_STEAM_BUBBLE -> {
                    0.24 * sin(2.0 * PI * 185.0 * t) +
                        0.15 * sin(2.0 * PI * 340.0 * t) +
                        0.03 * rng.nextDouble(-1.0, 1.0)
                }

                AcousticSizzleSignature.STEADY_APPLAUSE_SIZZLE -> {
                    0.17 * sin(2.0 * PI * 2650.0 * t) +
                        0.15 * sin(2.0 * PI * 3850.0 * t) +
                        0.12 * sin(2.0 * PI * 5100.0 * t) +
                        0.12 * rng.nextDouble(-1.0, 1.0)
                }

                AcousticSizzleSignature.AGGRESSIVE_POPPING_BURN -> {
                    val phaseInCycle = i % 1600
                    val popBurst = if (phaseInCycle < 90) {
                        val bt = phaseInCycle.toDouble() / sampleRate.toDouble()
                        0.92 * exp(-bt * 140.0) * (
                            0.6 * sin(2.0 * PI * 3900.0 * bt) +
                                0.4 * sin(2.0 * PI * 5900.0 * bt)
                            )
                    } else {
                        0.0
                    }
                    popBurst + 0.015 * rng.nextDouble(-1.0, 1.0)
                }
            }
            val clamped = raw.coerceIn(-0.98, 0.98)
            pcm[i] = (clamped * 32767.0).toInt().toShort()
        }

        return encodePcm16ToWavBase64(pcm, sampleRate)
    }

    @SuppressLint("MissingPermission")
    suspend fun recordLiveMicWavBase64(durationMs: Int = 1500): String = withContext(Dispatchers.IO) {
        val minBuf = AudioRecord.getMinBufferSize(
            SAMPLE_RATE_HZ,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(SAMPLE_RATE_HZ)

        val targetSamples = (SAMPLE_RATE_HZ * durationMs) / 1000
        val collected = ShortArray(targetSamples)
        val recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE_HZ,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minBuf * 2
        )

        try {
            check(recorder.state == AudioRecord.STATE_INITIALIZED) {
                "Microphone AudioRecord failed to initialize."
            }
            recorder.startRecording()
            var offset = 0
            while (offset < targetSamples) {
                val read = recorder.read(collected, offset, targetSamples - offset)
                if (read <= 0) break
                offset += read
            }
        } finally {
            runCatching { recorder.stop() }
            recorder.release()
        }

        encodePcm16ToWavBase64(collected, SAMPLE_RATE_HZ)
    }
}

@Composable
fun AcousticSizzleCaptureCard(
    capturedAudioLabel: String,
    onAudioCaptured: (base64Wav: String, label: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isRecording by remember { mutableStateOf(false) }
    var selectedPreset by remember { mutableStateOf<AcousticSizzleSignature?>(null) }

    fun launchLiveRecording() {
        if (isRecording) return
        isRecording = true
        scope.launch {
            runCatching {
                PcmWavSizzleCodec.recordLiveMicWavBase64(durationMs = 1500)
            }.onSuccess { wavB64 ->
                selectedPreset = null
                val stamp = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
                onAudioCaptured(wavB64, "Live mic recorded • $stamp")
            }.onFailure { err ->
                onAudioCaptured("", "Mic unavailable: ${err.message ?: "check permission"}")
            }
            isRecording = false
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchLiveRecording()
        } else {
            onAudioCaptured("", "Mic permission needed — or pick a sample below")
        }
    }

    SizzleSectionCard(
        title = "Pan Sound",
        subtitle = capturedAudioLabel,
        icon = Icons.Default.GraphicEq
    ) {
        Button(
            enabled = !isRecording,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val hasPerm = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
                if (hasPerm) {
                    launchLiveRecording()
                } else {
                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = null,
                modifier = Modifier.size(SizzleTheme.spacing.iconSmall)
            )
            Spacer(modifier = Modifier.width(SizzleTheme.spacing.xs))
            Text(
                text = if (isRecording) "Listening (1.5s)..." else "Record Pan (1.5s)",
                maxLines = 1
            )
        }

        Text(
            text = "Or test with a sample sound:",
            style = SizzleTheme.typography.bodySmall,
            color = SizzleTheme.colors.textSecondary
        )

        val presetRows = listOf(
            listOf(
                AcousticSizzleSignature.WET_STEAM_BUBBLE to "Wet Bubble",
                AcousticSizzleSignature.STEADY_APPLAUSE_SIZZLE to "Crisp Sizzle"
            ),
            listOf(
                AcousticSizzleSignature.AGGRESSIVE_POPPING_BURN to "Harsh Scorch",
                AcousticSizzleSignature.SILENT_COLD to "Cold Pan"
            )
        )

        Column(verticalArrangement = Arrangement.spacedBy(SizzleTheme.spacing.xxs)) {
            presetRows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SizzleTheme.spacing.xs)
                ) {
                    rowItems.forEach { (sig, label) ->
                        FilterChip(
                            selected = selectedPreset == sig,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedPreset = sig
                                scope.launch(Dispatchers.Default) {
                                    val wavB64 = PcmWavSizzleCodec.synthesizePresetWavBase64(sig)
                                    withContext(Dispatchers.Main) {
                                        onAudioCaptured(wavB64, "Sample: $label")
                                    }
                                }
                            },
                            label = {
                                Text(
                                    text = label,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
