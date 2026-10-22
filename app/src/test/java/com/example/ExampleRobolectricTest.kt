package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.database.ProjectJsonAdapter
import com.example.core.model.AspectRatio
import com.example.core.model.EditingProject
import com.example.core.model.FilterType
import com.example.core.model.FrameRate
import com.example.core.model.VideoClip
import com.example.core.model.VideoResolution
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("ProCut", appName)
    }

    @Test
    fun `test project serialization and deserialization with android json`() {
        val original = EditingProject(
            id = "proj-123",
            title = "Cinematic Reel",
            aspectRatio = AspectRatio.RATIO_9_16,
            resolution = VideoResolution.FHD_1080P,
            frameRate = FrameRate.FPS_60,
            clips = listOf(
                VideoClip(
                    id = "c1",
                    uriString = "content://video/test",
                    name = "Intro",
                    durationMs = 6000L,
                    trimStartMs = 500L,
                    trimEndMs = 5500L,
                    volume = 1.2f,
                    speed = 1.0f,
                    rotationDegrees = 90,
                    filter = FilterType.CINEMATIC,
                    isMuted = false
                )
            )
        )

        val json = ProjectJsonAdapter.toJson(original)
        assertNotNull(json)
        assertTrue(json.contains("Cinematic Reel"))

        val restored = ProjectJsonAdapter.fromJson(json)
        assertEquals(original.id, restored.id)
        assertEquals(original.title, restored.title)
        assertEquals(original.aspectRatio, restored.aspectRatio)
        assertEquals(original.resolution, restored.resolution)
        assertEquals(original.frameRate, restored.frameRate)
        assertEquals(1, restored.clips.size)

        val restoredClip = restored.clips.first()
        assertEquals("Intro", restoredClip.name)
        assertEquals(500L, restoredClip.trimStartMs)
        assertEquals(5500L, restoredClip.trimEndMs)
        assertEquals(90, restoredClip.rotationDegrees)
        assertEquals(FilterType.CINEMATIC, restoredClip.filter)
    }
}
