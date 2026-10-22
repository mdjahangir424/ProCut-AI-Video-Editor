package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF004D5A),
    onPrimaryContainer = NeonCyan,
    secondary = ElectricPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4A1E7A),
    onSecondaryContainer = Color(0xFFE9D5FF),
    tertiary = GoldenAmber,
    onTertiary = Color.Black,
    background = SlateDarkBg,
    onBackground = TextPrimaryDark,
    surface = SlateSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = SlateCard,
    onSurfaceVariant = TextSecondaryDark,
    outline = SlateBorder,
    error = PlayheadColor,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF007A8A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD0F8FF),
    onPrimaryContainer = Color(0xFF00363E),
    secondary = ElectricPurple,
    onSecondary = Color.White,
    tertiary = GoldenAmber,
    background = SlateLightBg,
    onBackground = TextPrimaryLight,
    surface = SlateLightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = SlateLightCard,
    onSurfaceVariant = TextSecondaryLight,
    outline = SlateLightBorder,
    error = Color(0xFFDC2626)
)

@Composable
fun ProCutTheme(
    darkTheme: Boolean = true, // Video editors default to dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
