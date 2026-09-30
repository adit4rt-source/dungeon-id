package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * PixelSpriteRenderer safely draws 16x16 retro pixel art grids using Compose Canvas.
 */
@Composable
fun PixelSprite(
    spriteKey: String,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    animated: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sprite_anim")
    val animOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -4f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )
    val bounceOffset = if (animated) animOffset else 0f

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer { translationY = bounceOffset }
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val canvasWidth = this.size.width
            val canvasHeight = this.size.height
            if (canvasWidth <= 0f || canvasHeight <= 0f) return@Canvas

            val grid = PixelSpriteDefinitions.getGrid(spriteKey)
            val rows = grid.size
            if (rows <= 0) return@Canvas

            val pixelHeight = canvasHeight / rows.toFloat()

            for (r in 0 until rows) {
                val row = grid.getOrNull(r) ?: continue
                val cols = row.size
                if (cols <= 0) continue

                val pixelWidth = canvasWidth / cols.toFloat()

                for (c in 0 until cols) {
                    val colorHex = row.getOrNull(c) ?: continue
                    if (colorHex != 0L) {
                        drawRect(
                            color = Color(colorHex),
                            topLeft = Offset(c * pixelWidth, r * pixelHeight),
                            size = Size(pixelWidth + 0.5f, pixelHeight + 0.5f)
                        )
                    }
                }
            }
        }
    }
}

object PixelSpriteDefinitions {

    private const val T = 0x00000000L
    private const val K = 0xFF181425L // Dark Outline
    private const val W = 0xFFFFFFFFL // White
    private const val R = 0xFFE53935L // Red
    private const val Y = 0xFFFFD54FL // Gold/Yellow
    private const val G = 0xFF43A047L // Emerald Green
    private const val C = 0xFF00E5FFL // Cyan Glow

    private fun row16(vararg values: Long): LongArray {
        val result = LongArray(16) { T }
        val count = minOf(values.size, 16)
        for (i in 0 until count) {
            result[i] = values[i]
        }
        return result
    }

    // 1. FROST WOLF (16x16)
    private val WOLF_16 = arrayOf(
        row16(T, T, K, K, T, T, T, T, T, T, T, T, K, K, T, T),
        row16(T, K, 0xFF81D4FA, K, T, T, T, T, T, T, T, K, 0xFF81D4FA, K, T, T),
        row16(K, 0xFFB3E5FC, 0xFF81D4FA, K, T, T, T, T, T, T, K, 0xFF81D4FA, 0xFFB3E5FC, K, T, T),
        row16(K, 0xFFECEFF1, 0xFFB3E5FC, K, K, K, K, K, K, K, K, 0xFFB3E5FC, 0xFFECEFF1, K, T, T),
        row16(K, 0xFFECEFF1, 0xFFECEFF1, 0xFFECEFF1, 0xFFECEFF1, 0xFFECEFF1, 0xFFECEFF1, 0xFFECEFF1, 0xFFECEFF1, 0xFFECEFF1, 0xFFECEFF1, 0xFFECEFF1, 0xFFECEFF1, K, T, T),
        row16(K, 0xFFECEFF1, C, C, 0xFFECEFF1, 0xFFECEFF1, 0xFFECEFF1, 0xFFECEFF1, 0xFFECEFF1, C, C, 0xFFECEFF1, 0xFFECEFF1, K, T, T),
        row16(K, 0xFFECEFF1, K, K, 0xFFECEFF1, 0xFFECEFF1, K, K, 0xFFECEFF1, K, K, 0xFFECEFF1, 0xFFECEFF1, K, T, T),
        row16(K, 0xFFECEFF1, 0xFFECEFF1, 0xFFECEFF1, 0xFFECEFF1, K, 0xFF263238, 0xFF263238, K, 0xFFECEFF1, 0xFFECEFF1, 0xFFECEFF1, 0xFFECEFF1, K, T, T),
        row16(T, K, 0xFFECEFF1, 0xFFECEFF1, 0xFFECEFF1, 0xFFECEFF1, K, K, 0xFFECEFF1, 0xFFECEFF1, 0xFFECEFF1, 0xFFECEFF1, K, T, T, T),
        row16(T, T, K, 0xFFE53935, 0xFFE53935, 0xFFE53935, Y, 0xFFE53935, 0xFFE53935, 0xFFE53935, K, T, T, T, T, T),
        row16(T, K, 0xFFECEFF1, 0xFFB3E5FC, 0xFFECEFF1, 0xFFECEFF1, 0xFFECEFF1, 0xFFECEFF1, 0xFFB3E5FC, 0xFFECEFF1, K, T, T, T, T, T),
        row16(T, K, 0xFFECEFF1, 0xFFECEFF1, 0xFFECEFF1, 0xFFECEFF1, 0xFFECEFF1, 0xFFECEFF1, 0xFFECEFF1, 0xFFECEFF1, K, T, T, T, T, T),
        row16(T, K, 0xFFECEFF1, 0xFFB3E5FC, 0xFFECEFF1, 0xFFECEFF1, 0xFFECEFF1, 0xFFECEFF1, 0xFFB3E5FC, 0xFFECEFF1, K, T, T, T, T, T),
        row16(T, K, 0xFFB3E5FC, K, 0xFFECEFF1, K, K, 0xFFECEFF1, K, 0xFFB3E5FC, K, T, T, T, T, T),
        row16(T, K, K, T, K, K, T, K, K, T, K, K, T, T, T, T),
        row16(T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T)
    )

    // 2. FIRE DRAGON / BABY DRAKE (16x16)
    private val DRAGON_16 = arrayOf(
        row16(T, T, Y, K, T, T, T, T, T, T, K, Y, T, T, T, T),
        row16(T, K, Y, Y, K, T, T, T, T, K, Y, Y, K, T, T, T),
        row16(T, K, 0xFFD32F2F, Y, K, K, K, K, K, K, Y, 0xFFD32F2F, K, T, T, T),
        row16(K, 0xFFD32F2F, 0xFFE53935, 0xFFE53935, 0xFFE53935, 0xFFE53935, 0xFFE53935, 0xFFE53935, 0xFFE53935, 0xFFE53935, 0xFFD32F2F, K, T, T, T, T),
        row16(K, 0xFFE53935, Y, Y, 0xFFE53935, 0xFFE53935, 0xFFE53935, 0xFFE53935, Y, Y, 0xFFE53935, K, T, T, T, T),
        row16(K, 0xFFE53935, K, K, 0xFFE53935, 0xFFE53935, 0xFFE53935, 0xFFE53935, K, K, 0xFFE53935, K, T, T, T, T),
        row16(K, 0xFFE53935, 0xFFE53935, 0xFFE53935, 0xFFE53935, 0xFFD32F2F, 0xFFD32F2F, 0xFFE53935, 0xFFE53935, 0xFFE53935, 0xFFE53935, K, T, T, T, T),
        row16(T, K, 0xFFD32F2F, 0xFFE53935, 0xFFE53935, 0xFFE53935, 0xFFE53935, 0xFFE53935, 0xFFE53935, 0xFFD32F2F, K, T, T, T, T, T),
        row16(K, 0xFFFF7043, K, 0xFFD32F2F, 0xFFFFB300, 0xFFFFB300, 0xFFFFB300, 0xFFFFB300, 0xFFD32F2F, K, 0xFFFF7043, K, T, T, T, T),
        row16(K, 0xFFFF7043, 0xFFFF7043, K, 0xFFFFB300, 0xFFFFE082, 0xFFFFE082, 0xFFFFB300, K, 0xFFFF7043, 0xFFFF7043, K, T, T, T, T),
        row16(T, K, 0xFFFF7043, K, 0xFFFFB300, 0xFFFFE082, 0xFFFFE082, 0xFFFFB300, K, 0xFFFF7043, K, T, T, T, T, T),
        row16(T, T, K, 0xFFD32F2F, 0xFFE53935, 0xFFFFB300, 0xFFFFB300, 0xFFE53935, 0xFFD32F2F, K, T, T, T, T, T, T),
        row16(T, T, K, 0xFFD32F2F, K, K, K, K, 0xFFD32F2F, K, 0xFFFF7043, K, T, T, T, T),
        row16(T, T, K, K, T, T, T, T, K, 0xFFFF7043, Y, K, T, T, T, T),
        row16(T, T, T, T, T, T, T, T, T, K, K, T, T, T, T, T),
        row16(T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T)
    )

    // 3. CRYSTAL TURTLE (16x16)
    private val TURTLE_16 = arrayOf(
        row16(T, T, T, T, T, K, K, K, K, T, T, T, T, T, T, T),
        row16(T, T, T, T, K, 0xFF43A047, 0xFF81C784, 0xFF81C784, 0xFF43A047, K, T, T, T, T, T, T),
        row16(T, T, T, T, K, 0xFF81C784, K, K, 0xFF81C784, K, T, T, T, T, T, T),
        row16(T, T, T, K, K, 0xFF43A047, 0xFF43A047, 0xFF43A047, 0xFF43A047, K, K, T, T, T, T, T),
        row16(T, T, K, 0xFF00695C, 0xFF00897B, 0xFF00897B, 0xFF00897B, 0xFF00897B, 0xFF00897B, 0xFF00695C, K, T, T, T, T, T),
        row16(T, K, 0xFF00897B, 0xFF00BFA5, 0xFF1DE9B6, 0xFF00BFA5, 0xFF00BFA5, 0xFF1DE9B6, 0xFF00BFA5, 0xFF00897B, K, T, T, T, T, T),
        row16(K, 0xFF00897B, 0xFF00BFA5, 0xFF00695C, 0xFF00897B, 0xFF00BFA5, 0xFF00BFA5, 0xFF00897B, 0xFF00695C, 0xFF00BFA5, 0xFF00897B, K, T, T, T, T),
        row16(K, 0xFF00897B, 0xFF1DE9B6, 0xFF00897B, 0xFF00BFA5, 0xFF1DE9B6, 0xFF1DE9B6, 0xFF00BFA5, 0xFF00897B, 0xFF1DE9B6, 0xFF00897B, K, T, T, T, T),
        row16(K, 0xFF00897B, 0xFF00BFA5, 0xFF00695C, 0xFF00897B, 0xFF00BFA5, 0xFF00BFA5, 0xFF00897B, 0xFF00695C, 0xFF00BFA5, 0xFF00897B, K, T, T, T, T),
        row16(T, K, 0xFF00897B, 0xFF00BFA5, 0xFF1DE9B6, 0xFF00BFA5, 0xFF00BFA5, 0xFF1DE9B6, 0xFF00BFA5, 0xFF00897B, K, T, T, T, T, T),
        row16(T, T, K, 0xFF00695C, 0xFF00897B, 0xFF00897B, 0xFF00897B, 0xFF00897B, 0xFF00897B, 0xFF00695C, K, T, T, T, T, T),
        row16(T, K, 0xFF43A047, K, K, K, K, K, K, K, K, 0xFF43A047, K, T, T, T),
        row16(T, K, 0xFF81C784, K, T, T, T, T, T, T, K, 0xFF81C784, K, T, T, T),
        row16(T, T, K, K, T, T, T, T, T, T, T, K, K, T, T, T),
        row16(T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T),
        row16(T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T)
    )

    // 4. PHOENIX BIRD (16x16)
    private val BIRD_16 = arrayOf(
        row16(T, T, T, T, T, T, Y, Y, Y, T, T, T, T, T, T, T),
        row16(T, T, T, T, T, K, Y, Y, Y, K, T, T, T, T, T, T),
        row16(T, T, T, T, K, 0xFFFFB300, 0xFFFFD54F, 0xFFFFD54F, 0xFFFFB300, K, T, T, T, T, T, T),
        row16(T, T, T, K, 0xFFFFB300, K, 0xFFFFD54F, 0xFFFFD54F, K, 0xFFFFB300, K, T, T, T, T, T),
        row16(T, T, T, K, 0xFFFFD54F, 0xFFFFD54F, 0xFFFF7043, 0xFFFF7043, 0xFFFFD54F, 0xFFFFD54F, K, T, T, T, T, T),
        row16(T, T, T, T, K, 0xFFFFB300, 0xFFFFB300, 0xFFFFB300, 0xFFFFB300, K, T, T, T, T, T, T),
        row16(T, T, K, 0xFFFF7043, K, 0xFFFFD54F, 0xFFFFE082, 0xFFFFD54F, K, 0xFFFF7043, K, T, T, T, T, T),
        row16(T, K, 0xFFFF7043, 0xFFFFB300, 0xFFFFD54F, 0xFFFFE082, 0xFFFFE082, 0xFFFFD54F, 0xFFFFB300, 0xFFFF7043, K, T, T, T, T, T),
        row16(K, 0xFFFF7043, 0xFFFFB300, 0xFFFFD54F, 0xFFFFE082, 0xFFFFE082, 0xFFFFE082, 0xFFFFE082, 0xFFFFD54F, 0xFFFFB300, 0xFFFF7043, K, T, T, T, T),
        row16(K, 0xFFE65100, 0xFFFF7043, 0xFFFFB300, 0xFFFFD54F, 0xFFFFE082, 0xFFFFE082, 0xFFFFD54F, 0xFFFFB300, 0xFFFF7043, 0xFFE65100, K, T, T, T, T),
        row16(T, K, 0xFFE65100, 0xFFFF7043, 0xFFFFB300, 0xFFFFB300, 0xFFFFB300, 0xFFFFB300, 0xFFFF7043, 0xFFE65100, K, T, T, T, T, T),
        row16(T, T, K, K, 0xFFE65100, 0xFFFF7043, 0xFFFF7043, 0xFFE65100, K, K, T, T, T, T, T, T),
        row16(T, T, T, T, K, 0xFFE65100, 0xFFE65100, K, T, T, T, T, T, T, T, T),
        row16(T, T, T, T, K, Y, Y, K, T, T, T, T, T, T, T, T),
        row16(T, T, T, K, Y, K, K, Y, K, T, T, T, T, T, T, T),
        row16(T, T, T, T, K, T, T, K, T, T, T, T, T, T, T, T)
    )

    // 5. MAGIC SLIME (16x16)
    private val SLIME_16 = arrayOf(
        row16(T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T),
        row16(T, T, T, T, T, K, K, K, K, T, T, T, T, T, T, T),
        row16(T, T, T, K, K, 0xFF29B6F6, 0xFF4FC3F7, 0xFF4FC3F7, K, K, T, T, T, T, T, T),
        row16(T, T, K, 0xFF29B6F6, 0xFF81D4FA, 0xFFE1F5FE, 0xFFE1F5FE, 0xFF4FC3F7, 0xFF0288D1, K, T, T, T, T, T, T),
        row16(T, K, 0xFF29B6F6, 0xFF4FC3F7, 0xFFE1F5FE, 0xFFE1F5FE, 0xFF4FC3F7, 0xFF4FC3F7, 0xFF0288D1, 0xFF01579B, K, T, T, T, T, T),
        row16(K, 0xFF29B6F6, 0xFF4FC3F7, 0xFF4FC3F7, 0xFF4FC3F7, 0xFF4FC3F7, 0xFF4FC3F7, 0xFF4FC3F7, 0xFF0288D1, 0xFF01579B, 0xFF01579B, K, T, T, T, T),
        row16(K, 0xFF29B6F6, 0xFF4FC3F7, K, K, 0xFF4FC3F7, 0xFF4FC3F7, K, K, 0xFF0288D1, 0xFF01579B, K, T, T, T, T),
        row16(K, 0xFF0288D1, 0xFF4FC3F7, K, W, 0xFF4FC3F7, 0xFF4FC3F7, K, W, 0xFF0288D1, 0xFF01579B, K, T, T, T, T),
        row16(K, 0xFF0288D1, 0xFF4FC3F7, 0xFF4FC3F7, 0xFF4FC3F7, 0xFF4FC3F7, 0xFF4FC3F7, 0xFF4FC3F7, 0xFF4FC3F7, 0xFF01579B, 0xFF01579B, K, T, T, T, T),
        row16(K, 0xFF0288D1, 0xFF4FC3F7, 0xFF4FC3F7, K, 0xFFE91E63, 0xFFE91E63, K, 0xFF4FC3F7, 0xFF01579B, 0xFF01579B, K, T, T, T, T),
        row16(K, 0xFF01579B, 0xFF0288D1, 0xFF4FC3F7, 0xFF4FC3F7, 0xFF4FC3F7, 0xFF4FC3F7, 0xFF0288D1, 0xFF01579B, 0xFF01579B, K, T, T, T, T, T),
        row16(T, K, 0xFF01579B, 0xFF0288D1, 0xFF0288D1, 0xFF0288D1, 0xFF0288D1, 0xFF01579B, 0xFF01579B, K, T, T, T, T, T, T),
        row16(T, T, K, K, K, K, K, K, K, K, T, T, T, T, T, T),
        row16(T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T),
        row16(T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T),
        row16(T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T)
    )

    // 6. GOBLIN THIEF (16x16)
    private val GOBLIN_16 = arrayOf(
        row16(T, T, T, T, T, K, 0xFFD32F2F, 0xFFD32F2F, 0xFFD32F2F, K, T, T, T, T, T, T),
        row16(T, T, T, T, K, 0xFFE53935, 0xFFE53935, 0xFFE53935, 0xFFE53935, 0xFFD32F2F, K, T, T, T, T, T),
        row16(T, K, K, T, K, 0xFFE53935, 0xFFE53935, 0xFFE53935, 0xFFE53935, 0xFFD32F2F, K, T, K, K, T, T),
        row16(K, 0xFF66BB6A, 0xFF43A047, K, 0xFF2E7D32, 0xFF2E7D32, 0xFF2E7D32, 0xFF2E7D32, 0xFF2E7D32, 0xFF2E7D32, K, 0xFF43A047, 0xFF66BB6A, K, T, T),
        row16(K, 0xFF43A047, 0xFF66BB6A, 0xFF43A047, 0xFF66BB6A, 0xFF66BB6A, 0xFF66BB6A, 0xFF66BB6A, 0xFF66BB6A, 0xFF66BB6A, 0xFF43A047, 0xFF66BB6A, 0xFF43A047, K, T, T),
        row16(T, K, 0xFF43A047, 0xFF66BB6A, R, R, 0xFF66BB6A, 0xFF66BB6A, R, R, 0xFF66BB6A, 0xFF43A047, K, T, T, T),
        row16(T, T, K, 0xFF66BB6A, K, K, 0xFF66BB6A, 0xFF66BB6A, K, K, 0xFF66BB6A, K, T, T, T, T),
        row16(T, T, K, 0xFF66BB6A, 0xFF66BB6A, 0xFF43A047, 0xFF43A047, 0xFF66BB6A, 0xFF66BB6A, 0xFF66BB6A, K, T, T, T, T, T),
        row16(T, T, T, K, 0xFF66BB6A, W, W, W, W, 0xFF66BB6A, K, T, T, T, T, T),
        row16(T, T, T, K, 0xFF43A047, 0xFF66BB6A, 0xFF66BB6A, 0xFF66BB6A, 0xFF66BB6A, 0xFF43A047, K, T, T, T, T, T),
        row16(T, T, K, 0xFF795548, 0xFF5D4037, 0xFF795548, 0xFF795548, 0xFF5D4037, 0xFF795548, K, T, T, T, T, T, T),
        row16(T, K, 0xFFECEFF1, K, 0xFF795548, 0xFF5D4037, 0xFF5D4037, 0xFF795548, K, 0xFF66BB6A, K, T, T, T, T, T),
        row16(T, K, 0xFFB0BEC5, K, 0xFF5D4037, 0xFF795548, 0xFF795548, 0xFF5D4037, K, 0xFF43A047, K, T, T, T, T, T),
        row16(T, T, K, K, 0xFF3E2723, K, K, 0xFF3E2723, K, K, T, T, T, T, T, T),
        row16(T, T, T, K, 0xFF4E342E, K, K, 0xFF4E342E, K, T, T, T, T, T, T, T),
        row16(T, T, T, K, K, T, T, K, K, T, T, T, T, T, T, T)
    )

    // 7. TITAN GOLEM (16x16)
    private val GOLEM_16 = arrayOf(
        row16(T, T, T, K, K, K, K, K, K, K, K, T, T, T, T, T),
        row16(T, T, K, 0xFF37474F, 0xFF455A64, 0xFF455A64, 0xFF455A64, 0xFF455A64, 0xFF37474F, K, T, T, T, T, T, T),
        row16(T, K, 0xFF455A64, 0xFF00E5FF, 0xFF00E5FF, 0xFF455A64, 0xFF455A64, 0xFF00E5FF, 0xFF00E5FF, 0xFF455A64, K, T, T, T, T, T),
        row16(T, K, 0xFF455A64, 0xFF00E5FF, K, 0xFF455A64, 0xFF455A64, 0xFF00E5FF, K, 0xFF455A64, K, T, T, T, T, T),
        row16(K, 0xFF37474F, 0xFF455A64, 0xFF455A64, 0xFF455A64, 0xFF00E5FF, 0xFF00E5FF, 0xFF455A64, 0xFF455A64, 0xFF455A64, 0xFF37474F, K, T, T, T, T),
        row16(K, 0xFF546E7A, 0xFF546E7A, 0xFF455A64, 0xFF455A64, 0xFF00E5FF, 0xFF00E5FF, 0xFF455A64, 0xFF455A64, 0xFF546E7A, 0xFF546E7A, K, T, T, T, T),
        row16(K, 0xFF37474F, 0xFF546E7A, 0xFF00E5FF, 0xFF00E5FF, 0xFF00E5FF, 0xFF00E5FF, 0xFF00E5FF, 0xFF00E5FF, 0xFF546E7A, 0xFF37474F, K, T, T, T, T),
        row16(K, 0xFF37474F, 0xFF455A64, 0xFF546E7A, 0xFF00E5FF, 0xFF00E5FF, 0xFF00E5FF, 0xFF00E5FF, 0xFF546E7A, 0xFF455A64, 0xFF37474F, K, T, T, T, T),
        row16(K, 0xFF263238, 0xFF37474F, 0xFF455A64, 0xFF546E7A, 0xFF546E7A, 0xFF546E7A, 0xFF546E7A, 0xFF455A64, 0xFF37474F, 0xFF263238, K, T, T, T, T),
        row16(T, K, 0xFF263238, 0xFF37474F, 0xFF455A64, 0xFF455A64, 0xFF455A64, 0xFF455A64, 0xFF37474F, 0xFF263238, K, T, T, T, T, T),
        row16(T, K, 0xFF37474F, 0xFF455A64, K, K, K, K, 0xFF455A64, 0xFF37474F, K, T, T, T, T, T),
        row16(T, K, 0xFF455A64, 0xFF37474F, K, T, T, K, 0xFF37474F, 0xFF455A64, K, T, T, T, T, T),
        row16(T, K, 0xFF37474F, 0xFF263238, K, T, T, K, 0xFF263238, 0xFF37474F, K, T, T, T, T, T),
        row16(T, K, K, K, K, T, T, K, K, K, K, T, T, T, T, T),
        row16(T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T),
        row16(T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T)
    )

    // 8. IGNIS DRAGON BOSS (16x16)
    private val BOSS_DRAGON_16 = arrayOf(
        row16(K, Y, T, T, T, T, T, T, T, T, T, T, Y, K, T, T),
        row16(K, 0xFFFFB300, Y, K, T, T, T, T, T, K, Y, 0xFFFFB300, K, T, T, T),
        row16(T, K, 0xFFD50000, 0xFFFFB300, K, K, K, K, K, 0xFFFFB300, 0xFFD50000, K, T, T, T, T),
        row16(K, 0xFFD50000, 0xFFFF1744, 0xFFFF1744, 0xFFFF1744, 0xFFFF1744, 0xFFFF1744, 0xFFFF1744, 0xFFFF1744, 0xFFFF1744, 0xFFD50000, K, T, T, T, T),
        row16(K, 0xFFFF1744, Y, Y, 0xFFFF1744, 0xFFFF1744, 0xFFFF1744, 0xFFFF1744, Y, Y, 0xFFFF1744, K, T, T, T, T),
        row16(K, 0xFFFF1744, K, K, 0xFFFF1744, 0xFFFF1744, 0xFFFF1744, 0xFFFF1744, K, K, 0xFFFF1744, K, T, T, T, T),
        row16(K, 0xFFFF1744, 0xFFFF1744, 0xFFFF1744, 0xFFFF1744, 0xFFFF8F00, 0xFFFF8F00, 0xFFFF1744, 0xFFFF1744, 0xFFFF1744, 0xFFFF1744, K, T, T, T, T),
        row16(T, K, 0xFFD50000, 0xFFFF1744, 0xFFFF1744, 0xFFFF1744, 0xFFFF1744, 0xFFFF1744, 0xFFFF1744, 0xFFD50000, K, T, T, T, T, T),
        row16(K, 0xFFFF5722, K, 0xFFD50000, 0xFFFFB300, 0xFFFFD54F, 0xFFFFD54F, 0xFFFFB300, 0xFFD50000, K, 0xFFFF5722, K, T, T, T, T),
        row16(K, 0xFFFF5722, 0xFFFF5722, K, 0xFFFFB300, 0xFFFFF59D, 0xFFFFF59D, 0xFFFFB300, K, 0xFFFF5722, 0xFFFF5722, K, T, T, T, T),
        row16(K, 0xFFFF5722, 0xFFFF5722, K, 0xFFFF8F00, 0xFFFFB300, 0xFFFFB300, 0xFFFF8F00, K, 0xFFFF5722, 0xFFFF5722, K, T, T, T, T),
        row16(T, K, 0xFFFF5722, K, 0xFFD50000, 0xFFFF1744, 0xFFFF1744, 0xFFD50000, K, 0xFFFF5722, K, T, T, T, T, T),
        row16(T, T, K, 0xFFD50000, K, K, K, K, 0xFFD50000, K, 0xFFFF8F00, K, T, T, T, T),
        row16(T, T, K, K, T, T, T, T, K, 0xFFFF8F00, Y, K, T, T, T, T),
        row16(T, T, T, T, T, T, T, T, T, K, K, T, T, T, T, T),
        row16(T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T)
    )

    // 9. FISH (16x16)
    private val FISH_16 = arrayOf(
        row16(T, T, T, T, T, T, T, T, T, T, 0xFF0288D1, 0xFF29B6F6, T, T, T, T),
        row16(T, T, T, T, T, T, T, T, T, 0xFF0288D1, 0xFF29B6F6, 0xFFB3E5FC, 0xFF0288D1, T, T, T),
        row16(T, T, T, T, T, T, T, T, 0xFF0288D1, 0xFF29B6F6, 0xFF29B6F6, 0xFFB3E5FC, 0xFF0288D1, T, T, T),
        row16(T, T, T, T, T, T, T, 0xFF0288D1, 0xFF29B6F6, 0xFF29B6F6, 0xFF29B6F6, K, 0xFF0288D1, T, T, T),
        row16(T, T, T, T, T, T, 0xFF0288D1, 0xFF29B6F6, 0xFF29B6F6, 0xFF29B6F6, 0xFFB3E5FC, 0xFFB3E5FC, 0xFF0288D1, T, T, T),
        row16(T, T, T, T, T, 0xFF0288D1, 0xFF29B6F6, 0xFF29B6F6, 0xFFB3E5FC, 0xFFB3E5FC, 0xFFB3E5FC, 0xFF0288D1, T, T, T, T),
        row16(T, T, T, 0xFFFF7043, 0xFF0288D1, 0xFF29B6F6, 0xFF29B6F6, 0xFFB3E5FC, 0xFF0288D1, 0xFF0288D1, 0xFF0288D1, T, T, T, T, T),
        row16(T, T, 0xFFFF7043, 0xFFFFB74D, 0xFF0288D1, 0xFF29B6F6, 0xFFB3E5FC, 0xFF0288D1, T, T, T, T, T, T, T, T),
        row16(T, 0xFFFF7043, 0xFFFFB74D, 0xFFFF7043, T, 0xFF0288D1, 0xFF0288D1, T, T, T, T, T, T, T, T, T),
        row16(0xFFFF7043, 0xFFFFB74D, 0xFFFF7043, T, T, T, T, T, T, T, T, T, T, T, T, T),
        row16(0xFFFF7043, 0xFFFF7043, T, T, T, T, T, T, T, T, T, T, T, T, T, T),
        row16(T, 0xFFFF7043, T, T, T, T, T, T, T, T, T, T, T, T, T, T),
        row16(T, T, T, T, T, 0xFFB3E5FC, 0xFF29B6F6, T, T, T, 0xFFB3E5FC, 0xFF29B6F6, T, T, T, T),
        row16(T, T, T, T, 0xFF29B6F6, 0xFF0288D1, 0xFF29B6F6, 0xFFB3E5FC, 0xFF29B6F6, 0xFF0288D1, 0xFF29B6F6, 0xFF0288D1, T, T, T, T),
        row16(T, T, T, 0xFF0288D1, 0xFF01579B, 0xFF0288D1, 0xFF0288D1, 0xFF01579B, 0xFF0288D1, 0xFF01579B, 0xFF0288D1, 0xFF01579B, T, T, T, T),
        row16(T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T)
    )

    // 10. KRAKEN / SEA MONSTER (16x16)
    private val KRAKEN_16 = arrayOf(
        row16(T, T, T, T, K, 0xFF4A148C, 0xFF6A1B9A, 0xFF6A1B9A, 0xFF4A148C, K, T, T, T, T, T, T),
        row16(T, T, T, K, 0xFF7B1FA2, 0xFFAB47BC, 0xFFCE93D8, 0xFFAB47BC, 0xFF7B1FA2, K, T, T, T, T, T, T),
        row16(T, T, K, 0xFF7B1FA2, 0xFFCE93D8, 0xFFBA68C8, 0xFFBA68C8, 0xFFCE93D8, 0xFF7B1FA2, K, T, T, T, T, T, T),
        row16(T, K, 0xFF4A148C, 0xFFAB47BC, R, R, 0xFFAB47BC, R, R, 0xFF4A148C, K, T, T, T, T, T),
        row16(T, K, 0xFF7B1FA2, 0xFFCE93D8, K, Y, 0xFFCE93D8, K, Y, 0xFF7B1FA2, K, T, T, T, T, T),
        row16(T, K, 0xFF4A148C, 0xFFAB47BC, 0xFFCE93D8, 0xFFAB47BC, 0xFFCE93D8, 0xFFAB47BC, 0xFF4A148C, K, T, T, T, T, T, T),
        row16(K, 0xFF7B1FA2, 0xFF4A148C, 0xFF4A148C, 0xFF4A148C, 0xFF4A148C, 0xFF4A148C, 0xFF4A148C, 0xFF4A148C, 0xFF7B1FA2, K, T, T, T, T, T),
        row16(K, 0xFFAB47BC, K, 0xFF7B1FA2, K, 0xFF7B1FA2, K, 0xFF7B1FA2, K, 0xFFAB47BC, K, T, T, T, T, T),
        row16(K, 0xFFCE93D8, K, 0xFFAB47BC, K, 0xFFAB47BC, K, 0xFFAB47BC, K, 0xFFCE93D8, K, T, T, T, T, T),
        row16(T, K, 0xFFCE93D8, K, 0xFFCE93D8, K, 0xFFCE93D8, K, 0xFFCE93D8, K, T, T, T, T, T, T),
        row16(T, T, K, 0xFFAB47BC, 0xFFCE93D8, 0xFFAB47BC, 0xFFCE93D8, 0xFFAB47BC, K, T, T, T, T, T, T, T),
        row16(T, K, 0xFF7B1FA2, K, K, K, K, K, 0xFF7B1FA2, K, T, T, T, T, T, T),
        row16(K, 0xFF4A148C, K, T, T, T, T, T, K, 0xFF4A148C, K, T, T, T, T, T),
        row16(K, 0xFF7B1FA2, K, T, T, T, T, T, K, 0xFF7B1FA2, K, T, T, T, T, T),
        row16(T, K, K, T, T, T, T, T, T, K, K, T, T, T, T, T),
        row16(T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T)
    )

    // 11. CROP / HARVEST (16x16)
    private val CROP_16 = arrayOf(
        row16(T, T, T, T, T, T, Y, Y, T, T, T, T, T, T, T, T),
        row16(T, T, T, T, T, Y, 0xFFF57F17, Y, T, T, T, T, T, T, T, T),
        row16(T, T, T, 0xFF81C784, T, Y, Y, 0xFFF57F17, T, 0xFF81C784, T, T, T, T, T, T),
        row16(T, T, 0xFF4CAF50, 0xFF81C784, T, T, 0xFFF57F17, Y, T, 0xFF81C784, 0xFF4CAF50, T, T, T, T, T),
        row16(T, 0xFF2E7D32, 0xFF4CAF50, T, T, Y, Y, 0xFFF57F17, T, T, 0xFF4CAF50, 0xFF2E7D32, T, T, T, T),
        row16(T, T, T, T, 0xFF81C784, Y, 0xFFF57F17, Y, 0xFF81C784, T, T, T, T, T, T, T),
        row16(T, T, T, 0xFF4CAF50, 0xFF81C784, T, Y, 0xFFF57F17, T, 0xFF81C784, 0xFF4CAF50, T, T, T, T, T),
        row16(T, T, 0xFF2E7D32, 0xFF4CAF50, T, Y, Y, 0xFFF57F17, T, T, 0xFF4CAF50, 0xFF2E7D32, T, T, T, T),
        row16(T, T, T, T, T, Y, 0xFFF57F17, Y, T, T, T, T, T, T, T, T),
        row16(T, T, T, T, T, T, 0xFF8D6E63, 0xFF5D4037, T, T, T, T, T, T, T, T),
        row16(T, T, T, T, 0xFF81C784, T, 0xFF8D6E63, 0xFF5D4037, T, 0xFF81C784, T, T, T, T, T, T),
        row16(T, T, T, 0xFF4CAF50, 0xFF81C784, T, 0xFF8D6E63, 0xFF5D4037, T, 0xFF81C784, 0xFF4CAF50, T, T, T, T, T),
        row16(T, T, T, T, T, T, 0xFF8D6E63, 0xFF5D4037, T, T, T, T, T, T, T, T),
        row16(T, T, T, T, T, T, 0xFF5D4037, 0xFF3E2723, T, T, T, T, T, T, T, T),
        row16(T, T, T, T, 0xFF4CAF50, 0xFF2E7D32, 0xFF5D4037, 0xFF3E2723, 0xFF2E7D32, 0xFF4CAF50, T, T, T, T, T, T),
        row16(T, T, T, 0xFF2E7D32, 0xFF1B5E20, 0xFF2E7D32, 0xFF3E2723, 0xFF3E2723, 0xFF2E7D32, 0xFF1B5E20, 0xFF2E7D32, T, T, T, T, T)
    )

    // 12. SEED POUCH (16x16)
    private val SEED_16 = arrayOf(
        row16(T, T, T, T, T, K, 0xFF8D6E63, 0xFF8D6E63, K, T, T, T, T, T, T, T),
        row16(T, T, T, T, K, 0xFFA1887F, 0xFFFFD54F, 0xFFFFD54F, 0xFFA1887F, K, T, T, T, T, T, T),
        row16(T, T, T, K, 0xFF8D6E63, 0xFFFFD54F, 0xFFFFD54F, 0xFFFFD54F, 0xFFFFD54F, 0xFF8D6E63, K, T, T, T, T, T),
        row16(T, T, K, 0xFF6D4C41, 0xFF8D6E63, 0xFF8D6E63, 0xFF8D6E63, 0xFF8D6E63, 0xFF8D6E63, 0xFF6D4C41, K, T, T, T, T),
        row16(T, K, 0xFF8D6E63, 0xFFA1887F, 0xFFA1887F, 0xFFA1887F, 0xFFA1887F, 0xFFA1887F, 0xFFA1887F, 0xFF8D6E63, K, T, T, T, T, T),
        row16(K, 0xFF8D6E63, 0xFFA1887F, 0xFF81C784, 0xFF4CAF50, 0xFFA1887F, 0xFFA1887F, 0xFFA1887F, 0xFFA1887F, 0xFF6D4C41, K, T, T, T, T),
        row16(K, 0xFF8D6E63, 0xFFA1887F, 0xFF4CAF50, 0xFF2E7D32, 0xFFA1887F, 0xFF81C784, 0xFFA1887F, 0xFFA1887F, 0xFF6D4C41, K, T, T, T, T),
        row16(K, 0xFF6D4C41, 0xFFA1887F, 0xFFA1887F, 0xFFA1887F, 0xFFA1887F, 0xFF4CAF50, 0xFF2E7D32, 0xFFA1887F, 0xFF5D4037, K, T, T, T, T),
        row16(K, 0xFF6D4C41, 0xFFA1887F, 0xFFA1887F, 0xFFA1887F, 0xFFA1887F, 0xFFA1887F, 0xFFA1887F, 0xFFA1887F, 0xFF5D4037, K, T, T, T, T),
        row16(K, 0xFF5D4037, 0xFF8D6E63, 0xFFA1887F, 0xFFA1887F, 0xFFA1887F, 0xFFA1887F, 0xFFA1887F, 0xFF8D6E63, 0xFF5D4037, K, T, T, T, T),
        row16(T, K, 0xFF5D4037, 0xFF8D6E63, 0xFF8D6E63, 0xFF8D6E63, 0xFF8D6E63, 0xFF8D6E63, 0xFF5D4037, K, T, T, T, T, T, T),
        row16(T, T, K, 0xFF4E342E, 0xFF5D4037, 0xFF5D4037, 0xFF5D4037, 0xFF4E342E, K, T, T, T, T, T, T, T),
        row16(T, T, T, K, K, K, K, K, K, T, T, T, T, T, T, T),
        row16(T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T),
        row16(T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T),
        row16(T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T)
    )

    // 13. REFINE STONE / CRYSTAL (16x16)
    private val STONE_16 = arrayOf(
        row16(T, T, T, T, T, K, K, K, K, T, T, T, T, T, T, T),
        row16(T, T, T, K, K, 0xFF40C4FF, 0xFF80D8FF, 0xFFE0F7FA, K, K, T, T, T, T, T, T),
        row16(T, T, K, 0xFF00B0FF, 0xFF40C4FF, 0xFF80D8FF, 0xFFFFFFFF, 0xFF80D8FF, 0xFF00B0FF, K, T, T, T, T, T, T),
        row16(T, K, 0xFF0091EA, 0xFF00B0FF, 0xFF40C4FF, 0xFF80D8FF, 0xFF80D8FF, 0xFF40C4FF, 0xFF00B0FF, 0xFF0091EA, K, T, T, T, T, T),
        row16(K, 0xFF0091EA, 0xFF0091EA, 0xFF00B0FF, 0xFF40C4FF, 0xFF40C4FF, 0xFF40C4FF, 0xFF40C4FF, 0xFF00B0FF, 0xFF0091EA, 0xFF01579B, K, T, T, T, T),
        row16(K, 0xFF01579B, 0xFF0091EA, 0xFF0091EA, 0xFF00B0FF, 0xFF00B0FF, 0xFF00B0FF, 0xFF00B0FF, 0xFF0091EA, 0xFF0091EA, 0xFF01579B, K, T, T, T, T),
        row16(K, 0xFF01579B, 0xFF01579B, 0xFF0091EA, 0xFF0091EA, 0xFF0091EA, 0xFF0091EA, 0xFF0091EA, 0xFF0091EA, 0xFF01579B, 0xFF01579B, K, T, T, T, T),
        row16(T, K, 0xFF01579B, 0xFF01579B, 0xFF0091EA, 0xFF0091EA, 0xFF0091EA, 0xFF0091EA, 0xFF01579B, 0xFF01579B, K, T, T, T, T, T),
        row16(T, T, K, 0xFF01579B, 0xFF01579B, 0xFF01579B, 0xFF01579B, 0xFF01579B, 0xFF01579B, K, T, T, T, T, T, T, T),
        row16(T, T, T, K, 0xFF01579B, 0xFF01579B, 0xFF01579B, 0xFF01579B, K, T, T, T, T, T, T, T, T),
        row16(T, T, T, T, K, 0xFF01579B, 0xFF01579B, K, T, T, T, T, T, T, T, T, T),
        row16(T, T, T, T, T, K, K, T, T, T, T, T, T, T, T, T, T),
        row16(T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T),
        row16(T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T),
        row16(T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T),
        row16(T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T)
    )

    // 14. FISHING ROD (16x16)
    private val ROD_16 = arrayOf(
        row16(T, T, T, T, T, T, T, T, T, T, T, T, T, T, Y, K),
        row16(T, T, T, T, T, T, T, T, T, T, T, T, T, Y, 0xFFFFB300, K),
        row16(T, T, T, T, T, T, T, T, T, T, T, T, Y, 0xFFFFB300, K, T),
        row16(T, T, T, T, T, T, T, T, T, T, T, Y, 0xFFFFB300, K, T, 0xFF00E5FF),
        row16(T, T, T, T, T, T, T, T, T, T, Y, 0xFFFFB300, K, T, T, 0xFF00E5FF),
        row16(T, T, T, T, T, T, T, T, T, Y, 0xFFFFB300, K, T, T, T, 0xFF00E5FF),
        row16(T, T, T, T, T, T, T, T, Y, 0xFFFFB300, K, T, T, T, T, 0xFF00E5FF),
        row16(T, T, T, T, T, T, T, Y, 0xFFFFB300, K, T, T, T, T, T, 0xFFE53935),
        row16(T, T, T, T, T, T, Y, 0xFFFFB300, K, T, T, T, T, T, T, 0xFFFFFFFF),
        row16(T, T, T, T, T, Y, 0xFFFFB300, K, T, T, T, T, T, T, T, 0xFFE53935),
        row16(T, T, T, T, Y, 0xFFFFB300, K, T, T, T, T, T, T, T, T, 0xFFB0BEC5),
        row16(T, T, T, Y, 0xFFFFB300, K, T, T, T, T, T, T, T, T, T, T),
        row16(T, T, Y, 0xFFFFB300, K, T, T, T, T, T, T, T, T, T, T, T),
        row16(T, K, 0xFF5D4037, 0xFF3E2723, K, T, T, T, T, T, T, T, T, T, T, T),
        row16(K, 0xFF5D4037, 0xFF3E2723, K, T, T, T, T, T, T, T, T, T, T, T, T),
        row16(K, K, K, T, T, T, T, T, T, T, T, T, T, T, T, T)
    )

    // 15. POTION BOTTLE (16x16)
    private val POTION_16 = arrayOf(
        row16(T, T, T, T, T, T, 0xFF8D6E63, 0xFF5D4037, T, T, T, T, T, T, T, T),
        row16(T, T, T, T, T, K, 0xFFB0BEC5, 0xFFB0BEC5, K, T, T, T, T, T, T, T),
        row16(T, T, T, T, T, K, 0xFFECEFF1, 0xFFECEFF1, K, T, T, T, T, T, T, T),
        row16(T, T, T, T, K, 0xFFB0BEC5, 0xFFB0BEC5, 0xFFB0BEC5, K, T, T, T, T, T, T),
        row16(T, T, T, K, 0xFFECEFF1, 0xFFE53935, 0xFFFF5252, 0xFFECEFF1, K, T, T, T, T, T),
        row16(T, T, K, 0xFFB0BEC5, 0xFFFF5252, 0xFFFF5252, 0xFFFF8A80, 0xFFE53935, 0xFFB0BEC5, K, T, T, T, T),
        row16(T, K, 0xFFB0BEC5, 0xFFFF1744, 0xFFFF5252, 0xFFFF8A80, 0xFFFFFFFF, 0xFFFF5252, 0xFFD50000, 0xFFB0BEC5, K, T, T, T),
        row16(T, K, 0xFFB0BEC5, 0xFFFF1744, 0xFFFF5252, 0xFFFF5252, 0xFFFF5252, 0xFFFF5252, 0xFFD50000, 0xFFB0BEC5, K, T, T, T),
        row16(T, K, 0xFFB0BEC5, 0xFFD50000, 0xFFFF1744, 0xFFFF5252, 0xFFFF5252, 0xFFFF1744, 0xFFD50000, 0xFFB0BEC5, K, T, T, T),
        row16(T, K, 0xFFB0BEC5, 0xFFD50000, 0xFFD50000, 0xFFFF1744, 0xFFFF1744, 0xFFD50000, 0xFFD50000, 0xFFB0BEC5, K, T, T, T),
        row16(T, K, 0xFF90A4AE, 0xFFB71C1C, 0xFFD50000, 0xFFD50000, 0xFFD50000, 0xFFD50000, 0xFFB71C1C, 0xFF90A4AE, K, T, T, T),
        row16(T, T, K, 0xFF90A4AE, 0xFFB71C1C, 0xFFB71C1C, 0xFFB71C1C, 0xFFB71C1C, 0xFF90A4AE, K, T, T, T, T, T),
        row16(T, T, T, K, K, K, K, K, K, T, T, T, T, T, T, T),
        row16(T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T),
        row16(T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T),
        row16(T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T)
    )

    // 16. TREASURE CHEST (16x16)
    private val CHEST_16 = arrayOf(
        row16(T, T, T, K, K, K, K, K, K, K, K, K, K, T, T, T),
        row16(T, T, K, 0xFF5D4037, 0xFF8D6E63, 0xFF8D6E63, 0xFF8D6E63, 0xFF8D6E63, 0xFF8D6E63, 0xFF8D6E63, 0xFF5D4037, K, T, T, T, T),
        row16(T, K, 0xFF8D6E63, Y, 0xFF8D6E63, 0xFF8D6E63, 0xFF8D6E63, 0xFF8D6E63, 0xFF8D6E63, Y, 0xFF8D6E63, K, T, T, T, T),
        row16(K, 0xFF8D6E63, Y, Y, Y, 0xFF8D6E63, 0xFF8D6E63, 0xFF8D6E63, Y, Y, Y, 0xFF8D6E63, K, T, T, T),
        row16(K, 0xFF5D4037, Y, 0xFFFFB300, Y, 0xFF5D4037, 0xFF5D4037, 0xFF5D4037, Y, 0xFFFFB300, Y, 0xFF5D4037, K, T, T, T),
        row16(K, K, K, K, K, K, K, K, K, K, K, K, K, K, T, T),
        row16(K, 0xFF5D4037, 0xFF8D6E63, Y, 0xFF8D6E63, Y, Y, Y, 0xFF8D6E63, Y, 0xFF8D6E63, 0xFF5D4037, K, T, T, T),
        row16(K, 0xFF5D4037, 0xFF8D6E63, Y, 0xFF8D6E63, Y, K, Y, 0xFF8D6E63, Y, 0xFF8D6E63, 0xFF5D4037, K, T, T, T),
        row16(K, 0xFF5D4037, 0xFF8D6E63, Y, 0xFF8D6E63, Y, Y, Y, 0xFF8D6E63, Y, 0xFF8D6E63, 0xFF5D4037, K, T, T, T),
        row16(K, 0xFF3E2723, 0xFF5D4037, Y, 0xFF5D4037, 0xFF5D4037, 0xFF5D4037, 0xFF5D4037, Y, 0xFF5D4037, 0xFF3E2723, K, T, T, T, T),
        row16(K, 0xFF3E2723, 0xFF5D4037, Y, 0xFF5D4037, 0xFF5D4037, 0xFF5D4037, 0xFF5D4037, Y, 0xFF5D4037, 0xFF3E2723, K, T, T, T, T),
        row16(T, K, 0xFF3E2723, Y, 0xFF3E2723, 0xFF3E2723, 0xFF3E2723, 0xFF3E2723, Y, 0xFF3E2723, K, T, T, T, T, T),
        row16(T, T, K, K, K, K, K, K, K, K, T, T, T, T, T, T),
        row16(T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T),
        row16(T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T),
        row16(T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T)
    )

    // 17. DEMON KING BOSS (16x16)
    private val DEMON_16 = arrayOf(
        row16(R, K, T, T, T, T, T, T, T, T, T, T, K, R, T, T),
        row16(K, R, R, K, T, T, T, T, T, T, K, R, R, K, T, T),
        row16(T, K, R, 0xFFB71C1C, K, K, K, K, K, K, 0xFFB71C1C, R, K, T, T, T),
        row16(T, T, K, 0xFF212121, 0xFF37474F, 0xFF455A64, 0xFF455A64, 0xFF37474F, 0xFF212121, K, T, T, T, T, T, T),
        row16(T, K, 0xFF212121, 0xFF455A64, R, R, 0xFF455A64, R, R, 0xFF455A64, 0xFF212121, K, T, T, T, T),
        row16(T, K, 0xFF37474F, 0xFF455A64, Y, R, 0xFF455A64, Y, R, 0xFF455A64, 0xFF37474F, K, T, T, T, T),
        row16(T, K, 0xFF37474F, 0xFF455A64, 0xFF455A64, 0xFF212121, 0xFF212121, 0xFF455A64, 0xFF455A64, 0xFF37474F, K, T, T, T, T, T),
        row16(T, T, K, 0xFF212121, W, K, K, W, 0xFF212121, K, T, T, T, T, T, T),
        row16(T, K, 0xFF4A148C, 0xFF311B92, 0xFF4A148C, 0xFF4A148C, 0xFF4A148C, 0xFF4A148C, 0xFF311B92, 0xFF4A148C, K, T, T, T, T, T),
        row16(K, 0xFF7B1FA2, 0xFF4A148C, 0xFF7B1FA2, 0xFFAB47BC, 0xFFAB47BC, 0xFF7B1FA2, 0xFF4A148C, 0xFF7B1FA2, K, T, T, T, T, T, T),
        row16(K, 0xFF7B1FA2, 0xFFAB47BC, 0xFF7B1FA2, 0xFF4A148C, 0xFF4A148C, 0xFF7B1FA2, 0xFFAB47BC, 0xFF7B1FA2, K, T, T, T, T, T, T),
        row16(T, K, 0xFF4A148C, 0xFF212121, 0xFF212121, 0xFF212121, 0xFF212121, 0xFF4A148C, K, T, T, T, T, T, T, T),
        row16(T, T, K, 0xFF212121, K, K, K, 0xFF212121, K, T, T, T, T, T, T, T),
        row16(T, T, K, K, T, T, T, K, K, T, T, T, T, T, T, T),
        row16(T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T),
        row16(T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T)
    )

    // 18. VOID EMPEROR / OMEGA GENESIS BOSS (16x16)
    private val VOID_16 = arrayOf(
        row16(T, T, T, T, T, C, C, C, T, T, T, T, T, T, T, T),
        row16(T, T, T, C, 0xFF7C4DFF, 0xFFB388FF, 0xFFB388FF, 0xFF7C4DFF, C, T, T, T, T, T, T, T),
        row16(T, T, C, 0xFF651FFF, 0xFF7C4DFF, 0xFFEDE7F6, 0xFFEDE7F6, 0xFF7C4DFF, 0xFF651FFF, C, T, T, T, T, T, T),
        row16(T, C, 0xFF651FFF, 0xFF6200EA, 0xFF311B92, 0xFF000000, 0xFF000000, 0xFF311B92, 0xFF6200EA, 0xFF651FFF, C, T, T, T, T, T),
        row16(C, 0xFF7C4DFF, 0xFF311B92, 0xFF000000, 0xFF000000, C, C, 0xFF000000, 0xFF000000, 0xFF311B92, 0xFF7C4DFF, C, T, T, T, T),
        row16(C, 0xFFB388FF, 0xFF000000, 0xFF000000, C, 0xFFFFFFFF, 0xFFFFFFFF, C, 0xFF000000, 0xFF000000, 0xFFB388FF, C, T, T, T, T),
        row16(C, 0xFFB388FF, 0xFF000000, 0xFF000000, C, 0xFFFFFFFF, 0xFFFFFFFF, C, 0xFF000000, 0xFF000000, 0xFFB388FF, C, T, T, T, T),
        row16(C, 0xFF7C4DFF, 0xFF311B92, 0xFF000000, 0xFF000000, C, C, 0xFF000000, 0xFF000000, 0xFF311B92, 0xFF7C4DFF, C, T, T, T, T),
        row16(T, C, 0xFF651FFF, 0xFF6200EA, 0xFF311B92, 0xFF000000, 0xFF000000, 0xFF311B92, 0xFF6200EA, 0xFF651FFF, C, T, T, T, T, T),
        row16(T, T, C, 0xFF651FFF, 0xFF7C4DFF, 0xFF311B92, 0xFF311B92, 0xFF7C4DFF, 0xFF651FFF, C, T, T, T, T, T, T),
        row16(T, T, T, C, 0xFF7C4DFF, 0xFFB388FF, 0xFFB388FF, 0xFF7C4DFF, C, T, T, T, T, T, T, T),
        row16(T, T, T, T, T, C, C, C, T, T, T, T, T, T, T, T),
        row16(T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T),
        row16(T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T),
        row16(T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T),
        row16(T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T)
    )

    fun getGrid(key: String): Array<LongArray> {
        val lower = key.lowercase()
        return when {
            // Fish & Aquatic
            lower.contains("kraken") || lower.contains("tentacle") || lower.contains("sea_monster") -> KRAKEN_16
            lower.contains("fish") || lower.contains("ikan") || lower.contains("whale") || lower.contains("salmon") || lower.contains("trout") || lower.contains("carp") || lower.contains("eel") -> FISH_16

            // Crops & Seeds
            lower.contains("crop") || lower.contains("plant") || lower.contains("harvest") || lower.contains("gandum") || lower.contains("wortel") || lower.contains("jagung") || lower.contains("tomat") || lower.contains("bawang") -> CROP_16
            lower.contains("seed") || lower.contains("benih") -> SEED_16

            // Items & Upgrades
            lower.contains("stone") || lower.contains("crystal") || lower.contains("fragment") || lower.contains("relic") || lower.contains("core") || lower.contains("ore") || lower.contains("gem") -> STONE_16
            lower.contains("rod") || lower.contains("pancing") || lower.contains("joran") || lower.contains("hook") -> ROD_16
            lower.contains("potion") || lower.contains("ramuan") || lower.contains("elixir") || lower.contains("bottle") || lower.contains("flask") -> POTION_16
            lower.contains("chest") || lower.contains("box") || lower.contains("peti") || lower.contains("mystery") -> CHEST_16

            // Bosses & Dungeon Monsters
            lower.contains("void") || lower.contains("omega") || lower.contains("ancient") || lower.contains("chaos") -> VOID_16
            lower.contains("demon") || lower.contains("devil") || lower.contains("diablo") -> DEMON_16
            lower.contains("boss") -> BOSS_DRAGON_16
            lower.contains("dragon") || lower.contains("drake") || lower.contains("wyvern") || lower.contains("leviathan") -> DRAGON_16
            lower.contains("turtle") || lower.contains("crab") -> TURTLE_16
            lower.contains("bird") || lower.contains("phoenix") || lower.contains("bat") || lower.contains("griffin") || lower.contains("angel") -> BIRD_16
            lower.contains("slime") -> SLIME_16
            lower.contains("goblin") || lower.contains("thief") || lower.contains("spider") -> GOBLIN_16
            lower.contains("golem") || lower.contains("treant") || lower.contains("boar") -> GOLEM_16
            lower.contains("wolf") || lower.contains("hound") || lower.contains("kitsune") || lower.contains("fox") || lower.contains("cat") -> WOLF_16
            else -> SLIME_16
        }
    }
}
