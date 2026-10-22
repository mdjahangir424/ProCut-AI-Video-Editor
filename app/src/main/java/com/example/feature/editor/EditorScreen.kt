package com.example.feature.editor

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.core.media.PlayerManager
import com.example.core.model.AspectRatio
import com.example.core.model.AudioClip
import com.example.core.model.EditingProject
import com.example.core.model.FilterType
import com.example.core.model.TextOverlay
import com.example.core.model.VideoClip
import com.example.ui.components.AiSubtitleDialog
import com.example.ui.components.ClipFilterDialog
import com.example.ui.components.ClipTrimDialog
import com.example.ui.components.TextOverlayDialog
import com.example.ui.components.TimelineCanvas
import com.example.ui.components.VideoPlayerView
import com.example.ui.theme.GoldenAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PlayheadColor
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateDarkBg
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.TrimHandleYellow
import java.util.UUID

@Composable
fun EditorScreen(
    projectId: String,
    onNavigateBack: () -> Unit,
    onNavigateExport: (String) -> Unit,
    viewModel: EditorViewModel = viewModel()
) {
    val context = LocalContext.current
    val project by viewModel.project.collectAsState()
    val selectedClipId by viewModel.selectedClipId.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val isAiGenerating by viewModel.isAiGenerating.collectAsState()

    // Dialog states
    var showTrimDialog by remember { mutableStateOf(false) }
    var showTextDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showAiSubtitleDialog by remember { mutableStateOf(false) }
    var timelineZoom by remember { mutableFloatStateOf(60f) }

    // Player manager instance
    val playerManager = remember { PlayerManager(context) }
    val isPlaying by playerManager.isPlaying.collectAsState()
    val currentPositionMs by playerManager.currentPositionMs.collectAsState()
    val totalDurationMs by playerManager.totalDurationMs.collectAsState()
    val playbackError by playerManager.playbackError.collectAsState()

    // Back handler
    BackHandler {
        playerManager.release()
        onNavigateBack()
    }

    DisposableEffect(Unit) {
        onDispose {
            playerManager.release()
        }
    }

    // Load project
    LaunchedEffect(projectId) {
        viewModel.loadProject(projectId)
    }

    // Update player when project state changes
    LaunchedEffect(project) {
        project?.let {
            playerManager.setProject(it)
        }
    }

    // Photo/Video picker launcher
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            val newClips = uris.mapIndexed { index, uri ->
                // Query metadata or default duration to 5000ms
                VideoClip(
                    id = UUID.randomUUID().toString(),
                    uriString = uri.toString(),
                    name = "Clip ${((project?.clips?.size ?: 0) + index + 1)}",
                    durationMs = 5000L,
                    trimStartMs = 0L,
                    trimEndMs = 5000L
                )
            }
            viewModel.addClips(newClips)
            Toast.makeText(context, "Added ${newClips.size} media clip(s)", Toast.LENGTH_SHORT).show()
        }
    }

    // Audio picker launcher
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val audio = AudioClip(
                id = UUID.randomUUID().toString(),
                uriString = it.toString(),
                name = "Audio ${((project?.audioClips?.size ?: 0) + 1)}",
                durationMs = 15000L,
                timelineStartMs = 0L,
                trimStartMs = 0L,
                trimEndMs = 15000L
            )
            viewModel.addAudioClip(audio)
            Toast.makeText(context, "Added background audio", Toast.LENGTH_SHORT).show()
        }
    }

    val activeProject = project
    val selectedClip = activeProject?.clips?.firstOrNull { it.id == selectedClipId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // 1. TOP BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        playerManager.release()
                        onNavigateBack()
                    },
                    modifier = Modifier.testTag("back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Column(modifier = Modifier.padding(start = 4.dp)) {
                    Text(
                        text = activeProject?.title ?: "Video Editor",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 140.dp)
                    )
                    Text(
                        text = activeProject?.aspectRatio?.label ?: "9:16",
                        color = NeonCyan,
                        fontSize = 11.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Undo
                IconButton(
                    onClick = { viewModel.undo() },
                    enabled = canUndo,
                    modifier = Modifier.size(36.dp).testTag("undo_button")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        tint = if (canUndo) Color.White else TextMutedDark
                    )
                }

                // Redo
                IconButton(
                    onClick = { viewModel.redo() },
                    enabled = canRedo,
                    modifier = Modifier.size(36.dp).testTag("redo_button")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Redo",
                        tint = if (canRedo) Color.White else TextMutedDark
                    )
                }

                // Save
                IconButton(
                    onClick = { viewModel.saveProjectExplicitly() },
                    modifier = Modifier.size(36.dp).testTag("save_button")
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = NeonCyan, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Save, contentDescription = "Save", tint = NeonCyan)
                    }
                }

                // Export Button
                Button(
                    onClick = {
                        playerManager.pause()
                        onNavigateExport(projectId)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("export_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.IosShare, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Export", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // 2. VIDEO PREVIEW AREA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            if (activeProject != null) {
                VideoPlayerView(
                    playerManager = playerManager,
                    aspectRatio = activeProject.aspectRatio,
                    currentPositionMs = currentPositionMs,
                    totalDurationMs = totalDurationMs,
                    isPlaying = isPlaying,
                    activeTextOverlays = activeProject.textOverlays,
                    playbackError = playbackError,
                    onTogglePlayPause = { playerManager.togglePlayPause() },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                CircularProgressIndicator(color = NeonCyan)
            }
        }

        // 3. PLAYBACK CONTROLLER & TIME
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { playerManager.togglePlayPause() },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SlateCard)
                    .testTag("play_pause_button")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = NeonCyan,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Timeline Zoom Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = { timelineZoom = (timelineZoom - 15f).coerceAtLeast(30f) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = TextSecondaryDark)
                }
                Text("Zoom", color = TextMutedDark, fontSize = 11.sp)
                IconButton(
                    onClick = { timelineZoom = (timelineZoom + 15f).coerceAtMost(120f) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = TextSecondaryDark)
                }
            }
        }

        // 4. TIMELINE CANVAS
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            if (activeProject != null) {
                TimelineCanvas(
                    clips = activeProject.clips,
                    audioClips = activeProject.audioClips,
                    textOverlays = activeProject.textOverlays,
                    totalDurationMs = totalDurationMs,
                    currentPositionMs = currentPositionMs,
                    selectedClipId = selectedClipId,
                    onSelectClip = { viewModel.selectClip(it) },
                    onSeek = { playerManager.seekToTimelinePosition(it) },
                    onAddClipClick = {
                        videoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    },
                    pixelsPerSecond = timelineZoom,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // 5. TOOLBAR ACTIONS (CapCut-inspired bottom bar)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(SlateSurface)
                .border(width = 1.dp, color = SlateBorder)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Split
            EditorToolItem(
                icon = Icons.Default.ContentCut,
                label = stringResource(R.string.split),
                color = PlayheadColor,
                onClick = {
                    val splitSuccess = viewModel.splitClipAtPlayhead(currentPositionMs)
                    if (splitSuccess) {
                        Toast.makeText(context, "Clip split successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Cannot split here (move playhead inside clip)", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            // Trim
            EditorToolItem(
                icon = Icons.Default.ContentCut,
                label = stringResource(R.string.trim),
                color = TrimHandleYellow,
                enabled = selectedClip != null,
                onClick = { showTrimDialog = true }
            )

            // Add Media
            EditorToolItem(
                icon = Icons.Default.Add,
                label = "Add Media",
                color = NeonCyan,
                onClick = {
                    videoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                    )
                }
            )

            // AI Subtitles
            EditorToolItem(
                icon = Icons.Default.AutoAwesome,
                label = "AI Subtitles",
                color = GoldenAmber,
                onClick = { showAiSubtitleDialog = true }
            )

            // Text
            EditorToolItem(
                icon = Icons.Default.TextFields,
                label = stringResource(R.string.add_text),
                color = Color(0xFFA855F7),
                onClick = { showTextDialog = true }
            )

            // Music
            EditorToolItem(
                icon = Icons.Default.MusicNote,
                label = stringResource(R.string.add_audio),
                color = Color(0xFF10B981),
                onClick = { audioPickerLauncher.launch("audio/*") }
            )

            // Filters
            EditorToolItem(
                icon = Icons.Default.Filter,
                label = stringResource(R.string.filters),
                color = NeonCyan,
                enabled = selectedClip != null,
                onClick = { showFilterDialog = true }
            )

            // Speed
            EditorToolItem(
                icon = Icons.Default.Speed,
                label = "Speed (${selectedClip?.speed ?: 1.0}x)",
                color = Color.White,
                enabled = selectedClip != null,
                onClick = {
                    selectedClip?.let {
                        val nextSpeed = when (it.speed) {
                            1.0f -> 1.5f
                            1.5f -> 2.0f
                            2.0f -> 0.5f
                            else -> 1.0f
                        }
                        viewModel.setClipSpeed(it.id, nextSpeed)
                        Toast.makeText(context, "Speed: ${nextSpeed}x", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            // Rotate
            EditorToolItem(
                icon = Icons.Default.RotateRight,
                label = stringResource(R.string.rotate),
                color = Color.White,
                enabled = selectedClip != null,
                onClick = {
                    selectedClip?.let {
                        viewModel.rotateClip(it.id)
                        Toast.makeText(context, "Rotated +90°", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            // Duplicate
            EditorToolItem(
                icon = Icons.Default.ContentCopy,
                label = stringResource(R.string.duplicate),
                color = Color.White,
                enabled = selectedClip != null,
                onClick = {
                    selectedClip?.let {
                        viewModel.duplicateClip(it.id)
                        Toast.makeText(context, "Clip duplicated", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            // Delete
            EditorToolItem(
                icon = Icons.Default.Delete,
                label = stringResource(R.string.delete),
                color = PlayheadColor,
                enabled = selectedClip != null,
                onClick = {
                    selectedClip?.let {
                        viewModel.deleteClip(it.id)
                        Toast.makeText(context, "Clip deleted", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    // DIALOGS
    if (showTrimDialog && selectedClip != null) {
        ClipTrimDialog(
            clip = selectedClip,
            onDismiss = { showTrimDialog = false },
            onApply = { startMs, endMs ->
                viewModel.trimClip(selectedClip.id, startMs, endMs)
                showTrimDialog = false
            }
        )
    }

    if (showTextDialog) {
        TextOverlayDialog(
            onDismiss = { showTextDialog = false },
            onSave = { text, colorHex, fontSize ->
                viewModel.addTextOverlay(
                    TextOverlay(
                        text = text,
                        startTimeMs = currentPositionMs,
                        endTimeMs = (currentPositionMs + 3500L).coerceAtMost(totalDurationMs),
                        colorHex = colorHex,
                        fontSizeSp = fontSize
                    )
                )
                showTextDialog = false
                Toast.makeText(context, "Caption added at current playhead", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showFilterDialog && selectedClip != null) {
        ClipFilterDialog(
            currentFilter = selectedClip.filter,
            onDismiss = { showFilterDialog = false },
            onSelectFilter = { filter ->
                viewModel.setClipFilter(selectedClip.id, filter)
                showFilterDialog = false
            }
        )
    }

    if (showAiSubtitleDialog) {
        AiSubtitleDialog(
            onDismiss = { showAiSubtitleDialog = false },
            onGenerate = { lang ->
                showAiSubtitleDialog = false
                viewModel.generateAiSubtitles(lang)
                Toast.makeText(context, "AI is analyzing audio and generating subtitles...", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun EditorToolItem(
    icon: ImageVector,
    label: String,
    color: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (enabled) color else TextMutedDark,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = if (enabled) Color.White else TextMutedDark,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
