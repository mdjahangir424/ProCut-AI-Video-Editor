package com.example.feature.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VideoSettings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateDarkBg
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextSecondaryDark
import java.io.File

@Composable
fun SettingsScreen(
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var aiOfflineOnly by remember { mutableStateOf(true) }
    var defaultQuality by remember { mutableStateOf("1080p FHD") }

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
            IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("settings_back_button")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = stringResource(R.string.settings),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 1. Language Setting
        SettingGroupCard(title = stringResource(R.string.language)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val nextLang = if (currentLanguage == "bn") "en" else "bn"
                        onLanguageChange(nextLang)
                    }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = NeonCyan)
                    Column {
                        Text("App Language", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(if (currentLanguage == "bn") "বাংলা (Bengali)" else "English", color = TextSecondaryDark, fontSize = 12.sp)
                    }
                }
                Text("Tap to switch", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // 2. Theme Setting
        SettingGroupCard(title = stringResource(R.string.theme_mode)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.DarkMode, contentDescription = null, tint = NeonCyan)
                    Column {
                        Text(stringResource(R.string.dark_theme), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("Cinematic high-contrast editing interface", color = TextSecondaryDark, fontSize = 12.sp)
                    }
                }
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = onThemeToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = NeonCyan
                    )
                )
            }
        }

        // 3. AI Mode Setting
        SettingGroupCard(title = "AI Configuration") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = NeonCyan)
                    Column {
                        Text("Offline AI Priority", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = if (aiOfflineOnly) "All speech analysis runs 100% locally on device" else "Online Gemini AI enabled for smart script generation",
                            color = TextSecondaryDark,
                            fontSize = 12.sp
                        )
                    }
                }
                Switch(
                    checked = aiOfflineOnly,
                    onCheckedChange = { aiOfflineOnly = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = NeonCyan)
                )
            }
        }

        // 4. Storage & Cache
        SettingGroupCard(title = "Storage Management") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // Clear cache dir
                        context.cacheDir.deleteRecursively()
                        Toast.makeText(context, context.getString(R.string.cache_cleared), Toast.LENGTH_SHORT).show()
                    }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.CleaningServices, contentDescription = null, tint = NeonCyan)
                    Column {
                        Text(stringResource(R.string.clear_cache), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("Delete temporary video frames and preview cache", color = TextSecondaryDark, fontSize = 12.sp)
                    }
                }
                Text("Clean", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // 5. About & Privacy
        SettingGroupCard(title = stringResource(R.string.about_procut)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = TextMutedDark, modifier = Modifier.size(18.dp))
                    Text("ProCut AI Video Editor v1.0.0 (Native Android)", color = TextSecondaryDark, fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = TextMutedDark, modifier = Modifier.size(18.dp))
                    Text("Engineered with AndroidX Media3 (ExoPlayer & Transformer)", color = TextSecondaryDark, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun SettingGroupCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                color = NeonCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            content()
        }
    }
}
