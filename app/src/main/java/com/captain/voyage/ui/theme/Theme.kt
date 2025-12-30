package com.captain.voyage.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Purple200,
    secondary = Teal200,
    tertiary = Purple700,
    background = VoyageBackgroundDark, 
    surface = VoyageBackgroundDark,
    onPrimary = Black,
    onSecondary = Black,
    onTertiary = White,
    onBackground = VoyageTextPrimary, 
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
)

@Composable
fun VoyageTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun voyageTextFieldColors(): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = VoyageTextPrimary,
        unfocusedTextColor = VoyageTextPrimary,
        focusedContainerColor = White,
        unfocusedContainerColor = White,
        focusedBorderColor = Color(0xFF5D4037),
        unfocusedBorderColor = Color(0xFF8D6E63),
        focusedLabelColor = Color(0xFF5D4037),
        unfocusedLabelColor = Color(0xFF8D6E63),
        cursorColor = Color(0xFF5D4037)
    )
}