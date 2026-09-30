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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.InventoryItemEntity
import com.example.data.local.entity.PetEntity
import com.example.data.model.GameDatabaseRegistry
import com.example.data.model.PetSpeciesDef
import com.example.ui.components.GlowingPulseBox
import com.example.ui.components.PixelBadge
import com.example.ui.components.PixelButton
import com.example.ui.components.PixelFrame
import com.example.ui.components.PixelSprite
import com.example.ui.components.PixelStepProgressBar
import com.example.ui.components.RetroGold
import com.example.ui.theme.PressStartFontFamily
import com.example.ui.theme.Vt323FontFamily
import com.example.ui.viewmodel.GameViewModel

@Composable
fun PetScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pets by viewModel.pets.collectAsStateWithLifecycle()
    val equippedPet by viewModel.equippedPet.collectAsStateWithLifecycle()
    val player by viewModel.playerProfile.collectAsStateWithLifecycle()
    val inventory by viewModel.inventory.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedPetId by remember { mutableIntStateOf(equippedPet?.id ?: pets.firstOrNull()?.id ?: 0) }

    var showFeedDialog by remember { mutableStateOf(false) }
    var showAdoptDialog by remember { mutableStateOf(false) }

    val currentSelectedPet = pets.find { it.id == selectedPetId } ?: equippedPet ?: pets.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F0C1B))
            .testTag("pet_screen_root")
    ) {
        // Pixel Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D0B14))
                .padding(bottom = 2.dp)
                .background(Color(0xFF4A148C))
                .padding(bottom = 2.dp)
                .background(Color(0xFF1E1430))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("pet_back_btn")) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SUAKA PET [PIXEL]",
                        fontFamily = PressStartFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = RetroGold
                    )
                    Text(
                        text = "Latih, beri makan & evolusikan sahabat pixelmu",
                        fontFamily = Vt323FontFamily,
                        fontSize = 14.sp,
                        color = Color(0xFFCE93D8)
                    )
                }
                PixelButton(
                    onClick = { showAdoptDialog = true },
                    backgroundColor = Color(0xFF6A1B9A),
                    testTag = "adopt_pet_btn"
                ) {
                    Text("+ ADOPSI", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color.White)
                }
            }
        }

        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color(0xFF1B152A),
            contentColor = RetroGold
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("🐾 PET AKTIF", fontFamily = PressStartFontFamily, fontSize = 8.sp) }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("📋 ROSTER (${pets.size})", fontFamily = PressStartFontFamily, fontSize = 8.sp) }
            )
        }

        if (selectedTabIndex == 0) {
            if (currentSelectedPet != null) {
                ActivePetPixelDetailView(
                    pet = currentSelectedPet,
                    playerStones = player?.upgradeStones ?: 0,
                    onFeedClick = { showFeedDialog = true },
                    onTrainClick = { viewModel.trainPet(currentSelectedPet.id) },
                    onEvolveClick = { viewModel.evolvePet(currentSelectedPet.id) },
                    onEquipClick = { viewModel.equipPet(currentSelectedPet.id) }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada pet peliharaan.", fontFamily = Vt323FontFamily, fontSize = 18.sp, color = Color.Gray)
                }
            }
        } else {
            PetRosterPixelListView(
                pets = pets,
                selectedPetId = selectedPetId,
                onSelectPet = { id ->
                    selectedPetId = id
                    selectedTabIndex = 0
                }
            )
        }
    }

    if (showFeedDialog && currentSelectedPet != null) {
        val edibleItems = inventory.filter {
            it.itemType == "CROP" || it.itemType == "FISH" || it.itemType == "PET_FOOD" || it.itemType == "FOOD" || it.id.startsWith("pet_food_")
        }
        PixelFeedPetDialog(
            pet = currentSelectedPet,
            edibleItems = edibleItems,
            onFeed = { itemId ->
                viewModel.feedPet(currentSelectedPet.id, itemId)
                showFeedDialog = false
            },
            onDismiss = { showFeedDialog = false }
        )
    }

    if (showAdoptDialog) {
        PixelAdoptPetDialog(
            playerGold = player?.gold ?: 0,
            onAdopt = { speciesId, name, cost ->
                viewModel.adoptNewPet(speciesId, name, cost)
                showAdoptDialog = false
            },
            onDismiss = { showAdoptDialog = false }
        )
    }
}

@Composable
fun ActivePetPixelDetailView(
    pet: PetEntity,
    playerStones: Int,
    onFeedClick: () -> Unit,
    onTrainClick: () -> Unit,
    onEvolveClick: () -> Unit,
    onEquipClick: () -> Unit
) {
    val species = GameDatabaseRegistry.PET_SPECIES.find { it.id == pet.speciesId }

    val currentName = when (pet.evolutionStage) {
        3 -> species?.stage3Name ?: pet.nickname
        2 -> species?.stage2Name ?: pet.nickname
        else -> pet.nickname
    }

    val attackStat = ((species?.baseAttack ?: 10) + (pet.level * (species?.attackGrowth ?: 2f))).toInt()
    val defenseStat = ((species?.baseDefense ?: 10) + (pet.level * (species?.defenseGrowth ?: 2f))).toInt()

    val canEvolve = (pet.level >= 10 && pet.evolutionStage == 1) || (pet.level >= 25 && pet.evolutionStage == 2)
    val requiredStones = if (pet.evolutionStage == 1) 2 else 5

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Pixel Pet Showcase Card
        item {
            PixelFrame(
                backgroundColor = Color(0xFF231838),
                borderColor = when (pet.evolutionStage) {
                    3 -> RetroGold
                    2 -> Color(0xFFAB47BC)
                    else -> Color(0xFF42A5F5)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PixelBadge(
                        text = when (pet.evolutionStage) {
                            3 -> "EVOLUSI PUNCAK (STAGE 3)"
                            2 -> "EVOLUSI TINGKAT 2"
                            else -> "BENTUK AWAL (STAGE 1)"
                        },
                        backgroundColor = when (pet.evolutionStage) {
                            3 -> Color(0xFFFF8F00)
                            2 -> Color(0xFF8E24AA)
                            else -> Color(0xFF0288D1)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Large Animated Pixel Sprite
                    PixelSprite(
                        spriteKey = pet.speciesId,
                        size = 80.dp,
                        animated = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = currentName.uppercase(),
                        fontFamily = PressStartFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = species?.specialty ?: "Pendamping RPG",
                        fontFamily = Vt323FontFamily,
                        fontSize = 15.sp,
                        color = Color(0xFFE1BEE7)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (pet.isEquipped) {
                        PixelBadge(text = "✓ PENDAMPING AKTIF", backgroundColor = Color(0xFF2E7D32))
                    } else {
                        PixelButton(
                            onClick = onEquipClick,
                            backgroundColor = Color(0xFF00796B),
                            testTag = "equip_pet_btn"
                        ) {
                            Text("PASANG PENDAMPING", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // Stats & Progress Frame
        item {
            PixelFrame(
                backgroundColor = Color(0xFF1B172A),
                borderColor = Color(0xFF4A3B69),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("STAT TEMPUR:", fontFamily = PressStartFontFamily, fontSize = 8.sp, color = RetroGold)
                        PixelBadge(text = "Lv.${pet.level}", backgroundColor = Color(0xFF3949AB))
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text("EXP: ${pet.exp}/${pet.maxExp}", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color(0xFFB0BEC5))
                    Spacer(modifier = Modifier.height(3.dp))
                    PixelStepProgressBar(current = pet.exp, max = pet.maxExp, barColor = Color(0xFFAB47BC), height = 8.dp, showLabel = false)

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFF3E1E1E), RectangleShape)
                                .padding(6.dp)
                        ) {
                            Column {
                                Text("ATK (FISIK)", fontFamily = PressStartFontFamily, fontSize = 6.sp, color = Color(0xFFFF8A80))
                                Text("$attackStat", fontFamily = PressStartFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFF14243B), RectangleShape)
                                .padding(6.dp)
                        ) {
                            Column {
                                Text("DEF (TEMPUR)", fontFamily = PressStartFontFamily, fontSize = 6.sp, color = Color(0xFF82B1FF))
                                Text("$defenseStat", fontFamily = PressStartFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("KENYANG: ${pet.hunger}%", fontFamily = PressStartFontFamily, fontSize = 6.sp, color = Color(0xFFFFB74D))
                            Spacer(modifier = Modifier.height(2.dp))
                            PixelStepProgressBar(current = pet.hunger, max = 100, barColor = Color(0xFFFF9800), height = 6.dp, showLabel = false)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("MOOD: ${pet.happiness}%", fontFamily = PressStartFontFamily, fontSize = 6.sp, color = Color(0xFFF48FB1))
                            Spacer(modifier = Modifier.height(2.dp))
                            PixelStepProgressBar(current = pet.happiness, max = 100, barColor = Color(0xFFE91E63), height = 6.dp, showLabel = false)
                        }
                    }
                }
            }
        }

        // Action Buttons: Feed, Train, Evolve
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PixelButton(
                        onClick = onFeedClick,
                        backgroundColor = Color(0xFFE65100),
                        modifier = Modifier.weight(1f),
                        testTag = "feed_pet_button"
                    ) {
                        Text("🍗 MAKAN", fontFamily = PressStartFontFamily, fontSize = 9.sp, color = Color.White)
                    }

                    PixelButton(
                        onClick = onTrainClick,
                        backgroundColor = Color(0xFF4A148C),
                        modifier = Modifier.weight(1f),
                        testTag = "train_pet_button"
                    ) {
                        Text("⚔️ LATIH (-5⚡)", fontFamily = PressStartFontFamily, fontSize = 8.sp, color = Color.White)
                    }
                }

                if (canEvolve) {
                    GlowingPulseBox(modifier = Modifier.fillMaxWidth()) {
                        PixelButton(
                            onClick = onEvolveClick,
                            backgroundColor = Color(0xFFFF8F00),
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "evolve_pet_button"
                        ) {
                            Text("✨ EVOLUSI SEKARANG! (Butuh $requiredStones Batu) ✨", fontFamily = PressStartFontFamily, fontSize = 8.sp, color = Color.White)
                        }
                    }
                } else if (pet.evolutionStage < 3) {
                    val nextLvl = if (pet.evolutionStage == 1) 10 else 25
                    Text(
                        text = "INFO: Capai Level $nextLvl untuk membuka evolusi Stage ${pet.evolutionStage + 1}!",
                        fontFamily = Vt323FontFamily,
                        fontSize = 15.sp,
                        color = Color(0xFFB0BEC5)
                    )
                }
            }
        }
    }
}

@Composable
fun PetRosterPixelListView(
    pets: List<PetEntity>,
    selectedPetId: Int,
    onSelectPet: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(pets) { pet ->
            val isSelected = pet.id == selectedPetId

            PixelFrame(
                backgroundColor = if (isSelected) Color(0xFF311B92) else Color(0xFF1B172A),
                borderColor = if (isSelected) RetroGold else Color(0xFF4A3B69),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectPet(pet.id) }
                    .testTag("pet_roster_item_${pet.id}")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PixelSprite(
                        spriteKey = pet.speciesId,
                        size = 40.dp,
                        animated = true
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(pet.nickname, fontFamily = PressStartFontFamily, fontSize = 9.sp, color = Color.White)
                            if (pet.isEquipped) {
                                Spacer(modifier = Modifier.width(4.dp))
                                PixelBadge(text = "AKTIF", backgroundColor = Color(0xFF2E7D32))
                            }
                        }
                        Text(
                            text = "Lv.${pet.level} • Stage ${pet.evolutionStage}",
                            fontFamily = Vt323FontFamily,
                            fontSize = 14.sp,
                            color = Color(0xFFCE93D8)
                        )
                    }
                    PixelButton(
                        onClick = { onSelectPet(pet.id) },
                        backgroundColor = Color(0xFF3949AB)
                    ) {
                        Text("PILIH", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun PixelFeedPetDialog(
    pet: PetEntity,
    edibleItems: List<InventoryItemEntity>,
    onFeed: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        PixelFrame(
            backgroundColor = Color(0xFF191428),
            borderColor = Color(0xFFFF9800),
            modifier = Modifier.fillMaxWidth().testTag("feed_pet_dialog")
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🍗 MAKAN ${pet.nickname.uppercase()}",
                        fontFamily = PressStartFontFamily,
                        fontSize = 9.sp,
                        color = RetroGold
                    )
                    PixelButton(onClick = onDismiss, backgroundColor = Color(0xFF424242)) {
                        Text("X", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (edibleItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        Text("Tidak ada ikan atau hasil panen di tas!", fontFamily = Vt323FontFamily, fontSize = 16.sp, color = Color.Gray)
                    }
                } else {
                    LazyColumn(modifier = Modifier.height(240.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(edibleItems) { item ->
                            PixelFrame(
                                backgroundColor = Color(0xFF221A36),
                                borderColor = Color(0xFF4A3B69),
                                contentPadding = 6.dp,
                                modifier = Modifier.fillMaxWidth().clickable { onFeed(item.id) }
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text(item.iconEmoji, fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.name, fontFamily = Vt323FontFamily, fontSize = 15.sp, color = Color.White)
                                        Text("+30 Kenyang & +15 EXP", fontFamily = Vt323FontFamily, fontSize = 13.sp, color = Color(0xFF81C784))
                                    }
                                    PixelBadge(text = "x${item.count}", backgroundColor = Color(0xFF3949AB))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PixelAdoptPetDialog(
    playerGold: Int,
    onAdopt: (speciesId: String, name: String, cost: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedSpecies by remember { mutableStateOf(GameDatabaseRegistry.PET_SPECIES[0]) }
    val adoptCost = 450

    Dialog(onDismissRequest = onDismiss) {
        PixelFrame(
            backgroundColor = Color(0xFF191428),
            borderColor = RetroGold,
            modifier = Modifier.fillMaxWidth().testTag("adopt_pet_dialog")
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🏡 ADOPSI PET PIXEL", fontFamily = PressStartFontFamily, fontSize = 9.sp, color = RetroGold)
                    PixelButton(onClick = onDismiss, backgroundColor = Color(0xFF424242)) {
                        Text("X", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Biaya: $adoptCost Gold", fontFamily = Vt323FontFamily, fontSize = 15.sp, color = Color(0xFFFFD54F))

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.height(240.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(GameDatabaseRegistry.PET_SPECIES) { species ->
                        val isSelected = selectedSpecies.id == species.id
                        PixelFrame(
                            backgroundColor = if (isSelected) Color(0xFF4A148C) else Color(0xFF221A36),
                            borderColor = if (isSelected) RetroGold else Color(0xFF4A3B69),
                            contentPadding = 6.dp,
                            modifier = Modifier.fillMaxWidth().clickable { selectedSpecies = species }
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                PixelSprite(spriteKey = species.id, size = 36.dp, animated = true)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(species.baseName, fontFamily = PressStartFontFamily, fontSize = 8.sp, color = Color.White)
                                    Text(species.specialty, fontFamily = Vt323FontFamily, fontSize = 14.sp, color = Color(0xFFCE93D8))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                PixelButton(
                    onClick = { onAdopt(selectedSpecies.id, selectedSpecies.baseName, adoptCost) },
                    enabled = playerGold >= adoptCost,
                    backgroundColor = Color(0xFF2E7D32),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ADOPSI ($adoptCost G)", fontFamily = PressStartFontFamily, fontSize = 8.sp, color = Color.White)
                }
            }
        }
    }
}
