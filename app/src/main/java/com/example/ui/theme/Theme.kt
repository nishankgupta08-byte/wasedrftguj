package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SleekDarkPrimary,
    secondary = SleekDarkSecondary,
    tertiary = SleekDarkTertiary,
    background = Color(0xFF000000), // Pure OLED Black
    surface = Color(0xFF101010),    // Dark Grey Surface Card
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    primaryContainer = SleekDarkPrimaryContainer,
    onPrimaryContainer = SleekDarkOnPrimaryContainer,
    surfaceVariant = Color(0xFF1E1E1E),
    onSurfaceVariant = Color(0xFFCCCCCC)
)

private val LightColorScheme = lightColorScheme(
    primary = SleekPrimary,
    secondary = SleekPrimaryVariant,
    tertiary = SleekAccentBlue,
    background = Color(0xFFFFFFFF), // Pure White
    surface = Color(0xFFF8F8F8),    // Clean Light Grey Surface Card
    onPrimary = Color.White,
    onSecondary = SleekOnPrimaryContainer,
    onBackground = SleekLightText,
    onSurface = SleekLightText,
    primaryContainer = SleekPrimaryVariant,
    onPrimaryContainer = SleekOnPrimaryContainer,
    surfaceVariant = SleekBorderLight,
    onSurfaceVariant = SleekSupportingText
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamicColor to enforce our gorgeous brand colors consistently
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
