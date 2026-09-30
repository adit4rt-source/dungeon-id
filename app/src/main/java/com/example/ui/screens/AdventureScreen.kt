package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.DungeonCombatState
import com.example.data.model.DungeonDifficulty
import com.example.data.model.DungeonRegistry
import com.example.data.model.DungeonWave
import com.example.data.model.HuntingDungeon
import com.example.ui.components.GlowingPulseBox
import com.example.ui.components.PixelBadge
import com.example.ui.components.PixelButton
import com.example.ui.components.PixelFrame
import com.example.ui.components.PixelSprite
import com.example.ui.components.PixelStepProgressBar
import com.example.ui.components.RarityChip
import com.example.ui.components.RetroGold
import com.example.ui.theme.PressStartFontFamily
import com.example.ui.theme.Vt323FontFamily
import com.example.ui.viewmodel.DungeonViewModel
import com.example.ui.viewmodel.GameViewModel
import com.example.util.LocalSoundManager
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AdventureScreen(
    viewModel: GameViewModel,
    dungeonViewModel: DungeonViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val soundManager = LocalSoundManager.current
    val combatState by dungeonViewModel.combatState.collectAsStateWithLifecycle()
    val player by dungeonViewModel.playerProfile.collectAsStateWithLifecycle()
    val equippedPet by dungeonViewModel.equippedPet.collectAsStateWithLifecycle()
    val selectedDifficulty by dungeonViewModel.selectedDifficulty.collectAsStateWithLifecycle()

    LaunchedEffect(combatState) {
        when (combatState) {
            is DungeonCombatState.DungeonVictory -> soundManager.playVictory()
            is DungeonCombatState.DungeonDefeat -> soundManager.playCombatHit()
            is DungeonCombatState.ResolvingAction -> soundManager.playCombatHit()
            else -> {}
        }
    }

    LaunchedEffect(Unit) {
        dungeonViewModel.notificationEvents.collectLatest { msg ->
            viewModel.showToast(msg)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F0C1B))
            .testTag("adventure_screen_root")
    ) {
        // Pixel Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D0B14))
                .padding(bottom = 2.dp)
                .background(Color(0xFF880E4F))
                .padding(bottom = 2.dp)
                .background(Color(0xFF24101A))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (combatState !is DungeonCombatState.HubSelection) {
                            dungeonViewModel.exitDungeonToHub()
                        }
                        onNavigateBack()
                    },
                    modifier = Modifier.testTag("adventure_back_btn")
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DUNGEON BERBURU [PIXEL]",
                        fontFamily = PressStartFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = RetroGold
                    )
                    Text(
                        text = "Kalahkan monster pixel, boss & raih batu tempa",
                        fontFamily = Vt323FontFamily,
                        fontSize = 14.sp,
                        color = Color(0xFFFF80AB)
                    )
                }

                if (combatState !is DungeonCombatState.HubSelection &&
                    combatState !is DungeonCombatState.DungeonVictory &&
                    combatState !is DungeonCombatState.DungeonDefeat
                ) {
                    PixelButton(
                        onClick = { dungeonViewModel.fleeDungeon() },
                        backgroundColor = Color(0xFF4E342E),
                        testTag = "flee_dungeon_btn"
                    ) {
                        Text("LARI", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color.White)
                    }
                }
            }
        }

        when (val state = combatState) {
            is DungeonCombatState.HubSelection -> {
                PixelDungeonHubSelectionView(
                    dungeons = DungeonRegistry.ALL_DUNGEONS,
                    playerLevel = player?.level ?: 1,
                    selectedDifficulty = selectedDifficulty,
                    onSelectDifficulty = { diff -> dungeonViewModel.setDifficulty(diff) },
                    onEnterDungeon = { dng -> dungeonViewModel.enterDungeon(dng) }
                )
            }

            is DungeonCombatState.WaveEntrance -> {
                PixelWaveEntranceView(wave = state.wave, difficulty = state.difficulty)
            }

            is DungeonCombatState.PlayerTurn -> {
                PixelCombatBattleArenaView(
                    state = state,
                    equippedPetName = equippedPet?.nickname ?: "Pet",
                    onLightAttack = {
                        soundManager.playCombatSlash()
                        dungeonViewModel.onPlayerActionLightAttack()
                    },
                    onHeavySkill = {
                        soundManager.playCombatSlash()
                        dungeonViewModel.onPlayerActionHeavySkill()
                    },
                    onPetSynergy = {
                        soundManager.playCombatSlash()
                        dungeonViewModel.onPlayerActionPetSynergy()
                    },
                    onGuard = {
                        soundManager.playMenuClick()
                        dungeonViewModel.onPlayerActionGuard()
                    },
                    onUsePotion = {
                        soundManager.playCoin()
                        dungeonViewModel.onPlayerActionUsePotion()
                    }
                )
            }

            is DungeonCombatState.ResolvingAction -> {
                PixelResolvingCombatActionView(state = state)
            }

            is DungeonCombatState.WaveCompleted -> {
                PixelWaveClearTransitionView(
                    state = state,
                    onProceedNext = { dungeonViewModel.proceedToNextWave() }
                )
            }

            is DungeonCombatState.DungeonVictory -> {
                PixelDungeonVictoryChestView(
                    state = state,
                    onExit = { dungeonViewModel.exitDungeonToHub() }
                )
            }

            is DungeonCombatState.DungeonDefeat -> {
                PixelDungeonDefeatView(
                    state = state,
                    onExit = { dungeonViewModel.exitDungeonToHub() }
                )
            }
        }
    }
}

@Composable
fun PixelDungeonHubSelectionView(
    dungeons: List<HuntingDungeon>,
    playerLevel: Int,
    selectedDifficulty: DungeonDifficulty,
    onSelectDifficulty: (DungeonDifficulty) -> Unit,
    onEnterDungeon: (HuntingDungeon) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Difficulty Level Selector
        item {
            PixelFrame(
                backgroundColor = Color(0xFF1B172A),
                borderColor = Color(0xFF4A3B69),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text("KESULITAN (DIFFICULTY):", fontFamily = PressStartFontFamily, fontSize = 8.sp, color = RetroGold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DungeonDifficulty.values().forEach { diff ->
                            val isSelected = selectedDifficulty == diff
                            PixelButton(
                                onClick = { onSelectDifficulty(diff) },
                                backgroundColor = if (isSelected) diff.composeColor() else Color(0xFF2C223E),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = diff.label.uppercase(),
                                    fontFamily = PressStartFontFamily,
                                    fontSize = 7.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Drop x${selectedDifficulty.dropRateMultiplier} • EXP x${selectedDifficulty.expMultiplier} • Stat Musuh x${selectedDifficulty.statMultiplier}",
                        fontFamily = Vt323FontFamily,
                        fontSize = 15.sp,
                        color = selectedDifficulty.composeColor()
                    )
                }
            }
        }

        // Dungeon Cards
        items(dungeons) { dungeon ->
            val isUnlocked = playerLevel >= dungeon.minPlayerLevel

            PixelFrame(
                backgroundColor = Color(0xFF201323),
                borderColor = if (isUnlocked) selectedDifficulty.composeColor() else Color(0xFF3E2723),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(dungeon.iconEmoji, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = dungeon.name.uppercase(),
                                    fontFamily = PressStartFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${dungeon.waves.size} Waves • -${dungeon.staminaCost} Stamina",
                                    fontFamily = Vt323FontFamily,
                                    fontSize = 14.sp,
                                    color = RetroGold
                                )
                            }
                        }

                        PixelBadge(
                            text = "Lv.${dungeon.minPlayerLevel}",
                            backgroundColor = if (isUnlocked) Color(0xFF43A047) else Color(0xFFB71C1C)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = dungeon.themeDescription,
                        fontFamily = Vt323FontFamily,
                        fontSize = 14.sp,
                        color = Color(0xFFB0BEC5)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    PixelButton(
                        onClick = { onEnterDungeon(dungeon) },
                        enabled = isUnlocked,
                        backgroundColor = selectedDifficulty.composeColor(),
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "enter_dungeon_btn_${dungeon.id}"
                    ) {
                        Text(
                            text = if (isUnlocked) "MASUK DUNGEON ⚔️" else "TERKUNCI (Lv.${dungeon.minPlayerLevel})",
                            fontFamily = PressStartFontFamily,
                            fontSize = 8.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PixelWaveEntranceView(
    wave: DungeonWave,
    difficulty: DungeonDifficulty
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0C1B))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            GlowingPulseBox {
                PixelSprite(spriteKey = wave.enemy.id, size = 96.dp, animated = true)
            }
            Spacer(modifier = Modifier.height(14.dp))
            PixelBadge(
                text = "${wave.enemy.rank.label.uppercase()} [${difficulty.label.uppercase()}]",
                backgroundColor = wave.enemy.rank.composeColor()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = wave.waveName.uppercase(),
                fontFamily = PressStartFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = RetroGold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = wave.enemy.name,
                fontFamily = Vt323FontFamily,
                fontSize = 18.sp,
                color = Color.White
            )
        }
    }
}

@Composable
fun PixelCombatBattleArenaView(
    state: DungeonCombatState.PlayerTurn,
    equippedPetName: String,
    onLightAttack: () -> Unit,
    onHeavySkill: () -> Unit,
    onPetSynergy: () -> Unit,
    onGuard: () -> Unit,
    onUsePotion: () -> Unit
) {
    val enemy = state.wave.enemy
    val enemyStatus = state.enemyStatus
    val playerStatus = state.playerStatus

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Floor & Wave Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PixelBadge(
                    text = "LANTAI ${state.wave.waveNumber}/${state.wave.totalWaves}",
                    backgroundColor = Color(0xFF3949AB)
                )
                PixelBadge(
                    text = "TURN ${state.turnCount}",
                    backgroundColor = state.difficulty.composeColor()
                )
            }
        }

        // Enemy Pixel Arena Card
        item {
            PixelFrame(
                backgroundColor = Color(0xFF28101C),
                borderColor = enemy.rank.composeColor(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PixelBadge(text = enemy.rank.label.uppercase(), backgroundColor = enemy.rank.composeColor())
                        Text(
                            text = "ATK:${(enemy.attack * state.difficulty.statMultiplier).toInt()} DEF:${(enemy.defense * state.difficulty.statMultiplier).toInt()}",
                            fontFamily = PressStartFontFamily,
                            fontSize = 6.sp,
                            color = Color(0xFFFFCDD2)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Animated Enemy Pixel Sprite!
                    PixelSprite(
                        spriteKey = enemy.id,
                        size = 80.dp,
                        animated = true
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = enemy.name.uppercase(),
                        fontFamily = PressStartFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Enemy Stepped HP Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("HP MUSUH", fontFamily = PressStartFontFamily, fontSize = 6.sp, color = Color(0xFFFF8A80))
                        Text("${enemyStatus.currentHp}/${enemyStatus.maxHp}", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    PixelStepProgressBar(
                        current = enemyStatus.currentHp,
                        max = enemyStatus.maxHp,
                        barColor = Color(0xFFE53935),
                        height = 10.dp,
                        showLabel = false
                    )
                }
            }
        }

        // Player Battle HUD
        item {
            PixelFrame(
                backgroundColor = Color(0xFF1B172A),
                borderColor = Color(0xFF4A3B69),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("HP PEMAIN", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color(0xFF81C784))
                            if (playerStatus.shieldAmount > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                PixelBadge(text = "+${playerStatus.shieldAmount} SHIELD", backgroundColor = Color(0xFF0288D1))
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        PixelStepProgressBar(
                            current = playerStatus.currentHp,
                            max = playerStatus.maxHp,
                            barColor = Color(0xFF43A047),
                            height = 8.dp,
                            showLabel = false
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "${playerStatus.currentHp}/${playerStatus.maxHp}",
                        fontFamily = PressStartFontFamily,
                        fontSize = 8.sp,
                        color = Color.White
                    )
                }
            }
        }

        // Action Command Pixel Buttons
        item {
            Text("PERINTAH TEMPUR:", fontFamily = PressStartFontFamily, fontSize = 8.sp, color = RetroGold)
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PixelButton(
                        onClick = onLightAttack,
                        backgroundColor = Color(0xFFC62828),
                        modifier = Modifier.weight(1f),
                        testTag = "action_light_attack"
                    ) {
                        Text("🗡️ SERANG", fontFamily = PressStartFontFamily, fontSize = 8.sp, color = Color.White)
                    }

                    PixelButton(
                        onClick = onHeavySkill,
                        backgroundColor = Color(0xFFAD1457),
                        modifier = Modifier.weight(1f),
                        testTag = "action_heavy_skill"
                    ) {
                        Text("⚡ BADAI (-2)", fontFamily = PressStartFontFamily, fontSize = 8.sp, color = Color.White)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PixelButton(
                        onClick = onPetSynergy,
                        backgroundColor = Color(0xFF6A1B9A),
                        modifier = Modifier.weight(1f),
                        testTag = "action_pet_synergy"
                    ) {
                        Text("🐾 $equippedPetName (-3)", fontFamily = PressStartFontFamily, fontSize = 8.sp, color = Color.White)
                    }

                    PixelButton(
                        onClick = onGuard,
                        backgroundColor = Color(0xFF00695C),
                        modifier = Modifier.weight(1f),
                        testTag = "action_guard"
                    ) {
                        Text("🛡️ BERTAHAN", fontFamily = PressStartFontFamily, fontSize = 8.sp, color = Color.White)
                    }
                }

                PixelButton(
                    onClick = onUsePotion,
                    backgroundColor = Color(0xFF0277BD),
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "action_use_potion"
                ) {
                    Text("🧪 RAMUAN HP (+40% DARAH)", fontFamily = PressStartFontFamily, fontSize = 8.sp, color = Color.White)
                }
            }
        }

        // Battle Logs
        item {
            PixelFrame(
                backgroundColor = Color(0xFF13101E),
                borderColor = Color(0xFF2C223E),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text("LOG TEMPUR:", fontFamily = PressStartFontFamily, fontSize = 6.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(2.dp))
                    state.logs.takeLast(3).forEach { log ->
                        Text(log, fontFamily = Vt323FontFamily, fontSize = 14.sp, color = Color(0xFFB0BEC5))
                    }
                }
            }
        }
    }
}

@Composable
fun PixelResolvingCombatActionView(
    state: DungeonCombatState.ResolvingAction
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF120E1E))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            GlowingPulseBox {
                Text(if (state.isTargetEnemy) "💥⚔️" else "🛡️✨", fontSize = 54.sp)
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = state.actionDescription,
                fontFamily = Vt323FontFamily,
                fontSize = 20.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (state.damageValue > 0) {
                PixelBadge(
                    text = if (state.isTargetEnemy) "-${state.damageValue} DMG" else "+${state.damageValue} HP",
                    backgroundColor = if (state.isCritical) Color(0xFFFF1744) else Color(0xFFFF8F00)
                )
            }
        }
    }
}

@Composable
fun PixelWaveClearTransitionView(
    state: DungeonCombatState.WaveCompleted,
    onProceedNext: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1B12))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        PixelFrame(
            backgroundColor = Color(0xFF192A1D),
            borderColor = Color(0xFF4CAF50),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("LANTAI CLEARED! ✓", fontFamily = PressStartFontFamily, fontSize = 11.sp, color = Color(0xFF81C784))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Hadiah: +${state.interimGoldGained}G • +${state.interimExpGained}EXP", fontFamily = Vt323FontFamily, fontSize = 16.sp, color = RetroGold)
                Spacer(modifier = Modifier.height(16.dp))
                PixelButton(
                    onClick = onProceedNext,
                    backgroundColor = Color(0xFF2E7D32),
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "proceed_next_wave_btn"
                ) {
                    Text("LANJUT KE LANTAI ${state.nextWaveNumber}/${state.totalWaves} ➡️", fontFamily = PressStartFontFamily, fontSize = 8.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun PixelDungeonVictoryChestView(
    state: DungeonCombatState.DungeonVictory,
    onExit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF140F24))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        PixelFrame(
            backgroundColor = Color(0xFF1E1736),
            borderColor = RetroGold,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🏆 DUNGEON CLEAR! 🏆", fontFamily = PressStartFontFamily, fontSize = 12.sp, color = RetroGold)
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(3) { idx ->
                        Text(if (idx < state.starRating) "⭐" else "☆", fontSize = 24.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${state.dungeon.name} [${state.difficulty.label}]",
                    fontFamily = PressStartFontFamily,
                    fontSize = 9.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "TOTAL: +${state.totalGoldEarned}G • +${state.totalExpEarned}EXP (${state.totalTurns} Turns)",
                    fontFamily = Vt323FontFamily,
                    fontSize = 16.sp,
                    color = RetroGold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Drops List
                if (state.dropsAcquired.isNotEmpty()) {
                    Text("ITEM JARAHAN:", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color(0xFFFFE082))
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyColumn(modifier = Modifier.height(100.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(state.dropsAcquired) { drop ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF282046))
                                    .padding(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(drop.iconEmoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(drop.name, fontFamily = Vt323FontFamily, fontSize = 14.sp, color = Color.White, modifier = Modifier.weight(1f))
                                RarityChip(rarity = drop.rarity)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("x${drop.count}", fontFamily = PressStartFontFamily, fontSize = 8.sp, color = RetroGold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                PixelButton(
                    onClick = onExit,
                    backgroundColor = Color(0xFF43A047),
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "claim_dungeon_victory_btn"
                ) {
                    Text("AMBIL HADIAH & KELUAR", fontFamily = PressStartFontFamily, fontSize = 8.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun PixelDungeonDefeatView(
    state: DungeonCombatState.DungeonDefeat,
    onExit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF200C12))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        PixelFrame(
            backgroundColor = Color(0xFF2D121B),
            borderColor = Color(0xFFB71C1C),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("☠️ KEKALAHAN ☠️", fontFamily = PressStartFontFamily, fontSize = 12.sp, color = Color(0xFFFF5252))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Gugur di Lantai ${state.waveFailed}", fontFamily = Vt323FontFamily, fontSize = 18.sp, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text(state.defeatReason, fontFamily = Vt323FontFamily, fontSize = 14.sp, color = Color(0xFFFFCDD2), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(14.dp))
                PixelButton(
                    onClick = onExit,
                    backgroundColor = Color(0xFFB71C1C),
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "exit_dungeon_defeat_btn"
                ) {
                    Text("KEMBALI KE DESA", fontFamily = PressStartFontFamily, fontSize = 8.sp, color = Color.White)
                }
            }
        }
    }
}
