package com.example.core.media

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.core.model.EditingProject
import com.example.core.model.VideoClip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class PlayerManager(context: Context) {
    private val appContext = context.applicationContext

    private var exoPlayer: ExoPlayer? = null
    val player: Player?
        get() = exoPlayer

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _totalDurationMs = MutableStateFlow(0L)
    val totalDurationMs: StateFlow<Long> = _totalDurationMs.asStateFlow()

    private val _playbackError = MutableStateFlow<String?>(null)
    val playbackError: StateFlow<String?> = _playbackError.asStateFlow()

    private var currentClips: List<VideoClip> = emptyList()
    private var trackingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        initPlayer()
    }

    private fun initPlayer() {
        if (exoPlayer != null) return
        exoPlayer = ExoPlayer.Builder(appContext).build().apply {
            repeatMode = Player.REPEAT_MODE_OFF
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    _isPlaying.value = playing
                    if (playing) {
                        startProgressTracking()
                    } else {
                        stopProgressTracking()
                        updateTimelinePosition()
                    }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        _isPlaying.value = false
                        stopProgressTracking()
                        _currentPositionMs.value = _totalDurationMs.value
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    _playbackError.value = error.localizedMessage ?: "Playback error: ${error.errorCodeName}"
                    _isPlaying.value = false
                    stopProgressTracking()
                }
            })
        }
    }

    fun setProject(project: EditingProject) {
        val player = exoPlayer ?: return
        currentClips = project.clips
        val totalMs = project.totalDurationMs
        _totalDurationMs.value = totalMs
        _playbackError.value = null

        if (project.clips.isEmpty()) {
            player.clearMediaItems()
            _currentPositionMs.value = 0L
            return
        }

        val mediaItems = project.clips.map { clip ->
            val clippingConfig = MediaItem.ClippingConfiguration.Builder()
                .setStartPositionMs(clip.trimStartMs.coerceAtLeast(0L))
                .setEndPositionMs(
                    if (clip.trimEndMs > clip.trimStartMs) clip.trimEndMs else clip.durationMs
                )
                .build()

            MediaItem.Builder()
                .setUri(Uri.parse(clip.uriString))
                .setMediaId(clip.id)
                .setClippingConfiguration(clippingConfig)
                .build()
        }

        val wasPlaying = player.isPlaying
        val previousPosition = _currentPositionMs.value

        player.setMediaItems(mediaItems)
        player.prepare()

        if (previousPosition > 0L && previousPosition < totalMs) {
            seekToTimelinePosition(previousPosition)
        }
        if (wasPlaying) {
            player.play()
        }
    }

    fun play() {
        exoPlayer?.let {
            if (it.playbackState == Player.STATE_ENDED) {
                seekToTimelinePosition(0L)
            }
            it.play()
        }
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }

    fun seekToTimelinePosition(targetPositionMs: Long) {
        val player = exoPlayer ?: return
        if (currentClips.isEmpty()) {
            _currentPositionMs.value = 0L
            return
        }

        val clampedPos = targetPositionMs.coerceIn(0L, _totalDurationMs.value)
        _currentPositionMs.value = clampedPos

        var accumulatedMs = 0L
        for (i in currentClips.indices) {
            val clip = currentClips[i]
            val clipDuration = clip.effectiveDurationMs
            if (clampedPos <= accumulatedMs + clipDuration || i == currentClips.lastIndex) {
                val clipRelativeMs = (clampedPos - accumulatedMs).coerceAtLeast(0L)
                player.seekTo(i, clipRelativeMs)
                break
            }
            accumulatedMs += clipDuration
        }
    }

    private fun startProgressTracking() {
        stopProgressTracking()
        trackingJob = scope.launch {
            while (isActive) {
                updateTimelinePosition()
                delay(40) // ~25-30fps UI update rate
            }
        }
    }

    private fun stopProgressTracking() {
        trackingJob?.cancel()
        trackingJob = null
    }

    private fun updateTimelinePosition() {
        val player = exoPlayer ?: return
        if (currentClips.isEmpty()) {
            _currentPositionMs.value = 0L
            return
        }

        val currentMediaItemIndex = player.currentMediaItemIndex
        if (currentMediaItemIndex < 0 || currentMediaItemIndex >= currentClips.size) {
            return
        }

        var accumulatedMs = 0L
        for (i in 0 until currentMediaItemIndex) {
            accumulatedMs += currentClips[i].effectiveDurationMs
        }

        val positionInCurrentClip = player.currentPosition.coerceAtLeast(0L)
        val calculatedPosition = (accumulatedMs + positionInCurrentClip).coerceAtMost(_totalDurationMs.value)
        _currentPositionMs.value = calculatedPosition
    }

    fun release() {
        stopProgressTracking()
        exoPlayer?.release()
        exoPlayer = null
    }
}
