package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.core.model.AspectRatio
import com.example.core.model.FilterType
import com.example.core.model.FrameRate
import com.example.core.model.TextOverlay
import com.example.core.model.VideoClip
import com.example.core.model.VideoResolution
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
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewProjectDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, ratio: AspectRatio, res: VideoResolution, fps: FrameRate) -> Unit
) {
    var title by remember { mutableStateOf("My Video ${System.currentTimeMillis() % 10000}") }
    var selectedRatio by remember { mutableStateOf(AspectRatio.RATIO_9_16) }
    var selectedRes by remember { mutableStateOf(VideoResolution.FHD_1080P) }
    var selectedFps by remember { mutableStateOf(FrameRate.FPS_30) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SlateSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
            modifier = Modifier.fillMaxWidth().testTag("new_project_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.project_setup_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMutedDark)
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.project_name)) },
                    modifier = Modifier.fillMaxWidth().testTag("project_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                // Aspect Ratio Selector
                Text(
                    text = stringResource(R.string.aspect_ratio),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondaryDark
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AspectRatio.entries.forEach { ratio ->
                        val isSelected = ratio == selectedRatio
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0x3300E5FF) else SlateCard)
                                .border(
                                    1.dp,
                                    if (isSelected) NeonCyan else SlateBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedRatio = ratio }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = ratio.label,
                                    color = if (isSelected) NeonCyan else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = ratio.subtitle,
                                    color = TextSecondaryDark,
                                    fontSize = 11.sp
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = NeonCyan)
                            }
                        }
                    }
                }

                // Resolution Selector
                Text(
                    text = stringResource(R.string.resolution),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondaryDark
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VideoResolution.entries.forEach { res ->
                        val isSelected = res == selectedRes
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0x3300E5FF) else SlateCard)
                                .border(1.dp, if (isSelected) NeonCyan else SlateBorder, RoundedCornerShape(8.dp))
                                .clickable { selectedRes = res }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = res.label.split(" ").first(),
                                color = if (isSelected) NeonCyan else Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Cancel", color = TextSecondaryDark)
                    }
                    Button(
                        onClick = { onCreate(title, selectedRatio, selectedRes, selectedFps) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                        modifier = Modifier.testTag("confirm_create_project")
                    ) {
                        Text(stringResource(R.string.create_project), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TextOverlayDialog(
    initialText: String = "",
    initialColor: String = "#FFFFFF",
    onDismiss: () -> Unit,
    onSave: (text: String, colorHex: String, fontSize: Float) -> Unit
) {
    var text by remember { mutableStateOf(initialText) }
    var selectedColor by remember { mutableStateOf(initialColor) }
    var fontSize by remember { mutableFloatStateOf(24f) }

    val colorPresets = listOf("#FFFFFF", "#00E5FF", "#FFD600", "#FF3366", "#A855F7", "#10B981")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SlateSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.text_overlay),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(stringResource(R.string.enter_text)) },
                    modifier = Modifier.fillMaxWidth().height(100.dp).testTag("text_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                // Font Size Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.font_size), color = TextSecondaryDark, fontSize = 13.sp)
                    Text("${fontSize.toInt()}sp", color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = fontSize,
                    onValueChange = { fontSize = it },
                    valueRange = 16f..42f,
                    colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                )

                // Color Presets
                Text(stringResource(R.string.text_color), color = TextSecondaryDark, fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    colorPresets.forEach { hex ->
                        val isSelected = hex == selectedColor
                        val color = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color.White else SlateBorder,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (hex == "#FFFFFF" || hex == "#FFD600") Color.Black else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.padding(end = 8.dp)) {
                        Text("Cancel", color = TextSecondaryDark)
                    }
                    Button(
                        onClick = { if (text.isNotBlank()) onSave(text, selectedColor, fontSize) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                        modifier = Modifier.testTag("save_text_button")
                    ) {
                        Text("Add to Video", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ClipTrimDialog(
    clip: VideoClip,
    onDismiss: () -> Unit,
    onApply: (trimStartMs: Long, trimEndMs: Long) -> Unit
) {
    var trimStart by remember { mutableLongStateOf(clip.trimStartMs) }
    var trimEnd by remember { mutableLongStateOf(clip.trimEndMs) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SlateSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Trim Clip: ${clip.name}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "New Duration: ${String.format(Locale.US, "%.1fs", (trimEnd - trimStart) / 1000f)}",
                    color = TrimHandleYellow,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // Start Trim Slider
                Text("Start Trim: ${String.format(Locale.US, "%.1fs", trimStart / 1000f)}", color = TextSecondaryDark, fontSize = 12.sp)
                Slider(
                    value = trimStart.toFloat(),
                    onValueChange = { newVal ->
                        trimStart = newVal.toLong().coerceAtMost(trimEnd - 500L)
                    },
                    valueRange = 0f..clip.durationMs.toFloat(),
                    colors = SliderDefaults.colors(thumbColor = TrimHandleYellow, activeTrackColor = TrimHandleYellow)
                )

                // End Trim Slider
                Text("End Trim: ${String.format(Locale.US, "%.1fs", trimEnd / 1000f)}", color = TextSecondaryDark, fontSize = 12.sp)
                Slider(
                    value = trimEnd.toFloat(),
                    onValueChange = { newVal ->
                        trimEnd = newVal.toLong().coerceAtLeast(trimStart + 500L)
                    },
                    valueRange = 0f..clip.durationMs.toFloat(),
                    colors = SliderDefaults.colors(thumbColor = TrimHandleYellow, activeTrackColor = TrimHandleYellow)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.padding(end = 8.dp)) {
                        Text("Cancel", color = TextSecondaryDark)
                    }
                    Button(
                        onClick = { onApply(trimStart, trimEnd) },
                        colors = ButtonDefaults.buttonColors(containerColor = TrimHandleYellow, contentColor = Color.Black)
                    ) {
                        Text("Apply Trim", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ClipFilterDialog(
    currentFilter: FilterType,
    onDismiss: () -> Unit,
    onSelectFilter: (FilterType) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SlateSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Cinematic Filters",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                FilterType.entries.forEach { filter ->
                    val isSelected = filter == currentFilter
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0x3300E5FF) else SlateCard)
                            .border(1.dp, if (isSelected) NeonCyan else SlateBorder, RoundedCornerShape(8.dp))
                            .clickable { onSelectFilter(filter) }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = filter.displayName,
                                color = if (isSelected) NeonCyan else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = filter.description,
                                color = TextSecondaryDark,
                                fontSize = 11.sp
                            )
                        }
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = NeonCyan)
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Done", color = TextSecondaryDark)
                    }
                }
            }
        }
    }
}

@Composable
fun AiSubtitleDialog(
    onDismiss: () -> Unit,
    onGenerate: (language: String) -> Unit
) {
    var selectedLang by remember { mutableStateOf("bn") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SlateSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GoldenAmber)
                    Text("Auto Subtitles (AI)", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "ProCut AI analyzes the timeline audio and automatically generates timed captions in Bengali or English.",
                    color = TextSecondaryDark,
                    fontSize = 13.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isBn = selectedLang == "bn"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isBn) Color(0x3300E5FF) else SlateCard)
                            .border(1.dp, if (isBn) NeonCyan else SlateBorder, RoundedCornerShape(8.dp))
                            .clickable { selectedLang = "bn" }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("বাংলা (Bengali)", color = if (isBn) NeonCyan else Color.White, fontWeight = FontWeight.Bold)
                    }

                    val isEn = selectedLang == "en"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isEn) Color(0x3300E5FF) else SlateCard)
                            .border(1.dp, if (isEn) NeonCyan else SlateBorder, RoundedCornerShape(8.dp))
                            .clickable { selectedLang = "en" }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("English", color = if (isEn) NeonCyan else Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.padding(end = 8.dp)) {
                        Text("Cancel", color = TextSecondaryDark)
                    }
                    Button(
                        onClick = { onGenerate(selectedLang) },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldenAmber, contentColor = Color.Black)
                    ) {
                        Text("Generate Subtitles", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
