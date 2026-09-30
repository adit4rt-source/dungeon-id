package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PlayerProfileEntity
import com.example.data.local.entity.UserAccountEntity
import com.example.data.model.ItemRarity
import com.example.ui.theme.PressStartFontFamily
import com.example.ui.theme.Vt323FontFamily
import com.example.util.LocalSoundManager

@Composable
fun PlayerTopAppBar(
    profile: PlayerProfileEntity?,
    userAccount: UserAccountEntity? = null,
    onOpenAccountDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (profile == null) return
    val soundManager = LocalSoundManager.current
    var isMuted by remember { mutableStateOf(soundManager.isMuted()) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0D0B14), RectangleShape)
            .padding(bottom = 3.dp)
            .background(Color(0xFF382B54), RectangleShape)
            .padding(bottom = 2.dp)
            .background(Color(0xFF191428), RectangleShape)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("player_header_bar")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Player Avatar & Level
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PixelSprite(
                        spriteKey = "pet_wolf",
                        size = 32.dp,
                        animated = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = profile.name,
                                fontFamily = PressStartFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD54F)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            PixelBadge(
                                text = "Lv.${profile.level}",
                                backgroundColor = Color(0xFF6A1B9A),
                                textColor = Color(0xFFE1BEE7)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        // EXP progress text
                        Text(
                            text = "EXP ${profile.exp}/${profile.maxExp}",
                            fontFamily = Vt323FontFamily,
                            fontSize = 13.sp,
                            color = Color(0xFFB0BEC5)
                        )
                    }
                }

                // Currencies: Gold, Diamonds, Upgrade Stones, Account & Sound Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PixelCurrencyChip(icon = "🪙", value = "${profile.gold}", color = Color(0xFFFFD54F), testTag = "gold_badge")
                    PixelCurrencyChip(icon = "💎", value = "${profile.diamonds}", color = Color(0xFF4FC3F7), testTag = "diamond_badge")
                    PixelCurrencyChip(icon = "🪨", value = "${profile.upgradeStones}", color = Color(0xFFCE93D8), testTag = "stones_badge")

                    PixelButton(
                        onClick = {
                            soundManager.playMenuClick()
                            onOpenAccountDialog()
                        },
                        backgroundColor = getProviderColor(userAccount?.provider ?: "GUEST"),
                        testTag = "open_account_dialog_button"
                    ) {
                        Text(
                            text = "${getProviderEmoji(userAccount?.provider ?: "GUEST")} ${userAccount?.username?.take(6) ?: "AKUN"}",
                            fontFamily = PressStartFontFamily,
                            fontSize = 6.sp,
                            color = Color.White
                        )
                    }

                    PixelButton(
                        onClick = {
                            isMuted = soundManager.toggleMute()
                        },
                        backgroundColor = if (isMuted) Color(0xFFB71C1C) else Color(0xFF281C3F),
                        testTag = "sound_mute_toggle"
                    ) {
                        Text(
                            text = if (isMuted) "🔇" else "🔊",
                            fontSize = 8.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Stepped Health & Stamina Bars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("HP", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color(0xFFFF5252))
                        Text("${profile.hp}/${profile.maxHp}", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    PixelStepProgressBar(
                        current = profile.hp,
                        max = profile.maxHp,
                        barColor = Color(0xFFE53935),
                        height = 10.dp,
                        showLabel = false
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("STM", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color(0xFFFFD54F))
                        Text("${profile.energy}/${profile.maxEnergy}", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    PixelStepProgressBar(
                        current = profile.energy,
                        max = profile.maxEnergy,
                        barColor = Color(0xFFFFB300),
                        height = 10.dp,
                        showLabel = false
                    )
                }
            }
        }
    }
}

@Composable
fun PixelCurrencyChip(
    icon: String,
    value: String,
    color: Color,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFF0A0914), RectangleShape)
            .padding(1.dp)
            .background(Color(0xFF221A36), RectangleShape)
            .padding(horizontal = 5.dp, vertical = 2.dp)
            .testTag(testTag)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 10.sp)
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = value,
                fontFamily = PressStartFontFamily,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun RarityChip(
    rarity: ItemRarity,
    modifier: Modifier = Modifier
) {
    PixelBadge(
        text = rarity.label.uppercase(),
        backgroundColor = rarity.composeColor().copy(alpha = 0.8f),
        textColor = Color.White,
        modifier = modifier
    )
}

@Composable
fun GlowingPulseBox(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pixel_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = modifier.scale(scale),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
