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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.InventoryItemEntity
import com.example.data.model.CookingRecipe
import com.example.data.model.GameDatabaseRegistry
import com.example.data.model.ItemRarity
import com.example.data.model.ItemType
import com.example.ui.components.PixelBadge
import com.example.ui.components.PixelButton
import com.example.ui.components.PixelFrame
import com.example.ui.components.RetroGold
import com.example.ui.theme.PressStartFontFamily
import com.example.ui.theme.Vt323FontFamily
import com.example.ui.viewmodel.GameViewModel
import com.example.util.LocalSoundManager

@Composable
fun MarketScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val soundManager = LocalSoundManager.current
    val player by viewModel.playerProfile.collectAsStateWithLifecycle()
    val inventory by viewModel.inventory.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F0C1B))
            .testTag("market_screen_root")
    ) {
        // Pixel Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D0B14))
                .padding(bottom = 2.dp)
                .background(Color(0xFFE65100))
                .padding(bottom = 2.dp)
                .background(Color(0xFF2C1608))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("market_back_btn")) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "PASAR & TEMPA [PIXEL]",
                        fontFamily = PressStartFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = RetroGold
                    )
                    Text(
                        text = "Toko, jual panen, pandai besi & masak hidangan",
                        fontFamily = Vt323FontFamily,
                        fontSize = 14.sp,
                        color = Color(0xFFFFCC80)
                    )
                }
            }
        }

        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color(0xFF23140C),
            contentColor = RetroGold
        ) {
            Tab(selected = selectedTabIndex == 0, onClick = { soundManager.playTabSwitch(); selectedTabIndex = 0 }, text = { Text("🛒 BELI", fontFamily = PressStartFontFamily, fontSize = 7.sp) })
            Tab(selected = selectedTabIndex == 1, onClick = { soundManager.playTabSwitch(); selectedTabIndex = 1 }, text = { Text("💰 JUAL", fontFamily = PressStartFontFamily, fontSize = 7.sp) })
            Tab(selected = selectedTabIndex == 2, onClick = { soundManager.playTabSwitch(); selectedTabIndex = 2 }, text = { Text("🔨 TEMPA", fontFamily = PressStartFontFamily, fontSize = 7.sp) })
            Tab(selected = selectedTabIndex == 3, onClick = { soundManager.playTabSwitch(); selectedTabIndex = 3 }, text = { Text("🍳 MASAK", fontFamily = PressStartFontFamily, fontSize = 7.sp) })
        }

        when (selectedTabIndex) {
            0 -> PixelBuyShopView(
                playerGold = player?.gold ?: 0,
                onBuyItem = { id, name, type, icon, rarity, cost ->
                    soundManager.playCoin()
                    viewModel.buyItem(id, name, type, icon, rarity, cost)
                }
            )
            1 -> PixelSellMarketView(
                inventory = inventory,
                onSellItem = { item ->
                    soundManager.playCoin()
                    viewModel.sellItem(item, 1)
                }
            )
            2 -> PixelBlacksmithUpgradeView(
                currentRodLevel = player?.fishingRodLevel ?: 1,
                playerGold = player?.gold ?: 0,
                playerStones = player?.upgradeStones ?: 0,
                inventory = inventory,
                onUpgradeRod = {
                    soundManager.playVictory()
                    viewModel.upgradeFishingRod()
                },
                onCraftRecipe = { recipeKey ->
                    soundManager.playVictory()
                    viewModel.craftBlacksmith(recipeKey)
                }
            )
            3 -> PixelCookingKitchenView(
                recipes = GameDatabaseRegistry.COOKING_RECIPES,
                inventory = inventory,
                onCook = { recipe ->
                    soundManager.playVictory()
                    viewModel.cookRecipe(recipe)
                }
            )
        }
    }
}

@Composable
fun PixelBuyShopView(
    playerGold: Int,
    onBuyItem: (id: String, name: String, type: ItemType, icon: String, rarity: ItemRarity, cost: Int) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("ALL") }

    val categories = listOf(
        "ALL" to "SEMUA",
        "FARM" to "🌽 TANI",
        "FISH" to "🎣 MANCING",
        "PET" to "🐾 PET",
        "UPGRADE" to "🔨 MATERIAL",
        "CONSUMABLE" to "🧪 POTION/PETI"
    )

    LazyColumn(modifier = Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(categories) { (key, label) ->
                    val isSelected = selectedCategory == key
                    PixelButton(
                        onClick = { selectedCategory = key },
                        backgroundColor = if (isSelected) Color(0xFFE65100) else Color(0xFF261D2E),
                        testTag = "shop_tab_$key"
                    ) {
                        Text(label, fontFamily = PressStartFontFamily, fontSize = 7.sp, color = if (isSelected) Color.White else Color(0xFFB0BEC5))
                    }
                }
            }
        }

        // 1. Seeds & Crops (Category: ALL or FARM)
        if (selectedCategory == "ALL" || selectedCategory == "FARM") {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text("BENIH & BIBIT PERTANIAN:", fontFamily = PressStartFontFamily, fontSize = 8.sp, color = RetroGold)
            }
            items(GameDatabaseRegistry.CROPS) { crop ->
                PixelShopItemCard(
                    name = crop.seedName,
                    desc = "Waktu ${crop.growDurationSeconds}s • Hasil: ${crop.name} (+${crop.expReward} EXP)",
                    icon = crop.iconEmoji,
                    cost = crop.seedCost,
                    canAfford = playerGold >= crop.seedCost,
                    onBuy = {
                        onBuyItem("crop_seed_${crop.id}", crop.seedName, ItemType.SEED, crop.iconEmoji, crop.rarity, crop.seedCost)
                    }
                )
            }
        }

        // 2. Baits & Fishing (Category: ALL or FISH)
        if (selectedCategory == "ALL" || selectedCategory == "FISH") {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text("UMPAN & PERALATAN MANCING:", fontFamily = PressStartFontFamily, fontSize = 8.sp, color = RetroGold)
            }
            items(GameDatabaseRegistry.BAITS) { bait ->
                PixelShopItemCard(
                    name = bait.name,
                    desc = "${bait.description} (+${bait.rareBonus}% Rarity)",
                    icon = bait.iconEmoji,
                    cost = bait.costGold,
                    canAfford = playerGold >= bait.costGold,
                    onBuy = {
                        onBuyItem("bait_${bait.id}", bait.name, ItemType.BAIT, bait.iconEmoji, ItemRarity.UNCOMMON, bait.costGold)
                    }
                )
            }
        }

        // 3. Filtered Catalog Items (Farms, Pet, Material, Consumable)
        val filteredCatalog = GameDatabaseRegistry.BOT_ITEM_CATALOG.filter { item ->
            when (selectedCategory) {
                "ALL" -> true
                "FARM" -> item.itemType == ItemType.MATERIAL && (item.id.contains("fertilizer") || item.id.contains("tool"))
                "FISH" -> item.id.contains("fishing") || item.id.contains("hook")
                "PET" -> item.itemType == ItemType.PET_FOOD || item.id.contains("pet")
                "UPGRADE" -> item.itemType == ItemType.MATERIAL && !item.id.contains("fertilizer") && !item.id.contains("pet")
                "CONSUMABLE" -> item.itemType == ItemType.POTION || item.itemType == ItemType.CHEST || item.id.contains("key")
                else -> true
            }
        }

        if (filteredCatalog.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                val catTitle = when (selectedCategory) {
                    "FARM" -> "PERLENGKAPAN & PUPUK TANI:"
                    "FISH" -> "PERALATAN MANCING TAMBAHAN:"
                    "PET" -> "PERAWATAN & EVOLUSI PET:"
                    "UPGRADE" -> "MATERIAL & BIJIH TEMPA:"
                    "CONSUMABLE" -> "RAMUAN, GULUNGAN & PETI HARTA:"
                    else -> "KATALOG ITEM BOT RPG LENGKAP:"
                }
                Text(catTitle, fontFamily = PressStartFontFamily, fontSize = 8.sp, color = RetroGold)
            }

            items(filteredCatalog) { botItem ->
                val cost = botItem.buyPrice ?: (botItem.sellPrice * 2)
                PixelShopItemCard(
                    name = botItem.name,
                    desc = botItem.description,
                    icon = botItem.iconEmoji,
                    cost = cost,
                    canAfford = playerGold >= cost,
                    onBuy = {
                        onBuyItem(botItem.id, botItem.name, botItem.itemType, botItem.iconEmoji, botItem.rarity, cost)
                    }
                )
            }
        }
    }
}

@Composable
fun PixelShopItemCard(
    name: String,
    desc: String,
    icon: String,
    cost: Int,
    canAfford: Boolean,
    onBuy: () -> Unit
) {
    PixelFrame(
        backgroundColor = Color(0xFF1E1724),
        borderColor = if (canAfford) Color(0xFF5D4037) else Color(0xFF261921),
        contentPadding = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 22.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontFamily = PressStartFontFamily, fontSize = 8.sp, color = Color.White)
                Text(desc, fontFamily = Vt323FontFamily, fontSize = 14.sp, color = Color(0xFFB0BEC5))
            }
            PixelButton(
                onClick = onBuy,
                enabled = canAfford,
                backgroundColor = Color(0xFF2E7D32),
                testTag = "buy_btn_${name.replace(" ", "_")}"
            ) {
                Text("$cost G", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun PixelSellMarketView(
    inventory: List<InventoryItemEntity>,
    onSellItem: (InventoryItemEntity) -> Unit
) {
    val sellableItems = inventory.filter { it.count > 0 && it.sellPrice > 0 }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            PixelFrame(backgroundColor = Color(0xFF2A1C12), borderColor = RetroGold, contentPadding = 8.dp, modifier = Modifier.fillMaxWidth()) {
                Text("Tengkulak desa membeli hasil panen, ikan tangkapan, bijih tempa, dan drop monster!", fontFamily = Vt323FontFamily, fontSize = 15.sp, color = RetroGold)
            }
        }

        if (sellableItems.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                    Text("Tas kosong! Belum ada barang untuk dijual.", fontFamily = Vt323FontFamily, fontSize = 16.sp, color = Color.Gray)
                }
            }
        } else {
            items(sellableItems) { item ->
                PixelFrame(backgroundColor = Color(0xFF1E1724), borderColor = Color(0xFF4E342E), contentPadding = 8.dp, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(item.iconEmoji, fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name, fontFamily = Vt323FontFamily, fontSize = 16.sp, color = Color.White)
                            Text("Ada: ${item.count} buah • Tipe: ${item.itemType}", fontFamily = Vt323FontFamily, fontSize = 13.sp, color = Color(0xFFB0BEC5))
                        }
                        PixelButton(onClick = { onSellItem(item) }, backgroundColor = Color(0xFFF57F17), testTag = "sell_btn_${item.id}") {
                            Text("+${item.sellPrice} G", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PixelBlacksmithUpgradeView(
    currentRodLevel: Int,
    playerGold: Int,
    playerStones: Int,
    inventory: List<InventoryItemEntity>,
    onUpgradeRod: () -> Unit,
    onCraftRecipe: (String) -> Unit
) {
    val currentTier = GameDatabaseRegistry.ROD_TIERS.getOrElse(currentRodLevel - 1) { GameDatabaseRegistry.ROD_TIERS[0] }
    val nextTier = GameDatabaseRegistry.ROD_TIERS.getOrNull(currentRodLevel)

    val ironCount = inventory.find { it.id == "item_iron_ore" }?.count ?: 0
    val stoneCount = inventory.find { it.id == "item_upgrade_stone" }?.count ?: playerStones
    val scaleCount = inventory.find { it.id == "item_dragon_scale" }?.count ?: 0

    LazyColumn(modifier = Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            PixelFrame(backgroundColor = Color(0xFF1E1724), borderColor = RetroGold, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("BENGKEL PANDAI BESI", fontFamily = PressStartFontFamily, fontSize = 10.sp, color = RetroGold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Pancing Saat Ini: ${currentTier.name} (Tier ${currentTier.level})", fontFamily = Vt323FontFamily, fontSize = 16.sp, color = Color.White)
                    Text("Speed ${(currentTier.catchSpeedMultiplier * 100).toInt()}% • Rare +${currentTier.rareBonusPercent}%", fontFamily = Vt323FontFamily, fontSize = 14.sp, color = Color(0xFF80DEEA))

                    Spacer(modifier = Modifier.height(12.dp))

                    if (nextTier != null) {
                        Text("Upgrade Berikutnya: ${nextTier.name}", fontFamily = PressStartFontFamily, fontSize = 8.sp, color = Color(0xFF81D4FA))
                        Text("Biaya: ${nextTier.upgradeCostGold} Gold • ${nextTier.upgradeCostStones} Batu Tempa", fontFamily = Vt323FontFamily, fontSize = 15.sp, color = RetroGold)
                        Spacer(modifier = Modifier.height(8.dp))
                        PixelButton(
                            onClick = onUpgradeRod,
                            enabled = playerGold >= nextTier.upgradeCostGold && playerStones >= nextTier.upgradeCostStones,
                            backgroundColor = Color(0xFFE65100),
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "blacksmith_upgrade_button"
                        ) {
                            Text("🔨 TINGKATKAN PANCING", fontFamily = PressStartFontFamily, fontSize = 8.sp, color = Color.White)
                        }
                    } else {
                        Text("Pancing sudah level puncak (Nebula Kosmik)! 🌟", fontFamily = Vt323FontFamily, fontSize = 16.sp, color = Color(0xFF69F0AE))
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text("PELEBURAN & PENEMPAAN MATERIAL:", fontFamily = PressStartFontFamily, fontSize = 9.sp, color = RetroGold)
        }

        // Crafting Recipe 1: 3 Iron Ore -> 1 Upgrade Stone
        item {
            PixelFrame(backgroundColor = Color(0xFF1E1724), borderColor = Color(0xFF5D4037), contentPadding = 8.dp, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("🪨", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("TEMPA BATU TEMPA", fontFamily = PressStartFontFamily, fontSize = 8.sp, color = Color.White)
                        Text("Butuh: 3 Bijih Besi (Punya: $ironCount) + 50G", fontFamily = Vt323FontFamily, fontSize = 14.sp, color = Color(0xFFB0BEC5))
                    }
                    PixelButton(
                        onClick = { onCraftRecipe("forge_stone") },
                        enabled = ironCount >= 3 && playerGold >= 50,
                        backgroundColor = Color(0xFF3949AB),
                        testTag = "forge_stone_btn"
                    ) {
                        Text("TEMPA", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color.White)
                    }
                }
            }
        }

        // Crafting Recipe 2: 2 Upgrade Stone -> 1 Gold Ingot
        item {
            PixelFrame(backgroundColor = Color(0xFF1E1724), borderColor = Color(0xFF5D4037), contentPadding = 8.dp, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("🪙", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("LEBUR BATANGAN EMAS", fontFamily = PressStartFontFamily, fontSize = 8.sp, color = Color.White)
                        Text("Butuh: 2 Batu Tempa (Punya: $stoneCount) + 100G", fontFamily = Vt323FontFamily, fontSize = 14.sp, color = Color(0xFFB0BEC5))
                    }
                    PixelButton(
                        onClick = { onCraftRecipe("forge_gold_ingot") },
                        enabled = stoneCount >= 2 && playerGold >= 100,
                        backgroundColor = Color(0xFFF57F17),
                        testTag = "forge_gold_btn"
                    ) {
                        Text("LEBUR", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color.White)
                    }
                }
            }
        }

        // Crafting Recipe 3: 1 Dragon Scale + 2 Upgrade Stones -> Dragon Scale Armor
        item {
            PixelFrame(backgroundColor = Color(0xFF1E1724), borderColor = Color(0xFF5D4037), contentPadding = 8.dp, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("🛡️", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("ZIRAH SISIK NAGA PURBA", fontFamily = PressStartFontFamily, fontSize = 8.sp, color = Color.White)
                        Text("Butuh: 1 Sisik Naga ($scaleCount) & 2 Batu ($stoneCount) + 250G", fontFamily = Vt323FontFamily, fontSize = 14.sp, color = Color(0xFFB0BEC5))
                    }
                    PixelButton(
                        onClick = { onCraftRecipe("forge_dragon_armor") },
                        enabled = scaleCount >= 1 && stoneCount >= 2 && playerGold >= 250,
                        backgroundColor = Color(0xFFD84315),
                        testTag = "forge_armor_btn"
                    ) {
                        Text("TEMPA", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun PixelCookingKitchenView(
    recipes: List<CookingRecipe>,
    inventory: List<InventoryItemEntity>,
    onCook: (CookingRecipe) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            PixelFrame(backgroundColor = Color(0xFF281510), borderColor = Color(0xFFD84315), contentPadding = 8.dp, modifier = Modifier.fillMaxWidth()) {
                Text("DAPUR RESEP: Masak ikan + hasil kebun menjadi hidangan pemulih!", fontFamily = Vt323FontFamily, fontSize = 15.sp, color = Color(0xFFFFCCBC))
            }
        }

        items(recipes) { recipe ->
            val hasCrop = inventory.any { it.id == "item_crop_${recipe.requiredCropId}" && it.count >= recipe.requiredCropCount }
            val hasFish = inventory.any {
                (it.id == "item_${recipe.requiredFishId}" || it.id.startsWith("item_crop_${recipe.requiredFishId}")) && it.count >= recipe.requiredFishCount
            }
            val canCook = hasCrop && hasFish

            PixelFrame(
                backgroundColor = Color(0xFF1E1724),
                borderColor = if (canCook) Color(0xFFD84315) else Color(0xFF3E2723),
                contentPadding = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(recipe.iconEmoji, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(recipe.name.uppercase(), fontFamily = PressStartFontFamily, fontSize = 8.sp, color = Color.White)
                            Text(recipe.description, fontFamily = Vt323FontFamily, fontSize = 14.sp, color = Color(0xFFB0BEC5))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Pulih: +${recipe.energyRestored}⚡ +${recipe.hpRestored}HP", fontFamily = Vt323FontFamily, fontSize = 14.sp, color = Color(0xFF81C784))
                        PixelButton(onClick = { onCook(recipe) }, enabled = canCook, backgroundColor = Color(0xFFD84315), testTag = "cook_btn_${recipe.id}") {
                            Text("MASAK 🍳", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
