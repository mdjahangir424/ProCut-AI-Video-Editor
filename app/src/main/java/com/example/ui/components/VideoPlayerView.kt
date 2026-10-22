package com.example.ui.components

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.example.core.media.PlayerManager
import com.example.core.model.AspectRatio
import com.example.core.model.TextOverlay
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PlayheadColor
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateCard
import java.util.Locale

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerView(
    playerManager: PlayerManager,
    aspectRatio: AspectRatio,
    currentPositionMs: Long,
    totalDurationMs: Long,
    isPlaying: Boolean,
    activeTextOverlays: List<TextOverlay>,
    playbackError: String?,
    onTogglePlayPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Aspect ratio container
        Box(
            modifier = Modifier
                .aspectRatio(aspectRatio.floatRatio)
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onTogglePlayPause()
                },
            contentAlignment = Alignment.Center
        ) {
            // AndroidX Media3 PlayerView
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        useController = false
                        player = playerManager.player
                    }
                },
                update = { view ->
                    if (view.player != playerManager.player) {
                        view.player = playerManager.player
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Render active text overlays on top of video at current timestamp
            activeTextOverlays.forEach { overlay ->
                if (currentPositionMs in overlay.startTimeMs..overlay.endTimeMs) {
                    Box(
                        modifier = Modifier
                            .align(
                                when {
                                    overlay.positionY < -0.3f -> Alignment.TopCenter
                                    overlay.positionY > 0.3f -> Alignment.BottomCenter
                                    else -> Alignment.Center
                                }
                            )
                            .padding(horizontal = 24.dp, vertical = 20.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(parseHexColor(overlay.backgroundColorHex, Color(0x990A0D14)))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = overlay.text,
                            color = parseHexColor(overlay.colorHex, Color.White),
                            fontSize = overlay.fontSizeSp.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Big Play button overlay when paused
            if (!isPlaying && playbackError == null) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0x990A0D14))
                        .clickable { onTogglePlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = NeonCyan,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // Playback Error indicator
            if (playbackError != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xCC0A0D14))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Preview Error: $playbackError",
                        color = PlayheadColor,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Timecode badge in bottom corner
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xCC131824))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "${formatTimecode(currentPositionMs)} / ${formatTimecode(totalDurationMs)}",
                color = NeonCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun formatTimecode(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val millis = (ms % 1000) / 10
    return String.format(Locale.US, "%02d:%02d.%02d", minutes, seconds, millis)
}

private fun parseHexColor(hex: String, fallback: Color): Color {
    return runCatching {
        val clean = hex.removePrefix("#")
        val colorInt = when (clean.length) {
            6 -> (0xFF000000 or clean.toLong(16)).toInt()
            8 -> clean.toLong(16).toInt()
            else -> return fallback
        }
        Color(colorInt)
    }.getOrDefault(fallback)
}
