package com.taska.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme

private val LightColorScheme = lightColorScheme(
    primary = AppPrimary,
    onPrimary = AppOnPrimary,
    background = AppBackground,
    onBackground = AppOnBackground,
    surface = AppSurface,
    onSurface = AppOnSurface,
    secondary = SignalGreen,
    onSecondary = BrandNavy,
    surfaceVariant = SignalGreenPale,
    outline = AppBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = SignalGreen,
    onPrimary = BrandNavy,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    secondary = SignalGreen,
    onSecondary = BrandNavy,
    surfaceVariant = BrandNavyLight,
    outline = BrandNavyLight
)

@Composable
fun TaskaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
