package com.example.core.media

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.annotation.OptIn
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.ScaleAndRotateTransformation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import com.example.core.model.EditingProject
import com.example.core.model.ExportSettings
import com.example.core.model.ExportState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(UnstableApi::class)
class TransformerExportManager(private val context: Context) {
    private val appContext = context.applicationContext

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    private var activeTransformer: Transformer? = null
    private var progressPollingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var activeOutputFile: File? = null

    suspend fun startExport(
        project: EditingProject,
        settings: ExportSettings
    ) = withContext(Dispatchers.Main) {
        if (_exportState.value is ExportState.InProgress || _exportState.value is ExportState.Preparing) {
            return@withContext
        }

        if (project.clips.isEmpty()) {
            _exportState.value = ExportState.Failed("No video clips in project to export.")
            return@withContext
        }

        _exportState.value = ExportState.Preparing

        val moviesDir = appContext.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
            ?: appContext.filesDir
        if (!moviesDir.exists()) {
            moviesDir.mkdirs()
        }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val cleanTitle = project.title.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(24)
        val outputFile = File(moviesDir, "ProCut_${cleanTitle}_$timestamp.mp4")
        activeOutputFile = outputFile

        try {
            val editedItems = project.clips.map { clip ->
                val clippingConfig = MediaItem.ClippingConfiguration.Builder()
                    .setStartPositionMs(clip.trimStartMs.coerceAtLeast(0L))
                    .setEndPositionMs(
                        if (clip.trimEndMs > clip.trimStartMs) clip.trimEndMs else clip.durationMs
                    )
                    .build()

                val mediaItem = MediaItem.Builder()
                    .setUri(Uri.parse(clip.uriString))
                    .setClippingConfiguration(clippingConfig)
                    .build()

                val effectsList = mutableListOf<Effect>()
                if (clip.rotationDegrees != 0) {
                    val rotateEffect = ScaleAndRotateTransformation.Builder()
                        .setRotationDegrees(clip.rotationDegrees.toFloat())
                        .build()
                    effectsList.add(rotateEffect)
                }

                val effects = Effects(/* audioProcessors= */ emptyList(), /* videoEffects= */ effectsList)

                EditedMediaItem.Builder(mediaItem)
                    .setRemoveAudio(clip.isMuted)
                    .setEffects(effects)
                    .build()
            }

            val sequence = EditedMediaItemSequence.Builder(editedItems).build()
            val composition = Composition.Builder(listOf(sequence)).build()

            val listener = object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    stopProgressPolling()
                    activeTransformer = null
                    val fileSizeFormatted = formatFileSize(outputFile.length())
                    _exportState.value = ExportState.Completed(
                        outputUriString = Uri.fromFile(outputFile).toString(),
                        filePath = outputFile.absolutePath,
                        durationMs = exportResult.durationMs,
                        fileSizeFormatted = fileSizeFormatted
                    )
                }

                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException
                ) {
                    stopProgressPolling()
                    activeTransformer = null
                    outputFile.delete()
                    val msg = exportException.localizedMessage ?: "Video export encoding failed."
                    _exportState.value = ExportState.Failed(msg)
                }
            }

            val transformer = Transformer.Builder(appContext)
                .setVideoMimeType(MimeTypes.VIDEO_H264)
                .setAudioMimeType(MimeTypes.AUDIO_AAC)
                .addListener(listener)
                .build()

            activeTransformer = transformer
            transformer.start(composition, outputFile.absolutePath)
            _exportState.value = ExportState.InProgress(progressPercentage = 0, elapsedMs = 0L)

            startProgressPolling(transformer)

        } catch (e: Exception) {
            outputFile.delete()
            activeTransformer = null
            _exportState.value = ExportState.Failed(e.localizedMessage ?: "Failed to initialize video encoder.")
        }
    }

    private fun startProgressPolling(transformer: Transformer) {
        stopProgressPolling()
        val startTime = System.currentTimeMillis()
        val progressHolder = ProgressHolder()

        progressPollingJob = scope.launch {
            while (isActive) {
                val state = transformer.getProgress(progressHolder)
                if (state == Transformer.PROGRESS_STATE_AVAILABLE) {
                    val pct = progressHolder.progress.coerceIn(0, 100)
                    val elapsed = System.currentTimeMillis() - startTime
                    _exportState.value = ExportState.InProgress(progressPercentage = pct, elapsedMs = elapsed)
                }
                delay(300)
            }
        }
    }

    private fun stopProgressPolling() {
        progressPollingJob?.cancel()
        progressPollingJob = null
    }

    fun cancelExport() {
        stopProgressPolling()
        activeTransformer?.cancel()
        activeTransformer = null
        activeOutputFile?.delete()
        _exportState.value = ExportState.Cancelled
    }

    fun resetState() {
        _exportState.value = ExportState.Idle
    }

    private fun formatFileSize(bytes: Long): String {
        val mb = bytes / (1024.0 * 1024.0)
        return String.format(Locale.US, "%.1f MB", mb)
    }
}
