package com.example.myapplication.ui.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun SugarGuardTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF26A69A),
            secondary = Color(0xFFFF9800),
            tertiary = Color(0xFF42A5F5),
            background = Color(0xFFF9FAFB),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFF3F4F6),
            onPrimary = Color(0xFFFFFFFF),
            onSecondary = Color(0xFFFFFFFF),
            onTertiary = Color(0xFFFFFFFF),
            onBackground = Color(0xFF1F2937),
            onSurface = Color(0xFF1F2937),
            onSurfaceVariant = Color(0xFF6B7280),
            primaryContainer = Color(0xFFF0FDFA),
            onPrimaryContainer = Color(0xFF00695C),
            secondaryContainer = Color(0xFFFFE0B2),
            onSecondaryContainer = Color(0xFFE65100),
            error = Color(0xFFEF4444),
            onError = Color(0xFFFFFFFF),
            outline = Color(0xFF9CA3AF)
        ),
        content = content
    )
}
