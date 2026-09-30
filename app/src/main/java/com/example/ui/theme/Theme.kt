package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val PixelDarkColorScheme = darkColorScheme(
    primary = PixelGold,
    onPrimary = Color.Black,
    primaryContainer = PixelBorderInner,
    onPrimaryContainer = PixelGoldLight,
    secondary = PixelAzure,
    onSecondary = Color.Black,
    secondaryContainer = PixelDeepOcean,
    onSecondaryContainer = PixelAzure,
    tertiary = PixelEmerald,
    onTertiary = Color.Black,
    tertiaryContainer = PixelEmeraldDark,
    onTertiaryContainer = Color(0xFFA5D6A7),
    background = PixelDarkSlate,
    onBackground = PixelTextLight,
    surface = PixelPanelSurface,
    onSurface = PixelTextWhite,
    surfaceVariant = PixelPanelSurfaceLight,
    onSurfaceVariant = PixelTextMuted,
    error = PixelCrimson,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val pixelColors = PixelColorPalette()
    val pixelTypography = PixelTypographyTokens()
    val pixelShapes = PixelShapes()

    CompositionLocalProvider(
        LocalPixelColors provides pixelColors,
        LocalPixelTypography provides pixelTypography,
        LocalPixelShapes provides pixelShapes
    ) {
        MaterialTheme(
            colorScheme = PixelDarkColorScheme,
            typography = Typography,
            content = content
        )
    }
}
