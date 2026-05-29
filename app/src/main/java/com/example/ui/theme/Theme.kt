package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SleekColorScheme = lightColorScheme(
    primary = SleekPrimary,
    onPrimary = SolidWhite,
    primaryContainer = SleekPurpleLight,
    onPrimaryContainer = SleekAccent,
    secondary = SleekSecondary,
    onSecondary = SolidWhite,
    secondaryContainer = SleekLavender,
    onSecondaryContainer = SleekTextDark,
    background = SleekBackground,
    onBackground = SleekTextDark,
    surface = SleekSurface,
    onSurface = SleekTextDark,
    surfaceVariant = SleekSurfaceVariant,
    onSurfaceVariant = SleekTextMuted,
    outline = MutedSlate,
    error = SleekCoralRed,
    onError = SolidWhite
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SleekColorScheme,
        typography = Typography,
        content = content
    )
}
