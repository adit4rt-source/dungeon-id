package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PressStartFontFamily
import com.example.ui.theme.Vt323FontFamily

val RetroDarkStone = Color(0xFF14121E)
val RetroPanelBg = Color(0xFF1E1A2E)
val RetroBorderOuter = Color(0xFF0A0914)
val RetroBorderHighlight = Color(0xFF5A4D7E)
val RetroGold = Color(0xFFFFD54F)
val RetroGoldDark = Color(0xFFFF8F00)

@Composable
fun PixelFrame(
    modifier: Modifier = Modifier,
    backgroundColor: Color = RetroPanelBg,
    borderColor: Color = RetroBorderHighlight,
    contentPadding: Dp = 12.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(RetroBorderOuter, RectangleShape)
            .padding(2.dp)
            .background(borderColor, RectangleShape)
            .padding(2.dp)
            .background(backgroundColor, RectangleShape)
            .padding(contentPadding)
    ) {
        content()
    }
}

@Composable
fun PixelButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = Color(0xFF3949AB),
    testTag: String = "",
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val actualBg = if (enabled) backgroundColor else Color(0xFF424242)
    val offsetY = if (isPressed && enabled) 2.dp else 0.dp

    Box(
        modifier = modifier
            .offset(y = offsetY)
            .background(RetroBorderOuter, RectangleShape)
            .padding(2.dp)
            .background(if (enabled) Color(0xFF8C9EFF) else Color(0xFF616161), RectangleShape)
            .padding(2.dp)
            .background(actualBg, RectangleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .testTag(testTag)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun PixelStepProgressBar(
    current: Int,
    max: Int,
    barColor: Color,
    modifier: Modifier = Modifier,
    height: Dp = 14.dp,
    showLabel: Boolean = true
) {
    val ratio = if (max > 0) (current.toFloat() / max.toFloat()).coerceIn(0f, 1f) else 0f

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .background(RetroBorderOuter, RectangleShape)
                .padding(2.dp)
                .background(Color(0xFF263238), RectangleShape)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(ratio)
                    .height(height)
                    .background(barColor, RectangleShape)
            )
        }
        if (showLabel) {
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$current/$max",
                    fontFamily = PressStartFontFamily,
                    fontSize = 8.sp,
                    color = Color(0xFFCFD8DC)
                )
            }
        }
    }
}

@Composable
fun PixelBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF2E7D32),
    textColor: Color = Color.White
) {
    Box(
        modifier = modifier
            .background(RetroBorderOuter, RectangleShape)
            .padding(1.dp)
            .background(backgroundColor, RectangleShape)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontFamily = PressStartFontFamily,
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun PixelDivider(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF382B54)
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(color, RectangleShape)
    )
}

@Composable
fun PixelStatTag(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    labelColor: Color = Color(0xFFB0BEC5),
    valueColor: Color = RetroGold
) {
    Row(
        modifier = modifier
            .background(Color(0xFF0F0C1B), RectangleShape)
            .padding(1.dp)
            .background(Color(0xFF1E172E), RectangleShape)
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontFamily = PressStartFontFamily, fontSize = 6.sp, color = labelColor)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = value, fontFamily = PressStartFontFamily, fontSize = 7.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}
