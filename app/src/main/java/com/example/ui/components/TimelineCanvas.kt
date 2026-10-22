package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.AudioClip
import com.example.core.model.TextOverlay
import com.example.core.model.VideoClip
import com.example.ui.theme.AudioTrackColor
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PlayheadColor
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateDarkBg
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.TextTrackColor
import com.example.ui.theme.TrimHandleYellow
import com.example.ui.theme.VideoTrackColor
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun TimelineCanvas(
    clips: List<VideoClip>,
    audioClips: List<AudioClip>,
    textOverlays: List<TextOverlay>,
    totalDurationMs: Long,
    currentPositionMs: Long,
    selectedClipId: String?,
    onSelectClip: (String?) -> Unit,
    onSeek: (Long) -> Unit,
    onAddClipClick: () -> Unit,
    pixelsPerSecond: Float = 60f, // Zoom level
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val scrollState = rememberScrollState()

    // Calculate total timeline width in dp
    val totalSeconds = (totalDurationMs / 1000f).coerceAtLeast(5f)
    val totalTimelineWidthDp = (totalSeconds * pixelsPerSecond + 200).dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SlateSurface)
            .border(width = 1.dp, color = SlateBorder)
    ) {
        // Track Header Labels & Ruler
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .background(SlateCard)
                .horizontalScroll(scrollState)
                .pointerInput(totalDurationMs, pixelsPerSecond) {
                    detectTapGestures { offset ->
                        val clickedSec = offset.x / (pixelsPerSecond * density.density)
                        val targetMs = (clickedSec * 1000).toLong().coerceIn(0L, totalDurationMs)
                        onSeek(targetMs)
                    }
                }
        ) {
            // Time Ruler Canvas
            Canvas(
                modifier = Modifier
                    .width(totalTimelineWidthDp)
                    .height(30.dp)
            ) {
                val secInterval = if (pixelsPerSecond < 40f) 5 else if (pixelsPerSecond < 80f) 2 else 1
                val totalSecsInt = totalSeconds.toInt() + 2

                for (s in 0..totalSecsInt step secInterval) {
                    val x = s * pixelsPerSecond * density.density
                    // Major tick
                    drawLine(
                        color = SlateBorder,
                        start = Offset(x, 10f),
                        end = Offset(x, size.height),
                        strokeWidth = 2f
                    )
                }
            }

            // Time Marks in seconds
            val secInterval = if (pixelsPerSecond < 40f) 5 else if (pixelsPerSecond < 80f) 2 else 1
            val totalSecsInt = totalSeconds.toInt() + 2
            for (s in 0..totalSecsInt step secInterval) {
                val xDp = (s * pixelsPerSecond).dp
                Text(
                    text = "${s}s",
                    color = TextMutedDark,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .offset(x = xDp + 4.dp, y = 4.dp)
                )
            }
        }

        // Multi-track scrollable body
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .horizontalScroll(scrollState)
                .pointerInput(totalDurationMs, pixelsPerSecond) {
                    detectTapGestures { offset ->
                        val clickedSec = offset.x / (pixelsPerSecond * density.density)
                        val targetMs = (clickedSec * 1000).toLong().coerceIn(0L, totalDurationMs)
                        onSeek(targetMs)
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .width(totalTimelineWidthDp)
                    .padding(vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 1. Text Overlay Track
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    textOverlays.forEach { overlay ->
                        val startSec = overlay.startTimeMs / 1000f
                        val durationSec = ((overlay.endTimeMs - overlay.startTimeMs) / 1000f).coerceAtLeast(0.5f)
                        val startDp = (startSec * pixelsPerSecond).dp
                        val widthDp = (durationSec * pixelsPerSecond).dp

                        Box(
                            modifier = Modifier
                                .offset(x = startDp)
                                .width(widthDp)
                                .height(22.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(TextTrackColor)
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "T: ${overlay.text}",
                                color = Color.White,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // 2. Main Video Track
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    clips.forEachIndexed { index, clip ->
                        val durationSec = (clip.effectiveDurationMs / 1000f).coerceAtLeast(0.2f)
                        val clipWidthDp = (durationSec * pixelsPerSecond).dp
                        val isSelected = clip.id == selectedClipId

                        Box(
                            modifier = Modifier
                                .width(clipWidthDp)
                                .height(60.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(VideoTrackColor)
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) TrimHandleYellow else SlateBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    onSelectClip(if (isSelected) null else clip.id)
                                }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxHeight(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Videocam,
                                        contentDescription = null,
                                        tint = if (isSelected) TrimHandleYellow else NeonCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = clip.name,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${formatDuration(clip.effectiveDurationMs)} (x${clip.speed})",
                                        color = TextSecondaryDark,
                                        fontSize = 10.sp
                                    )
                                    if (clip.filter.name != "NORMAL") {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(Color(0x6600E5FF))
                                                .padding(horizontal = 3.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = clip.filter.displayName.take(4),
                                                color = Color.White,
                                                fontSize = 8.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // Trim handles on selected clip
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .width(6.dp)
                                        .fillMaxHeight()
                                        .background(TrimHandleYellow)
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .width(6.dp)
                                        .fillMaxHeight()
                                        .background(TrimHandleYellow)
                                )
                            }
                        }
                    }

                    // Add Clip Button on timeline
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SlateCard)
                            .border(1.dp, SlateBorder, RoundedCornerShape(8.dp))
                            .clickable { onAddClipClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Clip",
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // 3. Audio Track
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    audioClips.forEach { audio ->
                        val startSec = audio.timelineStartMs / 1000f
                        val durationSec = (audio.effectiveDurationMs / 1000f).coerceAtLeast(0.5f)
                        val startDp = (startSec * pixelsPerSecond).dp
                        val widthDp = (durationSec * pixelsPerSecond).dp

                        Box(
                            modifier = Modifier
                                .offset(x = startDp)
                                .width(widthDp)
                                .height(22.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(AudioTrackColor)
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = Color(0xFF6EE7B7),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = audio.name,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Playhead indicator (Vertical Red/Cyan bar with needle cap)
            val playheadSec = currentPositionMs / 1000f
            val playheadOffsetDp = (playheadSec * pixelsPerSecond).dp

            Box(
                modifier = Modifier
                    .offset(x = playheadOffsetDp)
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(PlayheadColor)
            ) {
                // Playhead Pin Cap at top
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .offset(x = (-5).dp, y = 0.dp)
                        .clip(CircleShape)
                        .background(PlayheadColor)
                )
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    val sec = ms / 1000
    val rem = (ms % 1000) / 100
    return String.format(Locale.US, "%d.%ds", sec, rem)
}
