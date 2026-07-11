package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PurplePrimaryLight,
    onPrimary = Color.White,
    primaryContainer = GlassyPurpleDark,
    onPrimaryContainer = PurplePrimaryLight,
    secondary = PurpleSecondaryLight,
    onSecondary = Color.White,
    tertiary = PurpleTertiaryLight,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceTint = Color.Transparent
)

private val LightColorScheme = lightColorScheme(
    primary = PurplePrimaryLight,
    onPrimary = Color.White,
    primaryContainer = GlassyPurpleLight,
    onPrimaryContainer = PurplePrimaryLight,
    secondary = PurpleSecondaryLight,
    onSecondary = Color.White,
    tertiary = PurpleTertiaryLight,
    background = LightBackground,
    surface = LightSurface,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    surfaceVariant = Color(0xFFF3EAF8),
    onSurfaceVariant = Color(0xFF7B1FA2),
    surfaceTint = Color.Transparent
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to prioritize our vibrant branding
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
