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

private val DarkColorScheme = darkColorScheme(
    primary = CoralPrimaryDark,
    onPrimary = CoralOnPrimaryDark,
    primaryContainer = CoralContainerDark,
    onPrimaryContainer = CoralOnContainerDark,
    secondary = WarmSecondaryDark,
    onSecondary = WarmOnSecondaryDark,
    secondaryContainer = WarmContainerDark,
    onSecondaryContainer = WarmOnContainerDark,
    tertiary = SoftTertiaryDark,
    onTertiary = SoftOnTertiaryDark,
    tertiaryContainer = SoftContainerDark,
    onTertiaryContainer = SoftOnContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = CoralPrimaryLight,
    onPrimary = CoralOnPrimaryLight,
    primaryContainer = CoralContainerLight,
    onPrimaryContainer = CoralOnContainerLight,
    secondary = WarmSecondaryLight,
    onSecondary = WarmOnSecondaryLight,
    secondaryContainer = WarmContainerLight,
    onSecondaryContainer = WarmOnContainerLight,
    tertiary = SoftTertiaryLight,
    onTertiary = SoftOnTertiaryLight,
    tertiaryContainer = SoftContainerLight,
    onTertiaryContainer = SoftOnContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight
)

@Composable
fun AchdaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent community brand colors by default
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

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) = AchdaTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
