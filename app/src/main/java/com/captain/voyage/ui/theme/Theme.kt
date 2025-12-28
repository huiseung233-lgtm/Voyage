package com.captain.voyage.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Purple200,
    secondary = Teal200,
    tertiary = Purple700,
    background = VoyageBackgroundDark, // Darker paper in dark mode? Or default dark. Keeping consistent with XML for now.
    surface = VoyageBackgroundDark,
    onPrimary = Black,
    onSecondary = Black,
    onTertiary = White,
    onBackground = VoyageTextPrimary, // Might need adjustment for dark mode readability
    onSurface = VoyageTextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = Purple500,
    secondary = Teal200,
    tertiary = Purple700,
    background = VoyageBackgroundPaper,
    surface = VoyageBackgroundPaper,
    onPrimary = White,
    onSecondary = Black,
    onTertiary = White,
    onBackground = VoyageTextSecondary,
    onSurface = VoyageTextPrimary

    /* Other default colors to override
    error = Color(0xFFBA1A1A),
    */
)

@Composable
fun VoyageTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+. Set default to false for pixel art style.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    // SideEffect removed to be handled in MainActivity
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
