package com.sizzlesense.ai.presentation.kitchen.component

import android.graphics.Bitmap
import android.util.Base64
import android.view.TextureView
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.rtsp.RtspMediaSource
import com.sizzlesense.ai.domain.model.camera.TapoCameraConfig
import com.sizzlesense.ai.presentation.theme.SizzleTheme
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(UnstableApi::class)
@Composable
fun TapoRtspPlayerView(
    config: TapoCameraConfig,
    onConnectToggle: (Boolean) -> Unit,
    onFrameCaptured: (base64Jpeg: String, label: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var textureViewRef by remember { mutableStateOf<TextureView?>(null) }

    val exoPlayer = remember(
        config.ipAddress,
        config.username,
        config.password,
        config.useSubStream,
        config.isConnected
    ) {
        if (!config.isConnected) {
            null
        } else {
            ExoPlayer.Builder(context).build().apply {
                val rtspUrl = config.buildRtspUrl()
                val mediaSource = RtspMediaSource.Factory()
                    .setForceUseRtpTcp(true)
                    .createMediaSource(MediaItem.fromUri(rtspUrl))
                setMediaSource(mediaSource)
                prepare()
                playWhenReady = true
            }
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer?.release()
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SizzleTheme.spacing.xs)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(SizzleTheme.spacing.videoPreviewHeight)
                .clip(SizzleTheme.radius.md)
                .background(SizzleTheme.colors.videoCanvasBackground),
            contentAlignment = Alignment.Center
        ) {
            if (config.isConnected && exoPlayer != null) {
                AndroidView(
                    factory = { ctx ->
                        TextureView(ctx).also { tv ->
                            textureViewRef = tv
                            exoPlayer.setVideoTextureView(tv)
                        }
                    },
                    update = { tv ->
                        textureViewRef = tv
                        exoPlayer.setVideoTextureView(tv)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(SizzleTheme.spacing.xs),
                    modifier = Modifier.padding(SizzleTheme.spacing.lg)
                ) {
                    Text(
                        text = "Tapo C100 Overhead Kitchen Tracker (${config.ipAddress}:554)",
                        color = SizzleTheme.colors.textPrimary,
                        style = SizzleTheme.typography.titleSmall
                    )
                    Text(
                        text = "Enter your Tapo Camera Account credentials below to stream live RTSP video or capture directly via the Python AI Backend.",
                        color = SizzleTheme.colors.textSecondary,
                        style = SizzleTheme.typography.bodySmall
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SizzleTheme.spacing.xs)
        ) {
            Button(
                onClick = { onConnectToggle(!config.isConnected) },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (config.isConnected) "Stop RTSP Stream" else "Stream Tapo C100 Live")
            }

            OutlinedButton(
                onClick = {
                    val bmp = textureViewRef?.bitmap
                    if (bmp != null) {
                        val scaled = Bitmap.createScaledBitmap(bmp, 256, 256, true)
                        val out = ByteArrayOutputStream()
                        scaled.compress(Bitmap.CompressFormat.JPEG, 85, out)
                        val b64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
                        val ts = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
                        onFrameCaptured(b64, "Captured from ExoPlayer RTSP at $ts")
                    }
                },
                enabled = config.isConnected,
                modifier = Modifier.weight(1f)
            ) {
                Text("Grab Frame from Stream")
            }
        }
    }
}
