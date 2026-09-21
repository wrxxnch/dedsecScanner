package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DedsecColorScheme = darkColorScheme(
    primary = DedsecNeonGreen,
    onPrimary = DedsecBlack,
    primaryContainer = DedsecSurfaceVariant,
    onPrimaryContainer = DedsecNeonGreen,
    secondary = DedsecNeonCyan,
    onSecondary = DedsecBlack,
    secondaryContainer = DedsecSurfaceVariant,
    onSecondaryContainer = DedsecNeonCyan,
    tertiary = DedsecGlitchRed,
    onTertiary = DedsecBlack,
    background = DedsecBlack,
    onBackground = DedsecTextPrimary,
    surface = DedsecDarkSurface,
    onSurface = DedsecTextPrimary,
    surfaceVariant = DedsecSurfaceCard,
    onSurfaceVariant = DedsecTextSecondary,
    outline = DedsecBorder,
    error = DedsecGlitchRed,
    onError = DedsecBlack
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // DedSec is inherently a dark terminal aesthetic
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DedsecColorScheme,
        typography = Typography,
        content = content
    )
}
