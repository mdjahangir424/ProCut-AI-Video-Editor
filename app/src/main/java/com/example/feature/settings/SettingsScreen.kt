package com.example.feature.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.config.CreatorInfo
import com.example.core.firebase.AuthManager
import com.example.core.firebase.FirestoreSyncManager
import com.example.ui.theme.GoldenAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PlayheadColor
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateDarkBg
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextSecondaryDark
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    onNavigateAbout: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authManager = remember { AuthManager(context) }
    val syncManager = remember { FirestoreSyncManager(context) }

    val currentUser by authManager.currentUser.collectAsState()
    val isAuthLoading by authManager.isLoading.collectAsState()
    val authError by authManager.authError.collectAsState()
    val isSyncing by syncManager.isSyncing.collectAsState()
    val syncMessage by syncManager.syncMessage.collectAsState()

    var aiOfflineOnly by remember { mutableStateOf(false) }

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

        // Firebase Cloud Account & Backup Card
        SettingGroupCard(title = "Firebase Cloud & Backup") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (currentUser != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0x3310B981)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(24.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentUser?.displayName ?: "Google User",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = currentUser?.email ?: "",
                                color = TextSecondaryDark,
                                fontSize = 12.sp
                            )
                        }
                        OutlinedButton(
                            onClick = { authManager.signOut(scope) },
                            border = androidx.compose.foundation.BorderStroke(1.dp, PlayheadColor)
                        ) {
                            Text("Sign Out", color = PlayheadColor, fontSize = 11.sp)
                        }
                    }

                    Text(
                        text = "Firebase Firestore connected: projects are backed up automatically.",
                        color = Color(0xFF10B981),
                        fontSize = 11.sp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = GoldenAmber, modifier = Modifier.size(24.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sign in to Backup Projects",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Sync your timeline edits securely with Google Cloud Firebase",
                                color = TextSecondaryDark,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val activity = context as? Activity ?: return@Button
                            authManager.signInWithGoogle(activity, scope)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("google_sign_in_button"),
                        enabled = !isAuthLoading
                    ) {
                        if (isAuthLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text("Sign in with Google", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (authError != null) {
                        Text(authError ?: "", color = PlayheadColor, fontSize = 11.sp)
                    }
                }
            }
        }

        // Developer & Ownership Card
        SettingGroupCard(title = stringResource(R.string.developer_and_ownership)) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0x3300E5FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(24.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = CreatorInfo.DEVELOPER_NAME,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.creator_role),
                            color = GoldenAmber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Clickable phone dialer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateCard)
                        .clickable {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${CreatorInfo.CONTACT_PHONE}"))
                            runCatching { context.startActivity(intent) }
                                .onFailure { Toast.makeText(context, "Cannot open dialer", Toast.LENGTH_SHORT).show() }
                        }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                        Column {
                            Text(stringResource(R.string.contact_phone_label), color = TextMutedDark, fontSize = 10.sp)
                            Text(CreatorInfo.CONTACT_PHONE, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Text(stringResource(R.string.call_developer), color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Clickable email sender
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateCard)
                        .clickable {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${CreatorInfo.CONTACT_EMAIL}")).apply {
                                putExtra(Intent.EXTRA_SUBJECT, "ProCut AI Video Editor Feedback")
                            }
                            runCatching { context.startActivity(intent) }
                                .onFailure { Toast.makeText(context, "Cannot open email app", Toast.LENGTH_SHORT).show() }
                        }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                        Column {
                            Text(stringResource(R.string.contact_email_label), color = TextMutedDark, fontSize = 10.sp)
                            Text(CreatorInfo.CONTACT_EMAIL, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Text(stringResource(R.string.email_developer), color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Copyright notice
                Text(
                    text = CreatorInfo.COPYRIGHT_NOTICE,
                    color = TextMutedDark,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                // Button to Open full About Screen
                Button(
                    onClick = onNavigateAbout,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x3300E5FF), contentColor = NeonCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("open_about_screen_button")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.about_procut), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Language Setting
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

        // Theme Setting
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

        // AI Mode Setting
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

        // Storage & Cache
        SettingGroupCard(title = "Storage Management") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
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
