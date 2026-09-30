package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.FarmPlotEntity
import com.example.data.local.entity.InventoryItemEntity
import com.example.data.model.GameDatabaseRegistry
import com.example.ui.components.PixelBadge
import com.example.ui.components.PixelButton
import com.example.ui.components.PixelFrame
import com.example.ui.components.PixelStepProgressBar
import com.example.ui.components.RetroGold
import com.example.ui.theme.PressStartFontFamily
import com.example.ui.theme.Vt323FontFamily
import com.example.ui.viewmodel.GameViewModel
import com.example.util.LocalSoundManager
import kotlinx.coroutines.delay
import kotlin.math.max

@Composable
fun FarmingScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToMarket: () -> Unit,
    modifier: Modifier = Modifier
) {
    val soundManager = LocalSoundManager.current
    val plots by viewModel.farmPlots.collectAsStateWithLifecycle()
    val inventory by viewModel.inventory.collectAsStateWithLifecycle()

    var activePlotForPlanting by remember { mutableStateOf<FarmPlotEntity?>(null) }
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTimeMillis = System.currentTimeMillis()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F0C1B))
            .testTag("farming_screen_root")
    ) {
        // Pixel Farming Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D0B14))
                .padding(bottom = 2.dp)
                .background(Color(0xFF1B5E20))
                .padding(bottom = 2.dp)
                .background(Color(0xFF142E18))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        soundManager.playMenuClick()
                        onNavigateBack()
                    },
                    modifier = Modifier.testTag("farming_back_btn")
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "KEBUN TANI [PIXEL]",
                        fontFamily = PressStartFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = RetroGold
                    )
                    Text(
                        text = "Tanam benih, rawat tanah & siram air secara teratur",
                        fontFamily = Vt323FontFamily,
                        fontSize = 14.sp,
                        color = Color(0xFFA5D6A7)
                    )
                }
                PixelButton(
                    onClick = {
                        soundManager.playMenuClick()
                        onNavigateToMarket()
                    },
                    backgroundColor = Color(0xFF2E7D32),
                    testTag = "buy_seeds_shortcut"
                ) {
                    Text("+ BENIH", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color.White)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 1000.dp)
            ) {
                // Info Banner
                PixelFrame(
                    backgroundColor = Color(0xFF142419),
                    borderColor = Color(0xFF2E7D32),
                    contentPadding = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "TIPS TANAH: Siram air mempercepat panen 20%. Beri pupuk melipatgandakan panen 2x!",
                        fontFamily = Vt323FontFamily,
                        fontSize = 15.sp,
                        color = Color(0xFFC8E6C9)
                    )
                }

                // 8 Plots Grid (Adaptive: 2 cols on phones, 3-4 cols on tablets/landscape)
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 145.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
            items(plots) { plot ->
                PixelPlotCard(
                    plot = plot,
                    currentTime = currentTimeMillis,
                    onUnlock = {
                        soundManager.playCoin()
                        val cost = 250 + (plot.plotIndex * 150)
                        viewModel.unlockPlot(plot.plotIndex, cost)
                    },
                    onPlantClick = {
                        soundManager.playMenuClick()
                        activePlotForPlanting = plot
                    },
                    onWater = {
                        soundManager.playFishCast()
                        viewModel.waterPlot(plot.plotIndex)
                    },
                    onFertilize = {
                        soundManager.playMenuClick()
                        viewModel.fertilizePlot(plot.plotIndex)
                    },
                    onHarvest = {
                        soundManager.playCoin()
                        viewModel.harvestPlot(plot.plotIndex)
                    }
                )
            }
        }
    }
}

    if (activePlotForPlanting != null) {
        val seedItems = inventory.filter { it.itemType == "SEED" }
        PixelPlantSeedDialog(
            seedItems = seedItems,
            onSelectCrop = { cropId ->
                soundManager.playMenuClick()
                activePlotForPlanting?.let { plot -> viewModel.plantSeed(plot.plotIndex, cropId) }
                activePlotForPlanting = null
            },
            onDismiss = {
                soundManager.playMenuClick()
                activePlotForPlanting = null
            },
            onGoToShop = {
                soundManager.playMenuClick()
                activePlotForPlanting = null
                onNavigateToMarket()
            }
        )
    }
}
}

@Composable
fun PixelPlotCard(
    plot: FarmPlotEntity,
    currentTime: Long,
    onUnlock: () -> Unit,
    onPlantClick: () -> Unit,
    onWater: () -> Unit,
    onFertilize: () -> Unit,
    onHarvest: () -> Unit
) {
    val cropDef = plot.cropId?.let { cid -> GameDatabaseRegistry.CROPS.find { it.id == cid || it.id == cid.removePrefix("crop_") } }
    val unlockCost = 250 + (plot.plotIndex * 150)

    val isPlanted = cropDef != null
    val isReadyToHarvest = isPlanted && currentTime >= plot.harvestAtMillis
    val remainingSeconds = if (isPlanted && !isReadyToHarvest) {
        max(0L, (plot.harvestAtMillis - currentTime) / 1000L)
    } else 0L

    val soilColor = when {
        !plot.isUnlocked -> Color(0xFF1E1C24)
        plot.isWatered -> Color(0xFF2C1E18) // Wet dark soil
        else -> Color(0xFF3E2C22) // Dry brown soil
    }

    PixelFrame(
        backgroundColor = soilColor,
        borderColor = if (plot.isFertilized) RetroGold else Color(0xFF5D4037),
        contentPadding = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("farm_plot_${plot.plotIndex}")
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
                Text(
                    text = "PETAK #${plot.plotIndex + 1}",
                    fontFamily = PressStartFontFamily,
                    fontSize = 7.sp,
                    color = Color(0xFFD7CCC8)
                )

                if (plot.isUnlocked && isPlanted) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        if (plot.isWatered) PixelBadge(text = "💧", backgroundColor = Color(0xFF0288D1))
                        if (plot.isFertilized) PixelBadge(text = "✨", backgroundColor = Color(0xFFFF8F00))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (!plot.isUnlocked) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 10.dp)) {
                    Icon(Icons.Default.Lock, contentDescription = "Terkunci", tint = Color.Gray, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("TERKUNCI", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(6.dp))
                    PixelButton(onClick = onUnlock, backgroundColor = Color(0xFFE65100), testTag = "unlock_plot_${plot.plotIndex}") {
                        Text("$unlockCost G", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color.White)
                    }
                }
            } else if (!isPlanted) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onPlantClick() }
                        .padding(vertical = 12.dp)
                        .testTag("plant_empty_plot_${plot.plotIndex}")
                ) {
                    Text("🌱", fontSize = 28.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("TANAH KOSONG", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color(0xFFCFD8DC))
                    Text("Ketuk untuk tanam", fontFamily = Vt323FontFamily, fontSize = 13.sp, color = Color(0xFF81C784))
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val plantVisual = when {
                        isReadyToHarvest -> cropDef?.iconEmoji ?: "✨"
                        remainingSeconds > (cropDef?.growDurationSeconds ?: 30L) / 2 -> "🌱"
                        else -> "🌿"
                    }

                    Text(plantVisual, fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(cropDef?.name?.uppercase() ?: "TANAMAN", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color.White)

                    if (isReadyToHarvest) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("SIAP PANEN!", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = RetroGold)
                        Spacer(modifier = Modifier.height(6.dp))
                        PixelButton(onClick = onHarvest, backgroundColor = Color(0xFF43A047), modifier = Modifier.fillMaxWidth(), testTag = "harvest_plot_${plot.plotIndex}") {
                            Text("PANEN 🧺", fontFamily = PressStartFontFamily, fontSize = 8.sp, color = Color.White)
                        }
                    } else {
                        val totalDuration = (cropDef?.growDurationSeconds ?: 30L) * 1000L
                        val elapsed = max(0L, totalDuration - (remainingSeconds * 1000L))
                        val progress = (elapsed.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)

                        Text("${remainingSeconds}s lagi", fontFamily = Vt323FontFamily, fontSize = 14.sp, color = Color(0xFFFFF59D))
                        Spacer(modifier = Modifier.height(2.dp))
                        PixelStepProgressBar(current = elapsed.toInt(), max = totalDuration.toInt(), barColor = Color(0xFF81C784), height = 6.dp, showLabel = false)

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            PixelButton(
                                onClick = onWater,
                                enabled = !plot.isWatered,
                                backgroundColor = Color(0xFF0288D1),
                                modifier = Modifier.weight(1f),
                                testTag = "water_plot_${plot.plotIndex}"
                            ) {
                                Text("💧", fontSize = 10.sp)
                            }
                            PixelButton(
                                onClick = onFertilize,
                                enabled = !plot.isFertilized,
                                backgroundColor = Color(0xFFF57C00),
                                modifier = Modifier.weight(1f),
                                testTag = "fertilize_plot_${plot.plotIndex}"
                            ) {
                                Text("✨", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PixelPlantSeedDialog(
    seedItems: List<InventoryItemEntity>,
    onSelectCrop: (String) -> Unit,
    onDismiss: () -> Unit,
    onGoToShop: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        PixelFrame(
            backgroundColor = Color(0xFF141E17),
            borderColor = Color(0xFF4CAF50),
            modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth().testTag("plant_seed_dialog")
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🌱 PILIH BENIH", fontFamily = PressStartFontFamily, fontSize = 9.sp, color = RetroGold)
                    PixelButton(onClick = onDismiss, backgroundColor = Color(0xFF424242)) {
                        Text("X", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (seedItems.isEmpty()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 14.dp)) {
                        Text("Belum ada benih di tas!", fontFamily = Vt323FontFamily, fontSize = 16.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        PixelButton(onClick = onGoToShop, backgroundColor = Color(0xFF2E7D32)) {
                            Text("BELI DI PASAR", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color.White)
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 280.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(seedItems) { seedItem ->
                            val cropId = seedItem.id.removePrefix("crop_seed_")
                            val cropDef = GameDatabaseRegistry.CROPS.find { it.id == cropId || it.id == cropId.removePrefix("crop_") }

                            PixelFrame(
                                backgroundColor = Color(0xFF1D2E22),
                                borderColor = Color(0xFF2E7D32),
                                contentPadding = 6.dp,
                                modifier = Modifier.fillMaxWidth().clickable { onSelectCrop(cropId) }
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text(seedItem.iconEmoji, fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(seedItem.name, fontFamily = PressStartFontFamily, fontSize = 8.sp, color = Color.White)
                                        Text("Waktu: ${cropDef?.growDurationSeconds ?: 30}s • EXP +${cropDef?.expReward ?: 10}", fontFamily = Vt323FontFamily, fontSize = 14.sp, color = Color(0xFFA5D6A7))
                                    }
                                    PixelBadge(text = "x${seedItem.count}", backgroundColor = Color(0xFF1B5E20))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
