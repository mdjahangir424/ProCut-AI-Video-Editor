package com.example.feature.editor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.ai.AiManager
import com.example.core.model.AudioClip
import com.example.core.model.EditingProject
import com.example.core.model.FilterType
import com.example.core.model.TextOverlay
import com.example.core.model.VideoClip
import com.example.data.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class EditorViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProjectRepository(application)

    private val _project = MutableStateFlow<EditingProject?>(null)
    val project: StateFlow<EditingProject?> = _project.asStateFlow()

    private val _selectedClipId = MutableStateFlow<String?>(null)
    val selectedClipId: StateFlow<String?> = _selectedClipId.asStateFlow()

    private val _selectedTextId = MutableStateFlow<String?>(null)
    val selectedTextId: StateFlow<String?> = _selectedTextId.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _isAiGenerating = MutableStateFlow(false)
    val isAiGenerating: StateFlow<Boolean> = _isAiGenerating.asStateFlow()

    // Undo/Redo stacks
    private val undoStack = mutableListOf<EditingProject>()
    private val redoStack = mutableListOf<EditingProject>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    fun loadProject(projectId: String) {
        viewModelScope.launch {
            val p = repository.getProjectById(projectId)
            if (p != null) {
                _project.value = p
                _selectedClipId.value = p.clips.firstOrNull()?.id
                undoStack.clear()
                redoStack.clear()
                updateUndoRedoStates()
            }
        }
    }

    private fun pushStateForUndo() {
        val current = _project.value ?: return
        undoStack.add(current)
        if (undoStack.size > 20) {
            undoStack.removeAt(0)
        }
        redoStack.clear()
        updateUndoRedoStates()
    }

    private fun updateUndoRedoStates() {
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val current = _project.value ?: return
        redoStack.add(current)
        val previous = undoStack.removeAt(undoStack.lastIndex)
        _project.value = previous
        updateUndoRedoStates()
        autoSave(previous)
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val current = _project.value ?: return
        undoStack.add(current)
        val next = redoStack.removeAt(redoStack.lastIndex)
        _project.value = next
        updateUndoRedoStates()
        autoSave(next)
    }

    fun selectClip(clipId: String?) {
        _selectedClipId.value = clipId
    }

    fun addClips(newClips: List<VideoClip>) {
        val current = _project.value ?: return
        pushStateForUndo()
        val updated = current.copy(
            clips = current.clips + newClips,
            thumbnailUri = current.thumbnailUri ?: newClips.firstOrNull()?.uriString
        )
        _project.value = updated
        _selectedClipId.value = newClips.firstOrNull()?.id ?: _selectedClipId.value
        autoSave(updated)
    }

    fun splitClipAtPlayhead(currentPositionMs: Long): Boolean {
        val current = _project.value ?: return false
        if (current.clips.isEmpty()) return false

        // Determine which clip spans across currentPositionMs
        var accumulatedMs = 0L
        var targetIndex = -1
        var offsetInClip = 0L

        for (i in current.clips.indices) {
            val clip = current.clips[i]
            val clipEffectiveDuration = clip.effectiveDurationMs
            if (currentPositionMs in accumulatedMs..(accumulatedMs + clipEffectiveDuration)) {
                targetIndex = i
                offsetInClip = currentPositionMs - accumulatedMs
                break
            }
            accumulatedMs += clipEffectiveDuration
        }

        if (targetIndex == -1) return false
        val clipToSplit = current.clips[targetIndex]

        // Minimum 300ms from start and end to avoid zero-duration segments
        val splitSourceTimeMs = clipToSplit.trimStartMs + (offsetInClip * clipToSplit.speed).toLong()
        if (splitSourceTimeMs < clipToSplit.trimStartMs + 300L || splitSourceTimeMs > clipToSplit.trimEndMs - 300L) {
            return false
        }

        pushStateForUndo()

        val firstHalf = clipToSplit.copy(
            id = UUID.randomUUID().toString(),
            name = "${clipToSplit.name} (Part 1)",
            trimEndMs = splitSourceTimeMs
        )
        val secondHalf = clipToSplit.copy(
            id = UUID.randomUUID().toString(),
            name = "${clipToSplit.name} (Part 2)",
            trimStartMs = splitSourceTimeMs
        )

        val updatedClips = current.clips.toMutableList()
        updatedClips.removeAt(targetIndex)
        updatedClips.add(targetIndex, secondHalf)
        updatedClips.add(targetIndex, firstHalf)

        val updatedProject = current.copy(clips = updatedClips)
        _project.value = updatedProject
        _selectedClipId.value = secondHalf.id
        autoSave(updatedProject)
        return true
    }

    fun trimClip(clipId: String, trimStartMs: Long, trimEndMs: Long) {
        val current = _project.value ?: return
        pushStateForUndo()
        val updatedClips = current.clips.map { clip ->
            if (clip.id == clipId) {
                clip.copy(trimStartMs = trimStartMs, trimEndMs = trimEndMs)
            } else clip
        }
        val updated = current.copy(clips = updatedClips)
        _project.value = updated
        autoSave(updated)
    }

    fun deleteClip(clipId: String) {
        val current = _project.value ?: return
        pushStateForUndo()
        val updatedClips = current.clips.filterNot { it.id == clipId }
        val updated = current.copy(clips = updatedClips)
        _project.value = updated
        _selectedClipId.value = updatedClips.firstOrNull()?.id
        autoSave(updated)
    }

    fun duplicateClip(clipId: String) {
        val current = _project.value ?: return
        val index = current.clips.indexOfFirst { it.id == clipId }
        if (index == -1) return
        pushStateForUndo()

        val clip = current.clips[index]
        val duplicate = clip.copy(
            id = UUID.randomUUID().toString(),
            name = "${clip.name} (Copy)"
        )
        val updatedClips = current.clips.toMutableList()
        updatedClips.add(index + 1, duplicate)

        val updated = current.copy(clips = updatedClips)
        _project.value = updated
        _selectedClipId.value = duplicate.id
        autoSave(updated)
    }

    fun moveClip(clipId: String, delta: Int) {
        val current = _project.value ?: return
        val index = current.clips.indexOfFirst { it.id == clipId }
        if (index == -1) return
        val targetIndex = index + delta
        if (targetIndex !in current.clips.indices) return

        pushStateForUndo()
        val updatedClips = current.clips.toMutableList()
        val clip = updatedClips.removeAt(index)
        updatedClips.add(targetIndex, clip)

        val updated = current.copy(clips = updatedClips)
        _project.value = updated
        autoSave(updated)
    }

    fun setClipSpeed(clipId: String, speed: Float) {
        val current = _project.value ?: return
        pushStateForUndo()
        val updatedClips = current.clips.map {
            if (it.id == clipId) it.copy(speed = speed) else it
        }
        val updated = current.copy(clips = updatedClips)
        _project.value = updated
        autoSave(updated)
    }

    fun setClipVolume(clipId: String, volume: Float) {
        val current = _project.value ?: return
        pushStateForUndo()
        val updatedClips = current.clips.map {
            if (it.id == clipId) it.copy(volume = volume) else it
        }
        val updated = current.copy(clips = updatedClips)
        _project.value = updated
        autoSave(updated)
    }

    fun toggleClipMute(clipId: String) {
        val current = _project.value ?: return
        pushStateForUndo()
        val updatedClips = current.clips.map {
            if (it.id == clipId) it.copy(isMuted = !it.isMuted) else it
        }
        val updated = current.copy(clips = updatedClips)
        _project.value = updated
        autoSave(updated)
    }

    fun rotateClip(clipId: String) {
        val current = _project.value ?: return
        pushStateForUndo()
        val updatedClips = current.clips.map {
            if (it.id == clipId) it.copy(rotationDegrees = (it.rotationDegrees + 90) % 360) else it
        }
        val updated = current.copy(clips = updatedClips)
        _project.value = updated
        autoSave(updated)
    }

    fun setClipFilter(clipId: String, filter: FilterType) {
        val current = _project.value ?: return
        pushStateForUndo()
        val updatedClips = current.clips.map {
            if (it.id == clipId) it.copy(filter = filter) else it
        }
        val updated = current.copy(clips = updatedClips)
        _project.value = updated
        autoSave(updated)
    }

    fun addTextOverlay(textOverlay: TextOverlay) {
        val current = _project.value ?: return
        pushStateForUndo()
        val updated = current.copy(textOverlays = current.textOverlays + textOverlay)
        _project.value = updated
        _selectedTextId.value = textOverlay.id
        autoSave(updated)
    }

    fun deleteTextOverlay(id: String) {
        val current = _project.value ?: return
        pushStateForUndo()
        val updated = current.copy(textOverlays = current.textOverlays.filterNot { it.id == id })
        _project.value = updated
        autoSave(updated)
    }

    fun addAudioClip(audioClip: AudioClip) {
        val current = _project.value ?: return
        pushStateForUndo()
        val updated = current.copy(audioClips = current.audioClips + audioClip)
        _project.value = updated
        autoSave(updated)
    }

    fun deleteAudioClip(id: String) {
        val current = _project.value ?: return
        pushStateForUndo()
        val updated = current.copy(audioClips = current.audioClips.filterNot { it.id == id })
        _project.value = updated
        autoSave(updated)
    }

    fun generateAiSubtitles(language: String) {
        val current = _project.value ?: return
        if (current.clips.isEmpty()) return

        viewModelScope.launch {
            _isAiGenerating.value = true
            val subtitles = AiManager.generateSubtitles(
                totalDurationMs = current.totalDurationMs,
                language = language,
                topic = current.title
            )
            pushStateForUndo()
            val updated = current.copy(textOverlays = current.textOverlays + subtitles)
            _project.value = updated
            _isAiGenerating.value = false
            autoSave(updated)
        }
    }

    fun saveProjectExplicitly() {
        val current = _project.value ?: return
        viewModelScope.launch {
            _isSaving.value = true
            repository.saveProject(current)
            _isSaving.value = false
        }
    }

    private fun autoSave(project: EditingProject) {
        viewModelScope.launch {
            repository.saveProject(project)
        }
    }
}
