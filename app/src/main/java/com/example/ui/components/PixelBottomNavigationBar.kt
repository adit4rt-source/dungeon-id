package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.GameScreen
import com.example.ui.theme.PressStartFontFamily

enum class PixelNavIconType {
    HOME,
    FARMING,
    FISHING,
    HUNTING,
    INVENTORY
}

object PixelNavSpriteDefinitions {
    private const val CLR = 0x00000000L

    val FARMING_GRID: Array<LongArray> = arrayOf(
        longArrayOf(CLR,CLR,CLR,CLR,CLR,CLR,0xFFFFD54FL,0xFFFFD54FL,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,CLR,CLR,CLR,0xFFFFD54FL,0xFFF57F17L,0xFFFFD54FL,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,CLR,0xFF81C784L,CLR,0xFFFFD54FL,0xFFFFD54FL,0xFFF57F17L,CLR,0xFF81C784L,CLR,CLR,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,0xFF4CAF50L,0xFF81C784L,CLR,CLR,0xFFF57F17L,0xFFFFD54FL,CLR,0xFF81C784L,0xFF4CAF50L,CLR,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,0xFF2E7D32L,0xFF4CAF50L,CLR,CLR,0xFFFFD54FL,0xFFFFD54FL,0xFFF57F17L,CLR,CLR,0xFF4CAF50L,0xFF2E7D32L,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,CLR,CLR,0xFF81C784L,0xFFFFD54FL,0xFFF57F17L,0xFFFFD54FL,0xFF81C784L,CLR,CLR,CLR,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,CLR,0xFF4CAF50L,0xFF81C784L,CLR,0xFFFFD54FL,0xFFF57F17L,CLR,0xFF81C784L,0xFF4CAF50L,CLR,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,0xFF2E7D32L,0xFF4CAF50L,CLR,0xFFFFD54FL,0xFFFFD54FL,0xFFF57F17L,CLR,CLR,0xFF4CAF50L,0xFF2E7D32L,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,CLR,CLR,CLR,0xFFFFD54FL,0xFFF57F17L,0xFFFFD54FL,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,CLR,CLR,CLR,CLR,0xFF8D6E63L,0xFF5D4037L,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,CLR,CLR,0xFF81C784L,CLR,0xFF8D6E63L,0xFF5D4037L,CLR,0xFF81C784L,CLR,CLR,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,CLR,0xFF4CAF50L,0xFF81C784L,CLR,0xFF8D6E63L,0xFF5D4037L,CLR,0xFF81C784L,0xFF4CAF50L,CLR,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,CLR,CLR,CLR,CLR,0xFF8D6E63L,0xFF5D4037L,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,CLR,CLR,CLR,CLR,0xFF5D4037L,0xFF3E2723L,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,CLR,CLR,0xFF4CAF50L,0xFF2E7D32L,0xFF5D4037L,0xFF3E2723L,0xFF2E7D32L,0xFF4CAF50L,CLR,CLR,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,CLR,0xFF2E7D32L,0xFF1B5E20L,0xFF2E7D32L,0xFF3E2723L,0xFF3E2723L,0xFF2E7D32L,0xFF1B5E20L,0xFF2E7D32L,CLR,CLR,CLR,CLR,CLR)
    )

    val FISHING_GRID: Array<LongArray> = arrayOf(
        longArrayOf(CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,0xFF0288D1L,0xFF29B6F6L,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,0xFF0288D1L,0xFF29B6F6L,0xFFB3E5FCL,0xFF0288D1L,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,0xFF0288D1L,0xFF29B6F6L,0xFF29B6F6L,0xFFB3E5FCL,0xFF0288D1L,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,CLR,CLR,CLR,CLR,CLR,0xFF0288D1L,0xFF29B6F6L,0xFF29B6F6L,0xFF29B6F6L,0xFF000000L,0xFF0288D1L,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,CLR,CLR,CLR,CLR,0xFF0288D1L,0xFF29B6F6L,0xFF29B6F6L,0xFF29B6F6L,0xFFB3E5FCL,0xFFB3E5FCL,0xFF0288D1L,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,CLR,CLR,CLR,0xFF0288D1L,0xFF29B6F6L,0xFF29B6F6L,0xFFB3E5FCL,0xFFB3E5FCL,0xFFB3E5FCL,0xFF0288D1L,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,CLR,0xFFFF7043L,0xFF0288D1L,0xFF29B6F6L,0xFF29B6F6L,0xFFB3E5FCL,0xFF0288D1L,0xFF0288D1L,0xFF0288D1L,CLR,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,0xFFFF7043L,0xFFFFB74DL,0xFF0288D1L,0xFF29B6F6L,0xFFB3E5FCL,0xFF0288D1L,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,0xFFFF7043L,0xFFFFB74DL,0xFFFF7043L,CLR,0xFF0288D1L,0xFF0288D1L,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR),
        longArrayOf(0xFFFF7043L,0xFFFFB74DL,0xFFFF7043L,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR),
        longArrayOf(0xFFFF7043L,0xFFFF7043L,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,0xFFFF7043L,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,CLR,CLR,CLR,0xFFB3E5FCL,0xFF29B6F6L,CLR,CLR,CLR,0xFFB3E5FCL,0xFF29B6F6L,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,CLR,CLR,0xFF29B6F6L,0xFF0288D1L,0xFF29B6F6L,0xFFB3E5FCL,0xFF29B6F6L,0xFF0288D1L,0xFF29B6F6L,0xFF0288D1L,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,CLR,0xFF0288D1L,0xFF01579BL,0xFF0288D1L,0xFF0288D1L,0xFF01579BL,0xFF0288D1L,0xFF01579BL,0xFF0288D1L,0xFF01579BL,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR)
    )

    val HUNTING_GRID: Array<LongArray> = arrayOf(
        longArrayOf(CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,0xFFECEFF1L,0xFFB0BEC5L),
        longArrayOf(CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,0xFFECEFF1L,0xFFB0BEC5L,0xFF90A4AEL),
        longArrayOf(CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,0xFFECEFF1L,0xFFB0BEC5L,0xFF90A4AEL,CLR),
        longArrayOf(0xFFECEFF1L,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,0xFFECEFF1L,0xFFB0BEC5L,0xFF90A4AEL,CLR,CLR),
        longArrayOf(CLR,0xFFECEFF1L,0xFFB0BEC5L,CLR,CLR,CLR,CLR,CLR,CLR,CLR,0xFFECEFF1L,0xFFB0BEC5L,0xFF90A4AEL,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,0xFFECEFF1L,0xFFB0BEC5L,CLR,CLR,CLR,CLR,CLR,0xFFECEFF1L,0xFFB0BEC5L,0xFF90A4AEL,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,CLR,0xFFECEFF1L,0xFFB0BEC5L,CLR,CLR,CLR,0xFFECEFF1L,0xFFB0BEC5L,0xFF90A4AEL,CLR,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,CLR,CLR,0xFFECEFF1L,0xFFB0BEC5L,CLR,0xFFECEFF1L,0xFFB0BEC5L,0xFF90A4AEL,CLR,CLR,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,CLR,CLR,CLR,0xFFECEFF1L,0xFFFFD54FL,0xFFFFD54FL,0xFF90A4AEL,CLR,CLR,CLR,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,CLR,CLR,CLR,0xFFFFD54FL,0xFFE53935L,0xFFE53935L,0xFFFFD54FL,CLR,CLR,CLR,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,CLR,CLR,0xFFFFD54FL,0xFFFFD54FL,0xFFE53935L,0xFFE53935L,0xFFFFD54FL,0xFFFFD54FL,CLR,CLR,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,CLR,CLR,CLR,CLR,0xFFFFD54FL,0xFFFFD54FL,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,CLR,CLR,CLR,0xFF5D4037L,0xFF3E2723L,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,CLR,CLR,0xFF5D4037L,0xFF3E2723L,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,CLR,0xFFFFD54FL,0xFFFFCA28L,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,0xFFFFD54FL,0xFFFFCA28L,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR)
    )

    val INVENTORY_GRID: Array<LongArray> = arrayOf(
        longArrayOf(CLR,CLR,CLR,CLR,CLR,0xFF5D4037L,0xFF5D4037L,0xFF5D4037L,0xFF5D4037L,0xFF5D4037L,0xFF5D4037L,CLR,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,CLR,CLR,0xFF5D4037L,0xFF8D6E63L,0xFF8D6E63L,0xFF8D6E63L,0xFF8D6E63L,0xFF8D6E63L,0xFF8D6E63L,0xFF5D4037L,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,CLR,0xFF5D4037L,0xFF8D6E63L,0xFFA1887FL,0xFFA1887FL,0xFFA1887FL,0xFFA1887FL,0xFFA1887FL,0xFF8D6E63L,0xFF5D4037L,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,0xFF3E2723L,0xFF8D6E63L,0xFFA1887FL,0xFF3E2723L,0xFF3E2723L,0xFF3E2723L,0xFF3E2723L,0xFFA1887FL,0xFF8D6E63L,0xFF3E2723L,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,0xFF3E2723L,0xFF8D6E63L,0xFFA1887FL,0xFF3E2723L,0xFFFFD54FL,0xFFFFD54FL,0xFF3E2723L,0xFFA1887FL,0xFF8D6E63L,0xFF3E2723L,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,0xFF3E2723L,0xFF8D6E63L,0xFF8D6E63L,0xFFA1887FL,0xFF3E2723L,0xFFFFD54FL,0xFFFFD54FL,0xFF3E2723L,0xFFA1887FL,0xFF8D6E63L,0xFF8D6E63L,0xFF3E2723L,CLR,CLR,CLR),
        longArrayOf(CLR,0xFF3E2723L,0xFF8D6E63L,0xFF8D6E63L,0xFF8D6E63L,0xFF8D6E63L,0xFF8D6E63L,0xFF8D6E63L,0xFF8D6E63L,0xFF8D6E63L,0xFF8D6E63L,0xFF8D6E63L,0xFF3E2723L,CLR,CLR,CLR),
        longArrayOf(CLR,0xFF3E2723L,0xFF5D4037L,0xFF5D4037L,0xFF5D4037L,0xFF5D4037L,0xFF5D4037L,0xFF5D4037L,0xFF5D4037L,0xFF5D4037L,0xFF5D4037L,0xFF5D4037L,0xFF3E2723L,CLR,CLR,CLR),
        longArrayOf(CLR,0xFF3E2723L,0xFF8D6E63L,0xFF3E2723L,0xFF8D6E63L,0xFF8D6E63L,0xFF8D6E63L,0xFF8D6E63L,0xFF8D6E63L,0xFF8D6E63L,0xFF3E2723L,0xFF8D6E63L,0xFF3E2723L,CLR,CLR,CLR),
        longArrayOf(CLR,0xFF3E2723L,0xFF8D6E63L,0xFF3E2723L,0xFF8D6E63L,0xFF8D6E63L,0xFFFFD54FL,0xFFFFD54FL,0xFF8D6E63L,0xFF8D6E63L,0xFF3E2723L,0xFF8D6E63L,0xFF3E2723L,CLR,CLR,CLR),
        longArrayOf(CLR,0xFF3E2723L,0xFF8D6E63L,0xFF3E2723L,0xFF8D6E63L,0xFF8D6E63L,0xFFFFD54FL,0xFFFFD54FL,0xFF8D6E63L,0xFF8D6E63L,0xFF3E2723L,0xFF8D6E63L,0xFF3E2723L,CLR,CLR,CLR),
        longArrayOf(CLR,0xFF3E2723L,0xFF8D6E63L,0xFF3E2723L,0xFF8D6E63L,0xFF8D6E63L,0xFF8D6E63L,0xFF8D6E63L,0xFF8D6E63L,0xFF8D6E63L,0xFF3E2723L,0xFF8D6E63L,0xFF3E2723L,CLR,CLR,CLR),
        longArrayOf(CLR,0xFF3E2723L,0xFF8D6E63L,0xFF3E2723L,0xFF8D6E63L,0xFF8D6E63L,0xFF8D6E63L,0xFF8D6E63L,0xFF8D6E63L,0xFF8D6E63L,0xFF3E2723L,0xFF8D6E63L,0xFF3E2723L,CLR,CLR,CLR),
        longArrayOf(CLR,0xFF3E2723L,0xFF5D4037L,0xFF3E2723L,0xFF5D4037L,0xFF5D4037L,0xFF5D4037L,0xFF5D4037L,0xFF5D4037L,0xFF5D4037L,0xFF3E2723L,0xFF5D4037L,0xFF3E2723L,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,0xFF3E2723L,0xFF3E2723L,0xFF3E2723L,0xFF3E2723L,0xFF3E2723L,0xFF3E2723L,0xFF3E2723L,0xFF3E2723L,0xFF3E2723L,0xFF3E2723L,CLR,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR)
    )

    val HOME_GRID: Array<LongArray> = arrayOf(
        longArrayOf(CLR,CLR,0xFFD32F2FL,CLR,CLR,CLR,CLR,0xFFD32F2FL,CLR,CLR,CLR,CLR,0xFFD32F2FL,CLR,CLR,CLR),
        longArrayOf(CLR,0xFFD32F2FL,0xFFE53935L,0xFFD32F2FL,CLR,CLR,0xFFD32F2FL,0xFFE53935L,0xFFD32F2FL,CLR,CLR,0xFFD32F2FL,0xFFE53935L,0xFFD32F2FL,CLR,CLR),
        longArrayOf(CLR,0xFF78909CL,0xFFB0BEC5L,0xFF78909CL,CLR,CLR,0xFF78909CL,0xFFB0BEC5L,0xFF78909CL,CLR,CLR,0xFF78909CL,0xFFB0BEC5L,0xFF78909CL,CLR,CLR),
        longArrayOf(CLR,0xFF78909CL,0xFFB0BEC5L,0xFF78909CL,0xFF78909CL,0xFF78909CL,0xFF78909CL,0xFFB0BEC5L,0xFF78909CL,0xFF78909CL,0xFF78909CL,0xFF78909CL,0xFFB0BEC5L,0xFF78909CL,CLR,CLR),
        longArrayOf(CLR,0xFF546E7AL,0xFF78909CL,0xFF546E7AL,0xFFB0BEC5L,0xFFB0BEC5L,0xFF546E7AL,0xFF78909CL,0xFF546E7AL,0xFFB0BEC5L,0xFFB0BEC5L,0xFF546E7AL,0xFF78909CL,0xFF546E7AL,CLR,CLR),
        longArrayOf(CLR,0xFF546E7AL,0xFF37474FL,0xFF546E7AL,0xFFB0BEC5L,0xFFB0BEC5L,0xFF546E7AL,0xFF37474FL,0xFF546E7AL,0xFFB0BEC5L,0xFFB0BEC5L,0xFF546E7AL,0xFF37474FL,0xFF546E7AL,CLR,CLR),
        longArrayOf(CLR,0xFF546E7AL,0xFF78909CL,0xFF546E7AL,0xFFB0BEC5L,0xFFB0BEC5L,0xFF546E7AL,0xFF78909CL,0xFF546E7AL,0xFFB0BEC5L,0xFFB0BEC5L,0xFF546E7AL,0xFF78909CL,0xFF546E7AL,CLR,CLR),
        longArrayOf(CLR,0xFF455A64L,0xFF546E7AL,0xFF455A64L,0xFF78909CL,0xFF78909CL,0xFF455A64L,0xFF546E7AL,0xFF455A64L,0xFF78909CL,0xFF78909CL,0xFF455A64L,0xFF546E7AL,0xFF455A64L,CLR,CLR),
        longArrayOf(CLR,0xFF455A64L,0xFF78909CL,0xFF455A64L,0xFF78909CL,0xFF78909CL,0xFF455A64L,0xFF546E7AL,0xFF455A64L,0xFF78909CL,0xFF78909CL,0xFF455A64L,0xFF78909CL,0xFF455A64L,CLR,CLR),
        longArrayOf(CLR,0xFF37474FL,0xFF455A64L,0xFF37474FL,0xFF546E7AL,0xFF546E7AL,0xFF37474FL,0xFF455A64L,0xFF37474FL,0xFF546E7AL,0xFF546E7AL,0xFF37474FL,0xFF455A64L,0xFF37474FL,CLR,CLR),
        longArrayOf(CLR,0xFF37474FL,0xFF546E7AL,0xFF37474FL,0xFF37474FL,0xFF546E7AL,0xFF546E7AL,0xFF546E7AL,0xFF546E7AL,0xFF546E7AL,0xFF37474FL,0xFF37474FL,0xFF546E7AL,0xFF37474FL,CLR,CLR),
        longArrayOf(CLR,0xFF263238L,0xFF37474FL,0xFF263238L,0xFF37474FL,0xFF5D4037L,0xFF5D4037L,0xFF5D4037L,0xFF5D4037L,0xFF37474FL,0xFF263238L,0xFF37474FL,0xFF263238L,CLR,CLR,CLR),
        longArrayOf(CLR,0xFF263238L,0xFF37474FL,0xFF263238L,0xFF37474FL,0xFF8D6E63L,0xFFFFD54FL,0xFF8D6E63L,0xFF3E2723L,0xFF37474FL,0xFF263238L,0xFF37474FL,0xFF263238L,CLR,CLR,CLR),
        longArrayOf(CLR,0xFF263238L,0xFF37474FL,0xFF263238L,0xFF37474FL,0xFF8D6E63L,0xFF8D6E63L,0xFFFFD54FL,0xFF3E2723L,0xFF37474FL,0xFF263238L,0xFF37474FL,0xFF263238L,CLR,CLR,CLR),
        longArrayOf(CLR,0xFF263238L,0xFF263238L,0xFF263238L,0xFF263238L,0xFF5D4037L,0xFF3E2723L,0xFF3E2723L,0xFF3E2723L,0xFF263238L,0xFF263238L,0xFF263238L,0xFF263238L,CLR,CLR,CLR),
        longArrayOf(CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR,CLR)
    )

    fun getGrid(type: PixelNavIconType): Array<LongArray> {
        return when (type) {
            PixelNavIconType.FARMING -> FARMING_GRID
            PixelNavIconType.FISHING -> FISHING_GRID
            PixelNavIconType.HUNTING -> HUNTING_GRID
            PixelNavIconType.INVENTORY -> INVENTORY_GRID
            PixelNavIconType.HOME -> HOME_GRID
        }
    }
}

/**
 * Renders a crisp 16x16 pixel-art icon for navigation tabs.
 */
@Composable
fun PixelNavIcon(
    iconType: PixelNavIconType,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 26.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "nav_icon_anim")
    val bounceY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -3f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tab_bounce"
    )

    val translationOffset = if (isSelected) bounceY else 0f
    val grid = PixelNavSpriteDefinitions.getGrid(iconType)

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer { translationY = translationOffset }
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val canvasW = this.size.width
            val canvasH = this.size.height
            if (canvasW <= 0f || canvasH <= 0f) return@Canvas

            val rows = grid.size
            if (rows <= 0) return@Canvas
            val pixelH = canvasH / rows.toFloat()

            for (r in 0 until rows) {
                val row = grid[r]
                val cols = row.size
                if (cols <= 0) continue
                val pixelW = canvasW / cols.toFloat()

                for (c in 0 until cols) {
                    val colorHex = row[c]
                    if (colorHex != 0L) {
                        val baseColor = Color(colorHex)
                        val finalColor = if (isSelected) {
                            baseColor
                        } else {
                            baseColor.copy(alpha = 0.55f)
                        }
                        drawRect(
                            color = finalColor,
                            topLeft = Offset(c * pixelW, r * pixelH),
                            size = Size(pixelW + 0.3f, pixelH + 0.3f)
                        )
                    }
                }
            }
        }
    }
}

data class PixelNavItem(
    val screen: GameScreen,
    val iconType: PixelNavIconType,
    val label: String,
    val testTag: String
)

/**
 * Custom retro bottom navigation bar without using navigation libraries.
 * Driven entirely by state switching (`currentScreen` and `onSelectScreen`).
 */
@Composable
fun PixelBottomNavigationBar(
    currentScreen: GameScreen,
    onSelectScreen: (GameScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val navItems = remember {
        listOf(
            PixelNavItem(GameScreen.HOME, PixelNavIconType.HOME, "DESA", "nav_tab_home"),
            PixelNavItem(GameScreen.FARMING, PixelNavIconType.FARMING, "KEBUN", "nav_tab_farming"),
            PixelNavItem(GameScreen.FISHING, PixelNavIconType.FISHING, "MANCING", "nav_tab_fishing"),
            PixelNavItem(GameScreen.ADVENTURE, PixelNavIconType.HUNTING, "BERBURU", "nav_tab_hunting"),
            PixelNavItem(GameScreen.INVENTORY, PixelNavIconType.INVENTORY, "TAS", "nav_tab_inventory")
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF07050E), RectangleShape)
            .padding(top = 2.dp)
            .background(Color(0xFF3B2D54), RectangleShape)
            .padding(top = 2.dp)
            .background(Color(0xFF140F22), RectangleShape)
            .testTag("pixel_bottom_navigation_bar"),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth()) {
            val isCompact = maxWidth < 360.dp

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = if (isCompact) 2.dp else 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEach { item ->
                    val isSelected = currentScreen == item.screen
                    PixelNavTabButton(
                        item = item,
                        isSelected = isSelected,
                        isCompact = isCompact,
                        onClick = { onSelectScreen(item.screen) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PixelNavTabButton(
    item: PixelNavItem,
    isSelected: Boolean,
    isCompact: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .defaultMinSize(minWidth = if (isCompact) 44.dp else 52.dp, minHeight = if (isCompact) 44.dp else 50.dp)
            .padding(horizontal = 1.dp, vertical = 2.dp)
            .background(
                if (isSelected) Color(0xFF281C3F) else Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            )
            .semantics {
                role = Role.Tab
                contentDescription = "${item.label} Tab${if (isSelected) ", Terpilih" else ""}"
            }
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color(0xFFFFD54F)),
                onClick = onClick
            )
            .padding(vertical = if (isCompact) 2.dp else 4.dp)
            .testTag(item.testTag)
            .then(
                if (item.screen == GameScreen.ADVENTURE) Modifier.testTag("nav_tab_adventure") else Modifier
            )
    ) {
        // Pixel Art Icon
        PixelNavIcon(
            iconType = item.iconType,
            isSelected = isSelected,
            size = if (isCompact) 20.dp else 24.dp
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Label
        Text(
            text = item.label,
            fontFamily = PressStartFontFamily,
            fontSize = if (isCompact) 5.5.sp else 6.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color(0xFFFFD54F) else Color(0xFF8A9BA8)
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Pixel active dot indicator
        Box(
            modifier = Modifier
                .size(if (isCompact) 3.dp else 4.dp)
                .background(
                    if (isSelected) Color(0xFFFFD54F) else Color.Transparent,
                    shape = RectangleShape
                )
        )
    }
}
