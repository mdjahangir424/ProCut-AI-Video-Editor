package com.example.core.model

import java.util.UUID

enum class AspectRatio(val widthRatio: Int, val heightRatio: Int, val label: String, val subtitle: String) {
    RATIO_9_16(9, 16, "9:16", "TikTok / Reels / Shorts"),
    RATIO_16_9(16, 9, "16:9", "YouTube / Landscape"),
    RATIO_1_1(1, 1, "1:1", "Square / Instagram Post"),
    RATIO_4_5(4, 5, "4:5", "Portrait / Instagram"),
    RATIO_3_4(3, 4, "3:4", "Feed / Classic");

    val floatRatio: Float
        get() = widthRatio.toFloat() / heightRatio.toFloat()
}

enum class VideoResolution(val width: Int, val height: Int, val label: String, val bitrate: Int) {
    HD_720P(1280, 720, "720p HD", 4_000_000),
    FHD_1080P(1920, 1080, "1080p Full HD", 8_000_000),
    UHD_4K(3840, 2160, "4K Ultra HD", 20_000_000)
}

enum class FrameRate(val fps: Int, val label: String) {
    FPS_24(24, "24 fps (Cinematic)"),
    FPS_30(30, "30 fps (Standard)"),
    FPS_60(60, "60 fps (Smooth)")
}

enum class FilterType(val displayName: String, val description: String) {
    NORMAL("Normal", "Original untouched colors"),
    WARM("Golden Warm", "Sunset golden tones"),
    COOL("Cyan Cool", "Modern cinematic teal tones"),
    VIVID("Vivid Pop", "High contrast and saturated colors"),
    MONOCHROME("B&W Noir", "Classic black and white film"),
    CINEMATIC("Blockbuster", "Rich cinematic color grading")
}

data class VideoClip(
    val id: String = UUID.randomUUID().toString(),
    val uriString: String,
    val name: String,
    val durationMs: Long,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = durationMs,
    val volume: Float = 1.0f,
    val speed: Float = 1.0f,
    val rotationDegrees: Int = 0,
    val filter: FilterType = FilterType.NORMAL,
    val isMuted: Boolean = false
) {
    val effectiveDurationMs: Long
        get() = ((trimEndMs - trimStartMs).coerceAtLeast(0L) / speed).toLong()
}

data class AudioClip(
    val id: String = UUID.randomUUID().toString(),
    val uriString: String,
    val name: String,
    val durationMs: Long,
    val timelineStartMs: Long = 0L,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = durationMs,
    val volume: Float = 0.8f,
    val isLooping: Boolean = false
) {
    val effectiveDurationMs: Long
        get() = (trimEndMs - trimStartMs).coerceAtLeast(0L)
}

data class TextOverlay(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val startTimeMs: Long = 0L,
    val endTimeMs: Long = 3000L,
    val positionX: Float = 0.0f, // -1f (left) to +1f (right), 0f center
    val positionY: Float = 0.6f, // -1f (top) to +1f (bottom), 0.6f bottom third
    val fontSizeSp: Float = 22f,
    val colorHex: String = "#FFFFFF",
    val backgroundColorHex: String = "#99000000"
)

data class EditingProject(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val aspectRatio: AspectRatio = AspectRatio.RATIO_9_16,
    val resolution: VideoResolution = VideoResolution.FHD_1080P,
    val frameRate: FrameRate = FrameRate.FPS_30,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val clips: List<VideoClip> = emptyList(),
    val audioClips: List<AudioClip> = emptyList(),
    val textOverlays: List<TextOverlay> = emptyList(),
    val thumbnailUri: String? = null
) {
    val totalDurationMs: Long
        get() = clips.sumOf { it.effectiveDurationMs }
}

data class ExportSettings(
    val resolution: VideoResolution = VideoResolution.FHD_1080P,
    val frameRate: FrameRate = FrameRate.FPS_30,
    val quality: String = "Standard", // "High", "Standard", "Low"
    val outputFormat: String = "MP4"
)

sealed interface ExportState {
    data object Idle : ExportState
    data object Preparing : ExportState
    data class InProgress(val progressPercentage: Int, val elapsedMs: Long) : ExportState
    data class Completed(
        val outputUriString: String,
        val filePath: String,
        val durationMs: Long,
        val fileSizeFormatted: String
    ) : ExportState
    data class Failed(val errorMessage: String) : ExportState
    data object Cancelled : ExportState
}
