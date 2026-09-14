package com.nyxaiglow.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NyxaiColors = darkColorScheme(
    primary = Color(0xFFE7B46A),
    onPrimary = Color(0xFF2D1900),
    secondary = Color(0xFF9ED1C4),
    onSecondary = Color(0xFF06201A),
    background = Color(0xFF0D1110),
    surface = Color(0xFF151C19),
    surfaceVariant = Color(0xFF26332E),
    onSurface = Color(0xFFF1F4EF),
    onSurfaceVariant = Color(0xFFB8C3BB)
)

@Composable
fun NyxaiGlowTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NyxaiColors,
        content = content
    )
}
