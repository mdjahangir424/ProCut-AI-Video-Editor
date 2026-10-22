package com.example.core.database

import com.example.core.model.AspectRatio
import com.example.core.model.AudioClip
import com.example.core.model.EditingProject
import com.example.core.model.FilterType
import com.example.core.model.FrameRate
import com.example.core.model.TextOverlay
import com.example.core.model.VideoClip
import com.example.core.model.VideoResolution
import org.json.JSONArray
import org.json.JSONObject

object ProjectJsonAdapter {

    fun toJson(project: EditingProject): String {
        val root = JSONObject()
        root.put("id", project.id)
        root.put("title", project.title)
        root.put("aspectRatio", project.aspectRatio.name)
        root.put("resolution", project.resolution.name)
        root.put("frameRate", project.frameRate.name)
        root.put("createdAt", project.createdAt)
        root.put("updatedAt", project.updatedAt)
        root.put("thumbnailUri", project.thumbnailUri ?: "")

        val clipsArray = JSONArray()
        for (clip in project.clips) {
            val c = JSONObject()
            c.put("id", clip.id)
            c.put("uriString", clip.uriString)
            c.put("name", clip.name)
            c.put("durationMs", clip.durationMs)
            c.put("trimStartMs", clip.trimStartMs)
            c.put("trimEndMs", clip.trimEndMs)
            c.put("volume", clip.volume.toDouble())
            c.put("speed", clip.speed.toDouble())
            c.put("rotationDegrees", clip.rotationDegrees)
            c.put("filter", clip.filter.name)
            c.put("isMuted", clip.isMuted)
            clipsArray.put(c)
        }
        root.put("clips", clipsArray)

        val audioArray = JSONArray()
        for (audio in project.audioClips) {
            val a = JSONObject()
            a.put("id", audio.id)
            a.put("uriString", audio.uriString)
            a.put("name", audio.name)
            a.put("durationMs", audio.durationMs)
            a.put("timelineStartMs", audio.timelineStartMs)
            a.put("trimStartMs", audio.trimStartMs)
            a.put("trimEndMs", audio.trimEndMs)
            a.put("volume", audio.volume.toDouble())
            a.put("isLooping", audio.isLooping)
            audioArray.put(a)
        }
        root.put("audioClips", audioArray)

        val textArray = JSONArray()
        for (text in project.textOverlays) {
            val t = JSONObject()
            t.put("id", text.id)
            t.put("text", text.text)
            t.put("startTimeMs", text.startTimeMs)
            t.put("endTimeMs", text.endTimeMs)
            t.put("positionX", text.positionX.toDouble())
            t.put("positionY", text.positionY.toDouble())
            t.put("fontSizeSp", text.fontSizeSp.toDouble())
            t.put("colorHex", text.colorHex)
            t.put("backgroundColorHex", text.backgroundColorHex)
            textArray.put(t)
        }
        root.put("textOverlays", textArray)

        return root.toString()
    }

    fun fromJson(jsonStr: String): EditingProject {
        val root = JSONObject(jsonStr)
        val id = root.optString("id")
        val title = root.optString("title", "Untitled Project")
        val aspectRatio = runCatching { AspectRatio.valueOf(root.optString("aspectRatio")) }.getOrDefault(AspectRatio.RATIO_9_16)
        val resolution = runCatching { VideoResolution.valueOf(root.optString("resolution")) }.getOrDefault(VideoResolution.FHD_1080P)
        val frameRate = runCatching { FrameRate.valueOf(root.optString("frameRate")) }.getOrDefault(FrameRate.FPS_30)
        val createdAt = root.optLong("createdAt", System.currentTimeMillis())
        val updatedAt = root.optLong("updatedAt", System.currentTimeMillis())
        val thumbnailUri = root.optString("thumbnailUri").takeIf { it.isNotBlank() }

        val clips = mutableListOf<VideoClip>()
        val clipsArray = root.optJSONArray("clips") ?: JSONArray()
        for (i in 0 until clipsArray.length()) {
            val c = clipsArray.getJSONObject(i)
            clips.add(
                VideoClip(
                    id = c.optString("id"),
                    uriString = c.optString("uriString"),
                    name = c.optString("name", "Clip ${i + 1}"),
                    durationMs = c.optLong("durationMs", 0L),
                    trimStartMs = c.optLong("trimStartMs", 0L),
                    trimEndMs = c.optLong("trimEndMs", c.optLong("durationMs", 0L)),
                    volume = c.optDouble("volume", 1.0).toFloat(),
                    speed = c.optDouble("speed", 1.0).toFloat(),
                    rotationDegrees = c.optInt("rotationDegrees", 0),
                    filter = runCatching { FilterType.valueOf(c.optString("filter")) }.getOrDefault(FilterType.NORMAL),
                    isMuted = c.optBoolean("isMuted", false)
                )
            )
        }

        val audioClips = mutableListOf<AudioClip>()
        val audioArray = root.optJSONArray("audioClips") ?: JSONArray()
        for (i in 0 until audioArray.length()) {
            val a = audioArray.getJSONObject(i)
            audioClips.add(
                AudioClip(
                    id = a.optString("id"),
                    uriString = a.optString("uriString"),
                    name = a.optString("name", "Audio ${i + 1}"),
                    durationMs = a.optLong("durationMs", 0L),
                    timelineStartMs = a.optLong("timelineStartMs", 0L),
                    trimStartMs = a.optLong("trimStartMs", 0L),
                    trimEndMs = a.optLong("trimEndMs", a.optLong("durationMs", 0L)),
                    volume = a.optDouble("volume", 0.8).toFloat(),
                    isLooping = a.optBoolean("isLooping", false)
                )
            )
        }

        val textOverlays = mutableListOf<TextOverlay>()
        val textArray = root.optJSONArray("textOverlays") ?: JSONArray()
        for (i in 0 until textArray.length()) {
            val t = textArray.getJSONObject(i)
            textOverlays.add(
                TextOverlay(
                    id = t.optString("id"),
                    text = t.optString("text"),
                    startTimeMs = t.optLong("startTimeMs", 0L),
                    endTimeMs = t.optLong("endTimeMs", 3000L),
                    positionX = t.optDouble("positionX", 0.0).toFloat(),
                    positionY = t.optDouble("positionY", 0.6).toFloat(),
                    fontSizeSp = t.optDouble("fontSizeSp", 22.0).toFloat(),
                    colorHex = t.optString("colorHex", "#FFFFFF"),
                    backgroundColorHex = t.optString("backgroundColorHex", "#99000000")
                )
            )
        }

        return EditingProject(
            id = id,
            title = title,
            aspectRatio = aspectRatio,
            resolution = resolution,
            frameRate = frameRate,
            createdAt = createdAt,
            updatedAt = updatedAt,
            clips = clips,
            audioClips = audioClips,
            textOverlays = textOverlays,
            thumbnailUri = thumbnailUri
        )
    }
}
