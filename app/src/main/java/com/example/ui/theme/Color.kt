package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Sleek Interface Palette (M3 Alignments)
val SleekLightBackground = Color(0xFFFEF7FF)
val SleekLightText = Color(0xFF1D1B20)
val SleekSupportingText = Color(0xFF49454F)
val SleekPrimary = Color(0xFF6750A4)
val SleekPrimaryVariant = Color(0xFFE8DEF8)
val SleekOnPrimaryContainer = Color(0xFF1D192B)

val SleekBorderLight = Color(0xFFE6E1E5)
val SleekCardBorder = Color(0xFFCAC4D0)

// Overdue specific colors
val SleekOverdueBg = Color(0xFFFFF1F0)
val SleekOverdueBorder = Color(0xFFF9DEDC)
val SleekOverdueText = Color(0xFFB3261E)
val SleekOverdueActive = Color(0xFFF2B8B5)

// Accent Soft Blue (FAB / Special Items)
val SleekAccentBlue = Color(0xFFD1E1FF)
val SleekAccentBlueText = Color(0xFF001D35)

// Dark Theme counterparts for accessibility/completeness
val SleekDarkBackground = Color(0xFF141218)
val SleekDarkSurface = Color(0xFF1D1B20)
val SleekDarkOnSurface = Color(0xFFE6E1E5)
val SleekDarkPrimary = Color(0xFFD0BCFF)
val SleekDarkSecondary = Color(0xFFCCC2DC)
val SleekDarkTertiary = Color(0xFFADC1F9)
val SleekDarkPrimaryContainer = Color(0xFF4F378B)
val SleekDarkOnPrimaryContainer = Color(0xFFEADDFF)

fun String?.toColorSafely(fallback: Color = Color(0xFF64748B)): Color {
    if (this.isNullOrBlank()) return fallback
    return try {
        Color(android.graphics.Color.parseColor(this))
    } catch (e: Exception) {
        fallback
    }
}
