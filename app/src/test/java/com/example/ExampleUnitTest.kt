package com.example

import com.example.core.config.CreatorInfo
import com.example.core.model.AspectRatio
import com.example.core.model.EditingProject
import com.example.core.model.VideoClip
import com.example.core.model.VideoResolution
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testCreatorAttributionAndContactInfo() {
        assertEquals("Md Jahangir Alam", CreatorInfo.DEVELOPER_NAME)
        assertEquals("+8801778600028", CreatorInfo.CONTACT_PHONE)
        assertEquals("mdjahangiralam2528@gmail.com", CreatorInfo.CONTACT_EMAIL)
        assertEquals("ProCut AI Video Editor", CreatorInfo.APP_NAME)
        assertTrue(CreatorInfo.COPYRIGHT_NOTICE.contains("Md Jahangir Alam"))
        assertTrue(CreatorInfo.COPYRIGHT_NOTICE.contains("2026"))
        assertTrue(CreatorInfo.CREATOR_ATTRIBUTION.contains("Md Jahangir Alam"))
        assertTrue(CreatorInfo.THIRD_PARTY_LICENSES.isNotEmpty())
    }

    @Test
    fun testClipEffectiveDuration() {
        val clip = VideoClip(
            uriString = "content://media/1",
            name = "Test Clip",
            durationMs = 10000L,
            trimStartMs = 2000L,
            trimEndMs = 8000L,
            speed = 2.0f
        )
        // (8000 - 2000) / 2.0 = 3000ms
        assertEquals(3000L, clip.effectiveDurationMs)
    }

    @Test
    fun testTimelineTotalDuration() {
        val clip1 = VideoClip(
            uriString = "content://media/1",
            name = "Clip 1",
            durationMs = 5000L
        )
        val clip2 = VideoClip(
            uriString = "content://media/2",
            name = "Clip 2",
            durationMs = 8000L,
            trimStartMs = 1000L,
            trimEndMs = 5000L // 4000ms
        )
        val project = EditingProject(
            title = "Test Project",
            clips = listOf(clip1, clip2)
        )
        assertEquals(9000L, project.totalDurationMs)
    }

    @Test
    fun testSplitMath() {
        val clip = VideoClip(
            id = "original",
            uriString = "content://media/1",
            name = "Full Clip",
            durationMs = 10000L,
            trimStartMs = 0L,
            trimEndMs = 10000L
        )

        val splitOffset = 4000L
        val splitSourceTimeMs = clip.trimStartMs + splitOffset

        val firstHalf = clip.copy(trimEndMs = splitSourceTimeMs)
        val secondHalf = clip.copy(trimStartMs = splitSourceTimeMs)

        assertEquals(4000L, firstHalf.effectiveDurationMs)
        assertEquals(6000L, secondHalf.effectiveDurationMs)
        assertEquals(10000L, firstHalf.effectiveDurationMs + secondHalf.effectiveDurationMs)
    }

    @Test
    fun testAspectRatioRatios() {
        assertEquals(9f / 16f, AspectRatio.RATIO_9_16.floatRatio, 0.001f)
        assertEquals(16f / 9f, AspectRatio.RATIO_16_9.floatRatio, 0.001f)
        assertEquals(1f, AspectRatio.RATIO_1_1.floatRatio, 0.001f)
    }

    @Test
    fun testResolutionBitrates() {
        assertEquals(1920, VideoResolution.FHD_1080P.width)
        assertEquals(1080, VideoResolution.FHD_1080P.height)
        assertEquals(8_000_000, VideoResolution.FHD_1080P.bitrate)
    }
}
