package com.example.feature.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.R
import com.example.core.media.TransformerExportManager
import com.example.core.model.EditingProject
import com.example.core.model.ExportSettings
import com.example.core.model.ExportState
import com.example.core.model.FrameRate
import com.example.core.model.VideoResolution
import com.example.data.repository.ProjectRepository
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PlayheadColor
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateDarkBg
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextSecondaryDark
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

@Composable
fun ExportScreen(
    projectId: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { ProjectRepository(context) }
    val exportManager = remember { TransformerExportManager(context) }

    var project by remember { mutableStateOf<EditingProject?>(null) }
    var selectedResolution by remember { mutableStateOf(VideoResolution.FHD_1080P) }
    var selectedFrameRate by remember { mutableStateOf(FrameRate.FPS_30) }
    var selectedQuality by remember { mutableStateOf("Standard") }

    val exportState by exportManager.exportState.collectAsState()

    LaunchedEffect(projectId) {
        project = repository.getProjectById(projectId)
    }

    BackHandler {
        if (exportState is ExportState.InProgress) {
            exportManager.cancelExport()
        }
        onNavigateBack()
    }

    DisposableEffect(Unit) {
        onDispose {
            if (exportState is ExportState.InProgress) {
                exportManager.cancelExport()
            }
        }
    }

    // Estimated Size calculation
    val durationSec = ((project?.totalDurationMs ?: 0L) / 1000.0).coerceAtLeast(1.0)
    val bitrateBps = selectedResolution.bitrate * when (selectedQuality) {
        "High" -> 1.3
        "Low" -> 0.7
        else -> 1.0
    }
    val estimatedBytes = (durationSec * bitrateBps / 8).toLong()
    val estimatedMb = String.format(Locale.US, "%.1f MB", estimatedBytes / (1024.0 * 1024.0))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("export_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    text = stringResource(R.string.export_video),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Project Summary Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x3300E5FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.VideoFile, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(28.dp))
                }
                Column {
                    Text(
                        text = project?.title ?: "Loading...",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Aspect: ${project?.aspectRatio?.label} • Clips: ${project?.clips?.size ?: 0}",
                        color = TextSecondaryDark,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Duration: ${String.format(Locale.US, "%.1fs", durationSec)}",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Export Settings Panel (only show if not completed/in progress)
        if (exportState is ExportState.Idle || exportState is ExportState.Cancelled || exportState is ExportState.Failed) {
            // Resolution Selection
            Text(stringResource(R.string.resolution), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(VideoResolution.HD_720P, VideoResolution.FHD_1080P).forEach { res ->
                    val isSelected = res == selectedResolution
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0x3300E5FF) else SlateSurface)
                            .border(1.dp, if (isSelected) NeonCyan else SlateBorder, RoundedCornerShape(8.dp))
                            .clickable { selectedResolution = res }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = res.label,
                            color = if (isSelected) NeonCyan else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Frame Rate Selection
            Text(stringResource(R.string.frame_rate), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(FrameRate.FPS_30, FrameRate.FPS_60).forEach { fps ->
                    val isSelected = fps == selectedFrameRate
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0x3300E5FF) else SlateSurface)
                            .border(1.dp, if (isSelected) NeonCyan else SlateBorder, RoundedCornerShape(8.dp))
                            .clickable { selectedFrameRate = fps }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = fps.label,
                            color = if (isSelected) NeonCyan else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Bitrate Quality
            Text("Encoding Quality", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Standard", "High").forEach { q ->
                    val isSelected = q == selectedQuality
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0x3300E5FF) else SlateSurface)
                            .border(1.dp, if (isSelected) NeonCyan else SlateBorder, RoundedCornerShape(8.dp))
                            .clickable { selectedQuality = q }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = q,
                            color = if (isSelected) NeonCyan else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Estimated file size
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.estimated_size), color = TextSecondaryDark, fontSize = 13.sp)
                Text(estimatedMb, color = NeonCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Start Export Button
            Button(
                onClick = {
                    val p = project ?: return@Button
                    scope.launch {
                        val settings = ExportSettings(
                            resolution = selectedResolution,
                            frameRate = selectedFrameRate,
                            quality = selectedQuality
                        )
                        exportManager.startExport(p, settings)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("start_export_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Start Export (MP4)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Export Progress Section
        when (val state = exportState) {
            is ExportState.Preparing -> {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = NeonCyan)
                        Text("Preparing Media3 Transformer pipeline...", color = Color.White, fontSize = 14.sp)
                    }
                }
            }

            is ExportState.InProgress -> {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                    modifier = Modifier.fillMaxWidth().testTag("export_in_progress_card")
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.exporting),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "${state.progressPercentage}%",
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }

                        LinearProgressIndicator(
                            progress = { state.progressPercentage / 100f },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = NeonCyan,
                            trackColor = SlateBorder
                        )

                        Text(
                            text = "Encoding H.264 video track and AAC audio...",
                            color = TextSecondaryDark,
                            fontSize = 11.sp
                        )

                        OutlinedButton(
                            onClick = { exportManager.cancelExport() },
                            border = androidx.compose.foundation.BorderStroke(1.dp, PlayheadColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.export_cancel), color = PlayheadColor)
                        }
                    }
                }
            }

            is ExportState.Completed -> {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981)),
                    modifier = Modifier.fillMaxWidth().testTag("export_success_card")
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = stringResource(R.string.export_success),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Saved: ${File(state.filePath).name}\nSize: ${state.fileSizeFormatted}",
                            color = TextSecondaryDark,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Play video
                            Button(
                                onClick = {
                                    val file = File(state.filePath)
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    )
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(uri, "video/mp4")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    runCatching { context.startActivity(intent) }
                                        .onFailure { Toast.makeText(context, "No video player installed", Toast.LENGTH_SHORT).show() }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Text(stringResource(R.string.play_video), fontWeight = FontWeight.Bold)
                                }
                            }

                            // Share video
                            Button(
                                onClick = {
                                    val file = File(state.filePath)
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    )
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "video/mp4"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share Video"))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color.White),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Text(stringResource(R.string.share_video), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            is ExportState.Failed -> {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PlayheadColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = PlayheadColor)
                            Text(stringResource(R.string.export_failed), color = PlayheadColor, fontWeight = FontWeight.Bold)
                        }
                        Text(state.errorMessage, color = Color.White, fontSize = 12.sp)
                        OutlinedButton(
                            onClick = { exportManager.resetState() },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Try Again", color = NeonCyan)
                        }
                    }
                }
            }

            is ExportState.Cancelled -> {
                Text("Export was cancelled.", color = TextMutedDark, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
            }

            else -> {}
        }
    }
}
