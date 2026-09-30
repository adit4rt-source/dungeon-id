package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

data class PixelColorPalette(
    val background: Color = PixelDarkSlate,
    val panelSurface: Color = PixelPanelSurface,
    val panelSurfaceLight: Color = PixelPanelSurfaceLight,
    val panelSurfaceDeep: Color = PixelPanelSurfaceDeep,
    val borderOuter: Color = PixelBorderOuter,
    val borderInner: Color = PixelBorderInner,
    val borderHighlight: Color = PixelBorderHighlight,
    val gold: Color = PixelGold,
    val goldDark: Color = PixelGoldDark,
    val healthCrimson: Color = PixelCrimson,
    val staminaEmerald: Color = PixelEmerald,
    val manaAzure: Color = PixelAzure,
    val magicPurple: Color = PixelAmethyst,
    val textPrimary: Color = PixelTextWhite,
    val textSecondary: Color = PixelTextLight,
    val textMuted: Color = PixelTextMuted,
    val parchment: Color = PixelParchment
)

data class PixelTypographyTokens(
    val titleLarge: TextStyle = TextStyle(
        fontFamily = PressStartFontFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 18.sp,
        color = PixelTextWhite
    ),
    val titleMedium: TextStyle = TextStyle(
        fontFamily = PressStartFontFamily,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 14.sp,
        color = PixelGold
    ),
    val titleSmall: TextStyle = TextStyle(
        fontFamily = PressStartFontFamily,
        fontSize = 8.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 12.sp,
        color = PixelTextWhite
    ),
    val button: TextStyle = TextStyle(
        fontFamily = PressStartFontFamily,
        fontSize = 8.sp,
        fontWeight = FontWeight.Bold,
        color = PixelTextWhite
    ),
    val badge: TextStyle = TextStyle(
        fontFamily = PressStartFontFamily,
        fontSize = 7.sp,
        fontWeight = FontWeight.Normal,
        color = PixelTextWhite
    ),
    val bodyLarge: TextStyle = TextStyle(
        fontFamily = Vt323FontFamily,
        fontSize = 18.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 22.sp,
        color = PixelTextWhite
    ),
    val bodyMedium: TextStyle = TextStyle(
        fontFamily = Vt323FontFamily,
        fontSize = 15.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 19.sp,
        color = PixelTextLight
    ),
    val bodySmall: TextStyle = TextStyle(
        fontFamily = Vt323FontFamily,
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp,
        color = PixelTextMuted
    )
)

data class PixelShapes(
    val square: Shape = RectangleShape
)

val LocalPixelColors = compositionLocalOf { PixelColorPalette() }
val LocalPixelTypography = compositionLocalOf { PixelTypographyTokens() }
val LocalPixelShapes = compositionLocalOf { PixelShapes() }

object PixelTheme {
    val colors: PixelColorPalette
        @Composable
        @ReadOnlyComposable
        get() = LocalPixelColors.current

    val typography: PixelTypographyTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalPixelTypography.current

    val shapes: PixelShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalPixelShapes.current
}
