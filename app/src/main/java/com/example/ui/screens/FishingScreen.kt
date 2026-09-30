package com.example.ui.screens

import android.view.MotionEvent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.FishDexEntity
import com.example.data.model.FishDef
import com.example.data.model.FishingRodTier
import com.example.data.model.GameDatabaseRegistry
import com.example.ui.components.GlowingPulseBox
import com.example.ui.components.PixelBadge
import com.example.ui.components.PixelButton
import com.example.ui.components.PixelFrame
import com.example.ui.components.PixelStepProgressBar
import com.example.ui.components.RarityChip
import com.example.ui.components.RetroGold
import com.example.ui.theme.PressStartFontFamily
import com.example.ui.theme.Vt323FontFamily
import com.example.ui.viewmodel.FishingState
import com.example.ui.viewmodel.GameViewModel
import com.example.util.LocalSoundManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishingScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val soundManager = LocalSoundManager.current
    val player by viewModel.playerProfile.collectAsStateWithLifecycle()
    val fishingState by viewModel.fishingState.collectAsStateWithLifecycle()
    val fishDexList by viewModel.fishDex.collectAsStateWithLifecycle()

    LaunchedEffect(fishingState) {
        when (fishingState) {
            is FishingState.Casting -> soundManager.playFishCast()
            is FishingState.StrikeAlert -> soundManager.playFishBite()
            is FishingState.Caught -> soundManager.playFishCatch()
            is FishingState.Escaped -> soundManager.playCombatHit()
            else -> {}
        }
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedBaitId by remember { mutableStateOf("bait_cacing") }
    var showUpgradeDialog by remember { mutableStateOf(false) }

    val currentRodLevel = player?.fishingRodLevel ?: 1
    val currentRodTier = GameDatabaseRegistry.ROD_TIERS.getOrElse(currentRodLevel - 1) { GameDatabaseRegistry.ROD_TIERS[0] }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F0C1B))
            .testTag("fishing_screen_root")
    ) {
        // Fishing Pixel Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D0B14))
                .padding(bottom = 2.dp)
                .background(Color(0xFF006064))
                .padding(bottom = 2.dp)
                .background(Color(0xFF0E222A))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("fishing_back_btn")) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DANAU KRISTAL [MANCING]",
                        fontFamily = PressStartFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = RetroGold
                    )
                    Text(
                        text = "Tangkap ikan langka & taklukkan monster air",
                        fontFamily = Vt323FontFamily,
                        fontSize = 14.sp,
                        color = Color(0xFF80DEEA)
                    )
                }
            }
        }

        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color(0xFF132028),
            contentColor = RetroGold
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("🎣 MANCING", fontFamily = PressStartFontFamily, fontSize = 8.sp) }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = {
                    val countDiscovered = fishDexList.count { it.isDiscovered }
                    Text("📖 BUKU IKAN ($countDiscovered/14)", fontFamily = PressStartFontFamily, fontSize = 8.sp)
                }
            )
        }

        if (selectedTabIndex == 0) {
            PixelFishingPlaygroundView(
                viewModel = viewModel,
                fishingState = fishingState,
                currentRodTier = currentRodTier,
                selectedBaitId = selectedBaitId,
                onSelectBait = { selectedBaitId = it },
                onOpenUpgradeRod = { showUpgradeDialog = true }
            )
        } else {
            PixelFishDexCatalogView(fishDexList = fishDexList)
        }
    }

    if (fishingState is FishingState.Caught) {
        val caught = fishingState as FishingState.Caught
        PixelFishCaughtDialog(
            caught = caught,
            onDismiss = { viewModel.resetFishing() }
        )
    }

    if (showUpgradeDialog) {
        PixelUpgradeRodDialog(
            currentTier = currentRodTier,
            playerGold = player?.gold ?: 0,
            playerStones = player?.upgradeStones ?: 0,
            onUpgrade = {
                viewModel.upgradeFishingRod()
                showUpgradeDialog = false
            },
            onDismiss = { showUpgradeDialog = false }
        )
    }
}

@Composable
fun PixelFishingPlaygroundView(
    viewModel: GameViewModel,
    fishingState: FishingState,
    currentRodTier: FishingRodTier,
    selectedBaitId: String,
    onSelectBait: (String) -> Unit,
    onOpenUpgradeRod: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Lake Visual Canvas
        item {
            PixelFishingLakeVisual(fishingState = fishingState)
        }

        // Rod Info Banner
        item {
            PixelFrame(
                backgroundColor = Color(0xFF132028),
                borderColor = Color(0xFF00838F),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🎣", fontSize = 26.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(currentRodTier.name.uppercase(), fontFamily = PressStartFontFamily, fontSize = 9.sp, color = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            PixelBadge(text = "T${currentRodTier.level}", backgroundColor = Color(0xFF00695C))
                        }
                        Text(
                            text = "Speed ${(currentRodTier.catchSpeedMultiplier * 100).toInt()}% • Rare +${currentRodTier.rareBonusPercent}%",
                            fontFamily = Vt323FontFamily,
                            fontSize = 14.sp,
                            color = Color(0xFFB2EBF2)
                        )
                    }
                    if (currentRodTier.level < GameDatabaseRegistry.ROD_TIERS.size) {
                        PixelButton(
                            onClick = onOpenUpgradeRod,
                            backgroundColor = Color(0xFF00695C),
                            testTag = "upgrade_rod_btn"
                        ) {
                            Text("UPGRADE", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // Bait Selector
        item {
            Column {
                Text("PILIH UMPAN:", fontFamily = PressStartFontFamily, fontSize = 8.sp, color = RetroGold)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(GameDatabaseRegistry.BAITS) { bait ->
                        val isSelected = selectedBaitId == bait.id
                        PixelButton(
                            onClick = { onSelectBait(bait.id) },
                            backgroundColor = if (isSelected) Color(0xFF00695C) else Color(0xFF263238)
                        ) {
                            Text("${bait.iconEmoji} ${bait.name}", fontFamily = Vt323FontFamily, fontSize = 14.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // Action Controls by State
        item {
            when (fishingState) {
                is FishingState.Idle -> {
                    PixelButton(
                        onClick = { viewModel.startFishing(selectedBaitId) },
                        backgroundColor = Color(0xFF00897B),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        testTag = "cast_rod_button"
                    ) {
                        Text("🎣 LEMPAR KAIL (-2 STAMINA)", fontFamily = PressStartFontFamily, fontSize = 9.sp, color = Color.White)
                    }
                }

                is FishingState.Casting -> {
                    PixelFrame(backgroundColor = Color(0xFF102830), borderColor = Color(0xFF00ACC1), modifier = Modifier.fillMaxWidth()) {
                        Text("MELEMPARKAN KAIL...", fontFamily = PressStartFontFamily, fontSize = 9.sp, color = Color.White, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }

                is FishingState.WaitingBite -> {
                    PixelFrame(backgroundColor = Color(0xFF0A1F26), borderColor = Color(0xFF00E5FF), modifier = Modifier.fillMaxWidth()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text("MENUNGGU IKAN MENYAMBAR...", fontFamily = PressStartFontFamily, fontSize = 8.sp, color = Color(0xFF80DEEA))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Bersiaplah menekan tombol STRIKE!", fontFamily = Vt323FontFamily, fontSize = 14.sp, color = Color.White)
                        }
                    }
                }

                is FishingState.StrikeAlert -> {
                    GlowingPulseBox(modifier = Modifier.fillMaxWidth()) {
                        PixelButton(
                            onClick = { viewModel.onStrikeTap() },
                            backgroundColor = Color(0xFFFF1744),
                            modifier = Modifier.fillMaxWidth().height(58.dp),
                            testTag = "strike_button"
                        ) {
                            Text("⚡ STRIKE! TARIK SEKARANG! ⚡", fontFamily = PressStartFontFamily, fontSize = 10.sp, color = Color.White)
                        }
                    }
                }

                is FishingState.Reeling -> {
                    PixelReelingMiniGameCard(
                        reelingState = fishingState,
                        onHoldReel = { isHolding -> viewModel.setReelButtonHolding(isHolding) }
                    )
                }

                is FishingState.Escaped -> {
                    PixelFrame(backgroundColor = Color(0xFF261214), borderColor = Color(0xFFD32F2F), modifier = Modifier.fillMaxWidth()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text("IKAN TERLEPAS!", fontFamily = PressStartFontFamily, fontSize = 9.sp, color = Color(0xFFFF8A80))
                            Text(fishingState.reason, fontFamily = Vt323FontFamily, fontSize = 14.sp, color = Color(0xFFFFCDD2))
                            Spacer(modifier = Modifier.height(6.dp))
                            PixelButton(onClick = { viewModel.resetFishing() }, backgroundColor = Color(0xFF5D4037)) {
                                Text("COBA LAGI", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color.White)
                            }
                        }
                    }
                }

                is FishingState.Caught -> {}
            }
        }
    }
}

@Composable
fun PixelFishingLakeVisual(fishingState: FishingState) {
    val infiniteTransition = rememberInfiniteTransition(label = "water_ripple")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave"
    )

    PixelFrame(
        backgroundColor = Color(0xFF091C24),
        borderColor = Color(0xFF006064),
        contentPadding = 8.dp,
        modifier = Modifier.fillMaxWidth().height(160.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                drawRect(
                    color = Color(0x1800E5FF),
                    topLeft = Offset(width * 0.3f - waveOffset, height * 0.4f),
                    size = androidx.compose.ui.geometry.Size(width * 0.4f + (waveOffset * 2), 6f)
                )
                drawRect(
                    color = Color(0x1200E5FF),
                    topLeft = Offset(width * 0.2f - waveOffset, height * 0.6f),
                    size = androidx.compose.ui.geometry.Size(width * 0.6f + (waveOffset * 2), 6f)
                )
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (fishingState) {
                    is FishingState.Idle -> {
                        Text("🎣", fontSize = 36.sp)
                        Text("Danau Tenang...", fontFamily = Vt323FontFamily, fontSize = 16.sp, color = Color(0xFFB0BEC5))
                    }
                    is FishingState.Casting -> {
                        Text("🌊", fontSize = 36.sp)
                        Text("Kail meluncur ke air...", fontFamily = Vt323FontFamily, fontSize = 16.sp, color = Color(0xFF80DEEA))
                    }
                    is FishingState.WaitingBite -> {
                        Text("🔴", fontSize = 32.sp)
                        Text("Pelampung bergoyang...", fontFamily = Vt323FontFamily, fontSize = 16.sp, color = RetroGold)
                    }
                    is FishingState.StrikeAlert -> {
                        Text("⚠️", fontSize = 36.sp)
                        Text("IKAN MENYAMBAR!", fontFamily = PressStartFontFamily, fontSize = 10.sp, color = Color(0xFFFF5252))
                    }
                    is FishingState.Reeling -> {
                        Text("🐟", fontSize = 36.sp)
                        Text("Pertahankan tegangan tali!", fontFamily = Vt323FontFamily, fontSize = 16.sp, color = Color(0xFF69F0AE))
                    }
                    is FishingState.Caught -> {
                        Text("✨🏆✨", fontSize = 36.sp)
                        Text("BERHASIL DITANGKAP!", fontFamily = PressStartFontFamily, fontSize = 10.sp, color = RetroGold)
                    }
                    is FishingState.Escaped -> {
                        Text("💨", fontSize = 36.sp)
                        Text("Ikan terlepas...", fontFamily = Vt323FontFamily, fontSize = 16.sp, color = Color(0xFFFFAB91))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PixelReelingMiniGameCard(
    reelingState: FishingState.Reeling,
    onHoldReel: (Boolean) -> Unit
) {
    PixelFrame(
        backgroundColor = Color(0xFF102128),
        borderColor = Color(0xFF00ACC1),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("TARIK KAIL (REELING)", fontFamily = PressStartFontFamily, fontSize = 8.sp, color = RetroGold)
                RarityChip(rarity = reelingState.fish.rarity)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text("PROGRESS: ${reelingState.catchProgress.toInt()}%", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color.White)
            Spacer(modifier = Modifier.height(2.dp))
            PixelStepProgressBar(current = reelingState.catchProgress.toInt(), max = 100, barColor = Color(0xFF00E676), height = 10.dp, showLabel = false)

            Spacer(modifier = Modifier.height(8.dp))

            Text("TEGANGAN TALI:", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color.White)
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .background(Color(0xFF000000), RectangleShape)
                    .padding(2.dp)
            ) {
                val sweetStart = reelingState.targetZoneMin / 100f
                val sweetWidth = (reelingState.targetZoneMax - reelingState.targetZoneMin) / 100f

                Box(
                    modifier = Modifier
                        .fillMaxWidth(sweetWidth.coerceIn(0.1f, 0.6f))
                        .height(16.dp)
                        .background(Color(0xFF4CAF50).copy(alpha = 0.5f))
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth((reelingState.tension / 100f).coerceIn(0f, 1f))
                        .height(16.dp)
                        .background(
                            if (reelingState.tension in reelingState.targetZoneMin..reelingState.targetZoneMax) Color(0xFF00E676) else Color(0xFFFF5252)
                        )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            PixelButton(
                onClick = {},
                backgroundColor = Color(0xFF0277BD),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .pointerInteropFilter { event ->
                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                onHoldReel(true)
                                true
                            }
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                onHoldReel(false)
                                true
                            }
                            else -> false
                        }
                    }
                    .testTag("reel_hold_button")
            ) {
                Text("TAHAN UNTUK MENARIK", fontFamily = PressStartFontFamily, fontSize = 8.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun PixelFishCaughtDialog(
    caught: FishingState.Caught,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        PixelFrame(
            backgroundColor = Color(0xFF141F28),
            borderColor = RetroGold,
            modifier = Modifier.fillMaxWidth().testTag("fish_caught_dialog")
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("TANGKAPAN HEBAT! 🏆", fontFamily = PressStartFontFamily, fontSize = 11.sp, color = RetroGold)
                Spacer(modifier = Modifier.height(10.dp))
                Text(caught.fish.iconEmoji, fontSize = 48.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(caught.fish.name.uppercase(), fontFamily = PressStartFontFamily, fontSize = 10.sp, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                RarityChip(rarity = caught.fish.rarity)
                Spacer(modifier = Modifier.height(8.dp))
                Text("BERAT: ${String.format("%.1f", caught.weightKg)} KG • NILAI: +${caught.rewardGold}G • +${caught.expReward}EXP", fontFamily = Vt323FontFamily, fontSize = 16.sp, color = RetroGold)
                Spacer(modifier = Modifier.height(6.dp))
                Text("\"${caught.fish.lore}\"", fontFamily = Vt323FontFamily, fontSize = 14.sp, color = Color(0xFFB0BEC5), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(12.dp))
                PixelButton(onClick = onDismiss, backgroundColor = Color(0xFF2E7D32), modifier = Modifier.fillMaxWidth(), testTag = "collect_fish_button") {
                    Text("SIMPAN KE TAS & DEX", fontFamily = PressStartFontFamily, fontSize = 8.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun PixelUpgradeRodDialog(
    currentTier: FishingRodTier,
    playerGold: Int,
    playerStones: Int,
    onUpgrade: () -> Unit,
    onDismiss: () -> Unit
) {
    val nextTier = GameDatabaseRegistry.ROD_TIERS.getOrNull(currentTier.level)

    Dialog(onDismissRequest = onDismiss) {
        PixelFrame(backgroundColor = Color(0xFF19242E), borderColor = RetroGold, modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("PANDAI BESI PANCING", fontFamily = PressStartFontFamily, fontSize = 10.sp, color = RetroGold)
                Spacer(modifier = Modifier.height(8.dp))
                if (nextTier != null) {
                    Text("Tingkatkan ke: ${nextTier.name}", fontFamily = Vt323FontFamily, fontSize = 16.sp, color = Color.White)
                    Text("Biaya: ${nextTier.upgradeCostGold} Gold & ${nextTier.upgradeCostStones} Batu Tempa", fontFamily = Vt323FontFamily, fontSize = 15.sp, color = Color(0xFFFFD54F))
                    Spacer(modifier = Modifier.height(10.dp))
                    PixelButton(
                        onClick = onUpgrade,
                        enabled = playerGold >= nextTier.upgradeCostGold && playerStones >= nextTier.upgradeCostStones,
                        backgroundColor = Color(0xFFE65100),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("UPGRADE SEKARANG", fontFamily = PressStartFontFamily, fontSize = 8.sp, color = Color.White)
                    }
                } else {
                    Text("Pancing sudah level maksimum!", fontFamily = Vt323FontFamily, fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun PixelFishDexCatalogView(fishDexList: List<FishDexEntity>) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(GameDatabaseRegistry.FISHES) { fishDef ->
            val entry = fishDexList.find { it.fishId == fishDef.id }
            val isDiscovered = entry?.isDiscovered == true

            PixelFrame(
                backgroundColor = if (isDiscovered) Color(0xFF19242E) else Color(0xFF131A1F),
                borderColor = if (isDiscovered) fishDef.rarity.composeColor() else Color(0xFF37474F),
                contentPadding = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(if (isDiscovered) fishDef.iconEmoji else "❓", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(if (isDiscovered) fishDef.name.uppercase() else "???", fontFamily = PressStartFontFamily, fontSize = 8.sp, color = Color.White)
                        if (isDiscovered && entry != null) {
                            Text("Tertangkap: ${entry.countCaught}x • Rekor: ${String.format("%.1f", entry.maxWeightKg)}kg", fontFamily = Vt323FontFamily, fontSize = 14.sp, color = Color(0xFF80DEEA))
                        } else {
                            Text("Syarat: Alat Pancing Tier ${fishDef.requiredRodLevel}+", fontFamily = Vt323FontFamily, fontSize = 13.sp, color = Color.Gray)
                        }
                    }
                    RarityChip(rarity = fishDef.rarity)
                }
            }
        }
    }
}
