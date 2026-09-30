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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
import com.example.data.local.entity.PlayerProfileEntity
import com.example.data.local.entity.QuestEntity
import com.example.data.model.GameDatabaseRegistry
import com.example.data.model.ItemRarity
import com.example.ui.components.PixelBadge
import com.example.ui.components.PixelButton
import com.example.ui.components.PixelFrame
import com.example.ui.components.PixelSprite
import com.example.ui.components.PixelStepProgressBar
import com.example.ui.components.RarityChip
import com.example.ui.components.RetroBorderOuter
import com.example.ui.components.RetroGold
import com.example.ui.components.RetroPanelBg
import com.example.ui.theme.PressStartFontFamily
import com.example.ui.theme.Vt323FontFamily
import com.example.ui.viewmodel.GameViewModel
import com.example.util.LocalSoundManager
import com.example.util.SoundManager
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun HomeScreen(
    viewModel: GameViewModel,
    onNavigateToFishing: () -> Unit,
    onNavigateToFarming: () -> Unit,
    onNavigateToPets: () -> Unit,
    onNavigateToAdventure: () -> Unit,
    onNavigateToMarket: () -> Unit,
    onNavigateToInventory: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val soundManager = LocalSoundManager.current
    val player by viewModel.playerProfile.collectAsStateWithLifecycle()
    val equippedPet by viewModel.equippedPet.collectAsStateWithLifecycle()
    val quests by viewModel.dailyQuests.collectAsStateWithLifecycle()
    val inventory by viewModel.inventory.collectAsStateWithLifecycle()

    var showInventoryDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F0C1B))
            .testTag("home_screen_content"),
        contentAlignment = Alignment.TopCenter
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .widthIn(max = 1100.dp)
                .fillMaxSize()
        ) {
            val isWide = maxWidth >= 720.dp

            if (isWide) {
                // Wide / Tablet / Landscape 2-Column Layout
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Column: Player Card & Companion
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        HeroPixelCard(
                            player = player,
                            equippedPet = equippedPet,
                            onNavigateToInventory = onNavigateToInventory,
                            onPlayMenuClick = { soundManager.playMenuClick() }
                        )

                        ActivePetCompanionCard(
                            equippedPet = equippedPet,
                            onNavigateToPets = onNavigateToPets
                        )
                    }

                    // Right Column: Navigation Adventure Menu & Quests
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        AdventureMenuSection(
                            soundManager = soundManager,
                            onNavigateToFishing = onNavigateToFishing,
                            onNavigateToFarming = onNavigateToFarming,
                            onNavigateToPets = onNavigateToPets,
                            onNavigateToAdventure = onNavigateToAdventure,
                            onNavigateToMarket = onNavigateToMarket,
                            onNavigateToInventory = onNavigateToInventory
                        )

                        DailyQuestsHeader(quests = quests)

                        quests.forEach { quest ->
                            PixelQuestCard(
                                quest = quest,
                                onClaim = { viewModel.claimQuest(quest.id) }
                            )
                        }
                    }
                }
            } else {
                // Mobile Portrait Single Column
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        HeroPixelCard(
                            player = player,
                            equippedPet = equippedPet,
                            onNavigateToInventory = onNavigateToInventory,
                            onPlayMenuClick = { soundManager.playMenuClick() }
                        )
                    }

                    item {
                        ActivePetCompanionCard(
                            equippedPet = equippedPet,
                            onNavigateToPets = onNavigateToPets
                        )
                    }

                    item {
                        AdventureMenuSection(
                            soundManager = soundManager,
                            onNavigateToFishing = onNavigateToFishing,
                            onNavigateToFarming = onNavigateToFarming,
                            onNavigateToPets = onNavigateToPets,
                            onNavigateToAdventure = onNavigateToAdventure,
                            onNavigateToMarket = onNavigateToMarket,
                            onNavigateToInventory = onNavigateToInventory
                        )
                    }

                    item {
                        DailyQuestsHeader(quests = quests)
                    }

                    items(quests) { quest ->
                        PixelQuestCard(
                            quest = quest,
                            onClaim = { viewModel.claimQuest(quest.id) }
                        )
                    }
                }
            }
        }
    }

    if (showInventoryDialog) {
        PixelInventoryDialog(
            items = inventory,
            onDismiss = { showInventoryDialog = false },
            onUseItem = { item -> viewModel.useItem(item) }
        )
    }
}

@Composable
fun HeroPixelCard(
    player: PlayerProfileEntity?,
    equippedPet: PetEntity?,
    onNavigateToInventory: () -> Unit,
    onPlayMenuClick: () -> Unit
) {
    PixelFrame(
        backgroundColor = Color(0xFF1B162C),
        borderColor = Color(0xFF4A3B69),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "RPG REALM [INDIE]",
                        fontFamily = PressStartFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = RetroGold
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Dunia Petualangan Pixel Retro 16-Bit",
                        fontFamily = Vt323FontFamily,
                        fontSize = 15.sp,
                        color = Color(0xFFB0BEC5)
                    )
                }

                PixelButton(
                    onClick = {
                        onPlayMenuClick()
                        onNavigateToInventory()
                    },
                    backgroundColor = Color(0xFF37474F),
                    testTag = "open_inventory_button"
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Backpack, contentDescription = "Tas", tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("TAS", fontFamily = PressStartFontFamily, fontSize = 8.sp, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PixelStatBox(title = "PANCING", value = "Tier ${player?.fishingRodLevel ?: 1}", icon = "🎣", modifier = Modifier.weight(1f))
                PixelStatBox(title = "PET", value = if (equippedPet != null) "Stage ${equippedPet.evolutionStage}" else "None", icon = "🐾", modifier = Modifier.weight(1f))
                PixelStatBox(title = "LEVEL", value = "Lv.${player?.level ?: 1}", icon = "⚡", modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun ActivePetCompanionCard(
    equippedPet: PetEntity?,
    onNavigateToPets: () -> Unit
) {
    val pet = equippedPet
    val species = pet?.let { p -> GameDatabaseRegistry.PET_SPECIES.find { it.id == p.speciesId } }

    PixelFrame(
        backgroundColor = Color(0xFF221738),
        borderColor = Color(0xFF7B1FA2),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToPets() }
            .testTag("active_pet_banner_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PixelSprite(
                spriteKey = pet?.speciesId ?: "pet_wolf",
                size = 56.dp,
                animated = true
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = pet?.nickname ?: "Belum Memasang Pet",
                        fontFamily = PressStartFontFamily,
                        fontSize = 10.sp,
                        color = Color.White
                    )
                    if (pet != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        PixelBadge(
                            text = "S${pet.evolutionStage} Lv.${pet.level}",
                            backgroundColor = Color(0xFF6A1B9A)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (pet != null) {
                    Text(
                        text = species?.specialty ?: "Sahabat Tempur",
                        fontFamily = Vt323FontFamily,
                        fontSize = 14.sp,
                        color = Color(0xFFCE93D8)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
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
                } else {
                    Text(
                        text = "Buka menu Pet untuk mengadopsi sahabat!",
                        fontFamily = Vt323FontFamily,
                        fontSize = 14.sp,
                        color = Color(0xFFB0BEC5)
                    )
                }
            }

            Icon(Icons.Default.ChevronRight, contentDescription = "Detail", tint = Color.Gray)
        }
    }
}

@Composable
fun AdventureMenuSection(
    soundManager: SoundManager,
    onNavigateToFishing: () -> Unit,
    onNavigateToFarming: () -> Unit,
    onNavigateToPets: () -> Unit,
    onNavigateToAdventure: () -> Unit,
    onNavigateToMarket: () -> Unit,
    onNavigateToInventory: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "MENU PETUALANGAN:",
            fontFamily = PressStartFontFamily,
            fontSize = 10.sp,
            color = RetroGold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PixelMenuTile(
                title = "MANCING",
                subtitle = "Tangkap Ikan & Upgrade",
                icon = "🎣",
                bgColor = Color(0xFF006064),
                testTag = "nav_btn_fishing",
                modifier = Modifier.weight(1f),
                onClick = {
                    soundManager.playMenuClick()
                    onNavigateToFishing()
                }
            )
            PixelMenuTile(
                title = "KEBUN TANI",
                subtitle = "Tanam Benih & Siram",
                icon = "🌾",
                bgColor = Color(0xFF1B5E20),
                testTag = "nav_btn_farming",
                modifier = Modifier.weight(1f),
                onClick = {
                    soundManager.playMenuClick()
                    onNavigateToFarming()
                }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PixelMenuTile(
                title = "PET ANIMAL",
                subtitle = "Latih & Evolusi 3-Tier",
                icon = "🐾",
                bgColor = Color(0xFF4A148C),
                testTag = "nav_btn_pets",
                modifier = Modifier.weight(1f),
                onClick = {
                    soundManager.playMenuClick()
                    onNavigateToPets()
                }
            )
            PixelMenuTile(
                title = "DUNGEON",
                subtitle = "Berburu Monster & Boss",
                icon = "⚔️",
                bgColor = Color(0xFF880E4F),
                testTag = "nav_btn_adventure",
                modifier = Modifier.weight(1f),
                onClick = {
                    soundManager.playMenuClick()
                    onNavigateToAdventure()
                }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PixelMenuTile(
                title = "PASAR & TEMPA",
                subtitle = "Toko, Jual & Dapur",
                icon = "🏪",
                bgColor = Color(0xFFE65100),
                testTag = "nav_btn_market",
                modifier = Modifier.weight(1f),
                onClick = {
                    soundManager.playMenuClick()
                    onNavigateToMarket()
                }
            )
            PixelMenuTile(
                title = "TAS [PIXEL]",
                subtitle = "Kelola Item & Drop",
                icon = "🎒",
                bgColor = Color(0xFF311B92),
                testTag = "nav_btn_inventory",
                modifier = Modifier.weight(1f),
                onClick = {
                    soundManager.playMenuClick()
                    onNavigateToInventory()
                }
            )
        }
    }
}

@Composable
fun DailyQuestsHeader(quests: List<QuestEntity>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "MISI HARIAN:",
            fontFamily = PressStartFontFamily,
            fontSize = 10.sp,
            color = RetroGold
        )
        Text(
            text = "${quests.count { it.isCompleted && !it.isClaimed }} Siap",
            fontFamily = PressStartFontFamily,
            fontSize = 8.sp,
            color = Color(0xFF81C784)
        )
    }
}

@Composable
fun PixelStatBox(
    title: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFF0F0C1B), RectangleShape)
            .padding(1.dp)
            .background(Color(0xFF28213E), RectangleShape)
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(title, fontFamily = PressStartFontFamily, fontSize = 6.sp, color = Color(0xFFB0BEC5))
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontFamily = PressStartFontFamily, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun PixelMenuTile(
    title: String,
    subtitle: String,
    icon: String,
    bgColor: Color,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    PixelFrame(
        backgroundColor = bgColor,
        borderColor = Color(0xFFE0E0E0).copy(alpha = 0.4f),
        contentPadding = 10.dp,
        modifier = modifier
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    fontFamily = PressStartFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontFamily = Vt323FontFamily,
                    fontSize = 14.sp,
                    color = Color(0xFFECEFF1)
                )
            }
        }
    }
}

@Composable
fun PixelQuestCard(
    quest: QuestEntity,
    onClaim: () -> Unit
) {
    PixelFrame(
        backgroundColor = if (quest.isClaimed) Color(0xFF14121F).copy(alpha = 0.5f) else Color(0xFF1C182B),
        borderColor = if (quest.isCompleted && !quest.isClaimed) Color(0xFF4CAF50) else Color(0xFF382B54),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quest_card_${quest.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quest.title,
                    fontFamily = PressStartFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = quest.description,
                    fontFamily = Vt323FontFamily,
                    fontSize = 15.sp,
                    color = Color(0xFFB0BEC5)
                )
                Spacer(modifier = Modifier.height(4.dp))
                PixelStepProgressBar(
                    current = quest.currentCount,
                    max = quest.targetCount,
                    barColor = if (quest.isCompleted) Color(0xFF4CAF50) else Color(0xFFFFB300),
                    height = 8.dp,
                    showLabel = false
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "REWARD: +${quest.rewardGold}G +${quest.rewardExp}EXP" + if (quest.rewardDiamonds > 0) " +${quest.rewardDiamonds}💎" else "",
                    fontFamily = Vt323FontFamily,
                    fontSize = 14.sp,
                    color = RetroGold
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            when {
                quest.isClaimed -> {
                    PixelBadge(text = "SELESAI", backgroundColor = Color(0xFF2E7D32))
                }
                quest.isCompleted -> {
                    PixelButton(
                        onClick = onClaim,
                        backgroundColor = Color(0xFF43A047),
                        testTag = "claim_quest_${quest.id}"
                    ) {
                        Text("KLAIM 🎁", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color.White)
                    }
                }
                else -> {
                    Text(
                        text = "${quest.currentCount}/${quest.targetCount}",
                        fontFamily = PressStartFontFamily,
                        fontSize = 8.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun PixelInventoryDialog(
    items: List<InventoryItemEntity>,
    onDismiss: () -> Unit,
    onUseItem: (InventoryItemEntity) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("ALL") }

    val categories = listOf(
        "ALL" to "SEMUA",
        "FARM" to "🌽 TANI",
        "FISH" to "🐟 IKAN",
        "PET" to "🐾 PET",
        "MATERIAL" to "🔨 MATERIAL",
        "USE" to "🧪 KONSUMSI"
    )

    val filteredItems = items.filter { item ->
        when (selectedCategory) {
            "ALL" -> true
            "FARM" -> item.itemType == "SEED" || item.itemType == "CROP" || item.id.contains("fertilizer")
            "FISH" -> item.itemType == "FISH" || item.itemType == "BAIT" || item.id.contains("hook")
            "PET" -> item.itemType == "PET_FOOD" || item.id.contains("pet")
            "MATERIAL" -> item.itemType == "MATERIAL" || item.id.contains("ore") || item.id.contains("gem") || item.id.contains("scale")
            "USE" -> item.itemType == "POTION" || item.itemType == "FOOD" || item.itemType == "PET_FOOD" || item.itemType == "CHEST" || item.id.contains("potion") || item.id.contains("chest") || item.id.contains("scroll")
            else -> true
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        PixelFrame(
            backgroundColor = Color(0xFF14121F),
            borderColor = Color(0xFFFFD54F),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("inventory_dialog")
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎒 TAS PETUALANG",
                        fontFamily = PressStartFontFamily,
                        fontSize = 10.sp,
                        color = RetroGold
                    )
                    PixelButton(onClick = onDismiss, backgroundColor = Color(0xFF424242)) {
                        Text("X", fontFamily = PressStartFontFamily, fontSize = 8.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Category chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(categories) { (key, label) ->
                        val isSelected = selectedCategory == key
                        PixelButton(
                            onClick = { selectedCategory = key },
                            backgroundColor = if (isSelected) Color(0xFFE65100) else Color(0xFF261D2E),
                            testTag = "inv_cat_$key"
                        ) {
                            Text(label, fontFamily = PressStartFontFamily, fontSize = 6.sp, color = if (isSelected) Color.White else Color(0xFFB0BEC5))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (filteredItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Tidak ada barang di kategori ini.", fontFamily = Vt323FontFamily, fontSize = 16.sp, color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredItems) { item ->
                            val rarity = try {
                                ItemRarity.valueOf(item.rarity)
                            } catch (_: Exception) {
                                ItemRarity.COMMON
                            }

                            val isUsable = item.itemType in listOf("POTION", "FOOD", "PET_FOOD", "CHEST") ||
                                    item.id.contains("potion") || item.id.contains("scroll") || item.id.contains("chest") ||
                                    item.id.contains("pet_food") || item.id.contains("evo_crystal")

                            PixelFrame(
                                backgroundColor = Color(0xFF1F1A2F),
                                borderColor = rarity.composeColor().copy(alpha = 0.5f),
                                contentPadding = 6.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(item.iconEmoji, fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.name, fontFamily = Vt323FontFamily, fontSize = 16.sp, color = Color.White)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RarityChip(rarity = rarity)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("${item.sellPrice}G • ${item.itemType}", fontFamily = Vt323FontFamily, fontSize = 12.sp, color = RetroGold)
                                        }
                                    }
                                    PixelBadge(text = "x${item.count}", backgroundColor = Color(0xFF3949AB))

                                    if (isUsable) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        PixelButton(
                                            onClick = { onUseItem(item) },
                                            backgroundColor = Color(0xFF2E7D32),
                                            testTag = "use_item_${item.id}"
                                        ) {
                                            Text("PAKAI", fontFamily = PressStartFontFamily, fontSize = 6.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
