package com.example.feature.ai

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.ai.AiManager
import com.example.core.ai.AiScriptResponse
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.GoldenAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateDarkBg
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextSecondaryDark
import kotlinx.coroutines.launch

@Composable
fun AiStudioScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Titles/Hooks, 1: Script, 2: Audio EQ

    var topicInput by remember { mutableStateOf("How to grow on Reels with ProCut AI") }
    var selectedLanguage by remember { mutableStateOf("bn") } // "bn" or "en"
    var isLoading by remember { mutableStateOf(false) }

    var generatedTitles by remember { mutableStateOf<List<String>>(emptyList()) }
    var generatedScript by remember { mutableStateOf<AiScriptResponse?>(null) }
    var audioPresetApplied by remember { mutableStateOf<String?>(null) }

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
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("ai_back_button")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GoldenAmber, modifier = Modifier.size(24.dp))
                Text(
                    text = stringResource(R.string.ai_studio_title),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Privacy & Offline/Online Status Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x3310B981)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                }
                Column {
                    Text(
                        text = "100% Privacy Focused & Offline Ready",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.privacy_notice),
                        color = TextSecondaryDark,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Language Selector (Bengali / English)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val isBn = selectedLanguage == "bn"
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isBn) Color(0x3300E5FF) else SlateSurface)
                    .border(1.dp, if (isBn) NeonCyan else SlateBorder, RoundedCornerShape(8.dp))
                    .clickable { selectedLanguage = "bn" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("বাংলা (Bengali)", color = if (isBn) NeonCyan else Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            val isEn = selectedLanguage == "en"
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isEn) Color(0x3300E5FF) else SlateSurface)
                    .border(1.dp, if (isEn) NeonCyan else SlateBorder, RoundedCornerShape(8.dp))
                    .clickable { selectedLanguage = "en" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("English", color = if (isEn) NeonCyan else Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SlateSurface,
            contentColor = NeonCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = NeonCyan
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Viral Titles", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Short Script", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Voice Clean", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
        }

        // Tab Content
        when (selectedTab) {
            0 -> {
                // Viral Titles Generator
                OutlinedTextField(
                    value = topicInput,
                    onValueChange = { topicInput = it },
                    label = { Text("Video Topic / Keyword") },
                    modifier = Modifier.fillMaxWidth().testTag("ai_topic_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            generatedTitles = AiManager.generateViralTitles(topicInput, selectedLanguage)
                            isLoading = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldenAmber, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("generate_titles_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("Generate High-CTR Titles", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (generatedTitles.isNotEmpty()) {
                    Text("Suggested Titles (Tap to copy):", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        generatedTitles.forEach { title ->
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("ProCut Title", title))
                                        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = title, color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = NeonCyan, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Short-form Script Generator
                OutlinedTextField(
                    value = topicInput,
                    onValueChange = { topicInput = it },
                    label = { Text("Script Topic / Idea") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricPurple,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            generatedScript = AiManager.generateScript(topicInput, selectedLanguage)
                            isLoading = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple, contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("Generate 30s Viral Video Script", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                generatedScript?.let { script ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SlateSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Title: ${script.title}", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("🎣 3s Hook: ${script.hook}", color = GoldenAmber, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("🎬 Body: ${script.body}", color = Color.White, fontSize = 13.sp)
                            Text("📢 Call To Action: ${script.callToAction}", color = Color(0xFF10B981), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("🏷️ Tags: ${script.hashtags.joinToString(" ")}", color = TextSecondaryDark, fontSize = 12.sp)
                        }
                    }
                }
            }

            2 -> {
                // Voice Enhancement / Denoise
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = NeonCyan)
                            Text("Audio Cleanup & Vocal Enhancement", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "Apply AI frequency curve filters to isolate human speech and remove low-frequency room noise and hiss.",
                            color = TextSecondaryDark,
                            fontSize = 12.sp
                        )

                        listOf(
                            "Vocal Clarity Boost (+4dB Treble)",
                            "Studio Podcast Warmth",
                            "Background Hiss & Hum Reduction"
                        ).forEach { preset ->
                            val isApplied = audioPresetApplied == preset
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isApplied) Color(0x3300E5FF) else SlateCard)
                                    .border(1.dp, if (isApplied) NeonCyan else SlateBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        audioPresetApplied = preset
                                        Toast.makeText(context, "$preset preset enabled for editor", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(preset, color = if (isApplied) NeonCyan else Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text(if (isApplied) "Active" else "Select", color = if (isApplied) NeonCyan else TextMutedDark, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
