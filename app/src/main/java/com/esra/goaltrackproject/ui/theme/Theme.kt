package com.esra.goaltrackproject.ui.theme

import android.app.Activity
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
    primary = YellowGold,
    secondary = BlueLight,
    tertiary = YellowBright,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = OnDarkBackground,
    onSurface = OnDarkBackground
)

private val LightColorScheme = lightColorScheme(
    primary = BlueDark,
    secondary = YellowGold,
    tertiary = BlueLight,
    background = BackgroundNeutral,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun GoalTrackProjectTheme(
    themeMode: Int = 0,
    dynamicColor: Boolean = false, // Disabled to use custom Royal Navigation theme
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }

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