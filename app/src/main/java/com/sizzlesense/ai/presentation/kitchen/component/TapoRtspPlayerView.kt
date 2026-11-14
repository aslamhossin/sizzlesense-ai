package com.sizzlesense.ai.presentation.kitchen.component

import android.graphics.Bitmap
import android.util.Base64
import android.view.TextureView
import androidx.annotation.OptIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.rtsp.RtspMediaSource
import com.sizzlesense.ai.domain.model.camera.TapoCameraConfig
import com.sizzlesense.ai.presentation.common.component.SizzleSectionCard
import com.sizzlesense.ai.presentation.theme.SizzleTheme
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(UnstableApi::class)
@Composable
fun TapoRtspPlayerView(
    config: TapoCameraConfig,
    onConnectToggle: (Boolean) -> Unit,
    onFrameCaptured: (base64Jpeg: String, label: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
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

    DisposableEffect(exoPlayer, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> exoPlayer?.pause()
                Lifecycle.Event.ON_START -> {
                    if (config.isConnected) {
                        exoPlayer?.play()
                    }
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer?.release()
        }
    }

    SizzleSectionCard(
        title = "Stove Camera",
        subtitle = if (config.isConnected) "Streaming • ${config.ipAddress}" else "Offline",
        icon = Icons.Default.Videocam,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    if (config.isConnected) SizzleTheme.spacing.videoPreviewHeight else SizzleTheme.spacing.videoIdleHeight
                )
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
                    modifier = Modifier.padding(SizzleTheme.spacing.md)
                ) {
                    Icon(
                        imageVector = Icons.Default.VideocamOff,
                        contentDescription = null,
                        tint = SizzleTheme.colors.textSecondary,
                        modifier = Modifier.size(SizzleTheme.spacing.iconMedium)
                    )
                    Text(
                        text = "Configure credentials in Setup below to preview your pan",
                        color = SizzleTheme.colors.textSecondary,
                        style = SizzleTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
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
                Text(
                    text = if (config.isConnected) "Stop Camera" else "Start Camera",
                    maxLines = 1
                )
            }

            OutlinedButton(
                onClick = {
                    val bmp = textureViewRef?.bitmap ?: return@OutlinedButton
                    coroutineScope.launch {
                        val (b64, ts) = withContext(Dispatchers.Default) {
                            val scaled = Bitmap.createScaledBitmap(bmp, 256, 256, true)
                            val out = ByteArrayOutputStream()
                            scaled.compress(Bitmap.CompressFormat.JPEG, 85, out)
                            val encoded = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
                            val timestamp = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
                            encoded to timestamp
                        }
                        onFrameCaptured(b64, "Camera frame ($ts)")
                    }
                },
                enabled = config.isConnected,
                border = BorderStroke(
                    width = SizzleTheme.spacing.borderStroke,
                    color = if (config.isConnected) {
                        SizzleTheme.colors.outlineDefault
                    } else {
                        SizzleTheme.colors.outlineSubtle
                    }
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = SizzleTheme.colors.textPrimary,
                    disabledContentColor = SizzleTheme.colors.textSecondary.copy(alpha = 0.55f)
                ),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = null,
                    modifier = Modifier.size(SizzleTheme.spacing.iconSmall)
                )
                Spacer(modifier = Modifier.width(SizzleTheme.spacing.xs))
                Text(
                    text = "Snap Frame",
                    maxLines = 1
                )
            }
        }
    }
}
