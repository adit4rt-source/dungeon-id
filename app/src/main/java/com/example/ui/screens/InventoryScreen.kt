package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.InventoryItemEntity
import com.example.data.model.ItemRarity
import com.example.ui.components.PixelBadge
import com.example.ui.components.PixelButton
import com.example.ui.components.PixelFrame
import com.example.ui.components.RarityChip
import com.example.ui.components.RetroBorderOuter
import com.example.ui.components.RetroGold
import com.example.ui.theme.PressStartFontFamily
import com.example.ui.theme.Vt323FontFamily
import com.example.ui.viewmodel.GameViewModel
import com.example.util.LocalSoundManager

enum class InventoryCategoryFilter(val key: String, val label: String, val icon: String) {
    ALL("ALL", "SEMUA", "🎒"),
    BAIT("BAIT", "UMPAN", "🪱"),
    SEED("SEED", "BENIH", "🌱"),
    DROP("DROP", "DROP MONSTER", "💀"),
    CROP("CROP", "PANEN", "🌾"),
    FISH("FISH", "IKAN", "🐟"),
    POTION("POTION", "KONSUMSI", "🧪")
}

enum class InventorySortOrder(val label: String) {
    COUNT_DESC("JUMLAH BANYAK"),
    RARITY_DESC("RARITY TINGGI"),
    SELL_PRICE_DESC("HARGA TINGGI"),
    NAME_ASC("NAMA A-Z")
}

@Composable
fun InventoryScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val soundManager = LocalSoundManager.current
    val inventory by viewModel.inventory.collectAsStateWithLifecycle()
    val player by viewModel.playerProfile.collectAsStateWithLifecycle()

    var selectedCategory by remember { mutableStateOf(InventoryCategoryFilter.ALL) }
    var selectedSortOrder by remember { mutableStateOf(InventorySortOrder.COUNT_DESC) }
    var searchQuery by remember { mutableStateOf("") }
    var isGridView by remember { mutableStateOf(true) }
    var selectedItem by remember { mutableStateOf<InventoryItemEntity?>(null) }

    // Keep selected item synced if counts change
    val currentSelected = inventory.find { it.id == selectedItem?.id } ?: inventory.firstOrNull()

    // Filter by Category & Search
    val filteredItems = inventory.filter { item ->
        val matchesCategory = when (selectedCategory) {
            InventoryCategoryFilter.ALL -> true
            InventoryCategoryFilter.BAIT -> item.itemType == "BAIT" || item.id.startsWith("bait_")
            InventoryCategoryFilter.SEED -> item.itemType == "SEED" || item.id.startsWith("crop_seed_")
            InventoryCategoryFilter.DROP -> {
                item.id.startsWith("item_upgrade_stone") || item.id.startsWith("item_iron_ore") ||
                        item.id.contains("dragon") || item.id.contains("gem") || item.id.contains("obsidian") ||
                        (item.itemType == "MATERIAL" && !item.id.contains("fertilizer"))
            }
            InventoryCategoryFilter.CROP -> item.itemType == "CROP" || item.id.startsWith("item_crop_")
            InventoryCategoryFilter.FISH -> item.itemType == "FISH" || item.id.startsWith("fish_")
            InventoryCategoryFilter.POTION -> {
                item.itemType in listOf("POTION", "FOOD", "PET_FOOD", "CHEST") ||
                        item.id.contains("potion") || item.id.contains("scroll") ||
                        item.id.contains("chest") || item.id.contains("key") || item.id.contains("food")
            }
        }
        val matchesSearch = searchQuery.isBlank() || item.name.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }.sortedWith { a, b ->
        when (selectedSortOrder) {
            InventorySortOrder.COUNT_DESC -> b.count.compareTo(a.count)
            InventorySortOrder.RARITY_DESC -> {
                val rarityA = try { ItemRarity.valueOf(a.rarity).ordinal } catch (_: Exception) { 0 }
                val rarityB = try { ItemRarity.valueOf(b.rarity).ordinal } catch (_: Exception) { 0 }
                rarityB.compareTo(rarityA)
            }
            InventorySortOrder.SELL_PRICE_DESC -> (b.sellPrice * b.count).compareTo(a.sellPrice * a.count)
            InventorySortOrder.NAME_ASC -> a.name.compareTo(b.name, ignoreCase = true)
        }
    }

    val totalItemsCount = inventory.sumOf { it.count }
    val totalEstimatedValue = inventory.sumOf { it.sellPrice * it.count }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F0C1B))
            .testTag("inventory_screen_root")
    ) {
        // Pixel Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D0B14))
                .padding(bottom = 2.dp)
                .background(Color(0xFF4A3B69))
                .padding(bottom = 2.dp)
                .background(Color(0xFF1B152B))
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
                    modifier = Modifier.testTag("inventory_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TAS & INVENTARIS [PIXEL]",
                        fontFamily = PressStartFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = RetroGold
                    )
                    Text(
                        text = "$totalItemsCount Item (${inventory.size} Jenis) • Nilai: $totalEstimatedValue G",
                        fontFamily = Vt323FontFamily,
                        fontSize = 14.sp,
                        color = Color(0xFFCE93D8)
                    )
                }

                // Grid / List toggle
                PixelButton(
                    onClick = {
                        soundManager.playMenuClick()
                        isGridView = !isGridView
                    },
                    backgroundColor = Color(0xFF382B54),
                    testTag = "toggle_view_mode_btn"
                ) {
                    Icon(
                        imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                        contentDescription = "Ganti Tampilan",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.TopCenter
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .widthIn(max = 1100.dp)
                    .fillMaxSize()
            ) {
                val isWide = maxWidth >= 720.dp

                if (isWide) {
                    // Split pane layout: Left side list/grid, Right side inspector
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1.3f)
                                .fillMaxSize()
                        ) {
                            // Search Bar & Sort Controls
                            InventoryFilterControls(
                                searchQuery = searchQuery,
                                onSearchChange = { searchQuery = it },
                                selectedCategory = selectedCategory,
                                onSelectCategory = {
                                    soundManager.playTabSwitch()
                                    selectedCategory = it
                                },
                                selectedSortOrder = selectedSortOrder,
                                onSelectSortOrder = { selectedSortOrder = it }
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Grid or List
                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                InventoryItemsView(
                                    filteredItems = filteredItems,
                                    isGridView = isGridView,
                                    currentSelected = currentSelected,
                                    onSelectItem = { item ->
                                        soundManager.playMenuClick()
                                        selectedItem = item
                                    }
                                )
                            }
                        }

                        // Right Column: Item Inspector
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            if (currentSelected != null) {
                                PixelItemDetailInspector(
                                    item = currentSelected,
                                    playerGold = player?.gold ?: 0,
                                    onUse = {
                                        soundManager.playMenuClick()
                                        viewModel.useItem(currentSelected)
                                    },
                                    onSellOne = {
                                        soundManager.playCoin()
                                        viewModel.sellItem(currentSelected, 1)
                                    },
                                    onSellAll = {
                                        soundManager.playCoin()
                                        viewModel.sellItem(currentSelected, currentSelected.count)
                                    }
                                )
                            } else {
                                PixelFrame(
                                    backgroundColor = Color(0xFF1B162C),
                                    borderColor = Color(0xFF4A3B69),
                                    contentPadding = 16.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Pilih item untuk melihat detail dan opsi aksi.",
                                        fontFamily = Vt323FontFamily,
                                        fontSize = 16.sp,
                                        color = Color(0xFFB0BEC5)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Mobile Portrait stacked layout
                    Column(modifier = Modifier.fillMaxSize()) {
                        InventoryFilterControls(
                            searchQuery = searchQuery,
                            onSearchChange = { searchQuery = it },
                            selectedCategory = selectedCategory,
                            onSelectCategory = {
                                soundManager.playTabSwitch()
                                selectedCategory = it
                            },
                            selectedSortOrder = selectedSortOrder,
                            onSelectSortOrder = { selectedSortOrder = it },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                        ) {
                            InventoryItemsView(
                                filteredItems = filteredItems,
                                isGridView = isGridView,
                                currentSelected = currentSelected,
                                onSelectItem = { item ->
                                    soundManager.playMenuClick()
                                    selectedItem = item
                                }
                            )
                        }

                        if (currentSelected != null) {
                            PixelItemDetailInspector(
                                item = currentSelected,
                                playerGold = player?.gold ?: 0,
                                onUse = {
                                    soundManager.playMenuClick()
                                    viewModel.useItem(currentSelected)
                                },
                                onSellOne = {
                                    soundManager.playCoin()
                                    viewModel.sellItem(currentSelected, 1)
                                },
                                onSellAll = {
                                    soundManager.playCoin()
                                    viewModel.sellItem(currentSelected, currentSelected.count)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryFilterControls(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: InventoryCategoryFilter,
    onSelectCategory: (InventoryCategoryFilter) -> Unit,
    selectedSortOrder: InventorySortOrder,
    onSelectSortOrder: (InventorySortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        PixelFrame(
            backgroundColor = Color(0xFF151022),
            borderColor = Color(0xFF382B54),
            contentPadding = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Cari",
                    tint = Color(0xFFB0BEC5),
                    modifier = Modifier.size(16.dp).padding(start = 4.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = {
                        Text("Cari umpan, benih, ore, ikan...", fontFamily = Vt323FontFamily, fontSize = 15.sp, color = Color.Gray)
                    },
                    textStyle = TextStyle(fontFamily = Vt323FontFamily, fontSize = 16.sp, color = Color.White),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Hapus", tint = Color.Gray, modifier = Modifier.size(14.dp))
                            }
                        }
                    },
                    modifier = Modifier.weight(1f).height(44.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Category Tabs
        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            items(InventoryCategoryFilter.values()) { cat ->
                val isSelected = selectedCategory == cat
                PixelButton(
                    onClick = { onSelectCategory(cat) },
                    backgroundColor = if (isSelected) Color(0xFF7B1FA2) else Color(0xFF1B152B),
                    testTag = "inv_tab_${cat.key}"
                ) {
                    Text(
                        text = "${cat.icon} ${cat.label}",
                        fontFamily = PressStartFontFamily,
                        fontSize = 6.sp,
                        color = if (isSelected) Color.White else Color(0xFFB0BEC5)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Sort Selector Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("URUTKAN:", fontFamily = PressStartFontFamily, fontSize = 6.sp, color = Color(0xFFB0BEC5))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                items(InventorySortOrder.values()) { order ->
                    val isSelected = selectedSortOrder == order
                    PixelButton(
                        onClick = { onSelectSortOrder(order) },
                        backgroundColor = if (isSelected) Color(0xFFE65100) else Color(0xFF261D2E),
                        testTag = "sort_${order.name}"
                    ) {
                        Text(
                            text = order.label,
                            fontFamily = PressStartFontFamily,
                            fontSize = 5.sp,
                            color = if (isSelected) Color.White else Color(0xFF90A4AE)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryItemsView(
    filteredItems: List<InventoryItemEntity>,
    isGridView: Boolean,
    currentSelected: InventoryItemEntity?,
    onSelectItem: (InventoryItemEntity) -> Unit
) {
    if (filteredItems.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            PixelFrame(
                backgroundColor = Color(0xFF1B162C),
                borderColor = Color(0xFF4A3B69),
                contentPadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📦", fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "KATEGORI INI KOSONG",
                        fontFamily = PressStartFontFamily,
                        fontSize = 9.sp,
                        color = RetroGold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Kumpulkan umpan di toko, benih di kebun, atau buru monster di dungeon!",
                        fontFamily = Vt323FontFamily,
                        fontSize = 15.sp,
                        color = Color(0xFFB0BEC5),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    } else if (isGridView) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 64.dp),
            contentPadding = PaddingValues(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxSize().testTag("inventory_grid_view")
        ) {
            items(filteredItems) { item ->
                val isSelected = currentSelected?.id == item.id
                PixelInventoryGridSlot(
                    item = item,
                    isSelected = isSelected,
                    onClick = { onSelectItem(item) }
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxSize().testTag("inventory_list_view")
        ) {
            items(filteredItems) { item ->
                val isSelected = currentSelected?.id == item.id
                PixelInventoryListItem(
                    item = item,
                    isSelected = isSelected,
                    onClick = { onSelectItem(item) }
                )
            }
        }
    }
}

@Composable
fun PixelInventoryGridSlot(
    item: InventoryItemEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val rarity = try { ItemRarity.valueOf(item.rarity) } catch (_: Exception) { ItemRarity.COMMON }
    val borderColor = if (isSelected) RetroGold else rarity.composeColor().copy(alpha = 0.6f)

    PixelFrame(
        backgroundColor = if (isSelected) Color(0xFF38234A) else Color(0xFF1A142A),
        borderColor = borderColor,
        contentPadding = 4.dp,
        modifier = Modifier
            .size(76.dp)
            .clickable { onClick() }
            .testTag("item_slot_${item.id}")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Center Emoji Icon
            Text(
                text = item.iconEmoji,
                fontSize = 24.sp,
                modifier = Modifier.align(Alignment.Center)
            )

            // Top-left type tag indicator
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .background(Color(0xFF0F0B18), RectangleShape)
                    .padding(horizontal = 2.dp)
            ) {
                Text(
                    text = item.itemType.take(3),
                    fontFamily = PressStartFontFamily,
                    fontSize = 5.sp,
                    color = Color(0xFFB0BEC5)
                )
            }

            // Bottom-right Count Badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(Color(0xFF28183B), RectangleShape)
                    .border(1.dp, Color(0xFF4A3B69), RectangleShape)
                    .padding(horizontal = 3.dp)
            ) {
                Text(
                    text = "x${item.count}",
                    fontFamily = PressStartFontFamily,
                    fontSize = 6.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun PixelInventoryListItem(
    item: InventoryItemEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val rarity = try { ItemRarity.valueOf(item.rarity) } catch (_: Exception) { ItemRarity.COMMON }
    val borderColor = if (isSelected) RetroGold else rarity.composeColor().copy(alpha = 0.4f)

    PixelFrame(
        backgroundColor = if (isSelected) Color(0xFF2F1D40) else Color(0xFF191326),
        borderColor = borderColor,
        contentPadding = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("item_list_row_${item.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(item.iconEmoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontFamily = PressStartFontFamily,
                    fontSize = 8.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RarityChip(rarity = rarity)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tipe: ${item.itemType} • Nilai: ${item.sellPrice}G",
                        fontFamily = Vt323FontFamily,
                        fontSize = 13.sp,
                        color = Color(0xFFB0BEC5)
                    )
                }
            }
            PixelBadge(
                text = "x${item.count}",
                backgroundColor = if (isSelected) Color(0xFFE65100) else Color(0xFF3949AB)
            )
        }
    }
}

@Composable
fun PixelItemDetailInspector(
    item: InventoryItemEntity,
    playerGold: Int,
    onUse: () -> Unit,
    onSellOne: () -> Unit,
    onSellAll: () -> Unit
) {
    val rarity = try { ItemRarity.valueOf(item.rarity) } catch (_: Exception) { ItemRarity.COMMON }
    val isUsable = item.itemType in listOf("POTION", "FOOD", "PET_FOOD", "CHEST") ||
            item.id.contains("potion") || item.id.contains("scroll") ||
            item.id.contains("chest") || item.id.contains("pet_food") ||
            item.id.contains("evo_crystal")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0A0812), RectangleShape)
            .padding(top = 2.dp)
            .background(Color(0xFF5D4037), RectangleShape)
            .padding(top = 2.dp)
            .background(Color(0xFF1B1424), RectangleShape)
            .padding(10.dp)
            .testTag("item_detail_inspector")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(item.iconEmoji, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name.uppercase(),
                        fontFamily = PressStartFontFamily,
                        fontSize = 9.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RarityChip(rarity = rarity)
                        Spacer(modifier = Modifier.width(6.dp))
                        PixelBadge(text = item.itemType, backgroundColor = Color(0xFF4A148C))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Milik: ${item.count} buah",
                            fontFamily = Vt323FontFamily,
                            fontSize = 14.sp,
                            color = RetroGold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isUsable) {
                    PixelButton(
                        onClick = onUse,
                        backgroundColor = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f),
                        testTag = "inspector_use_btn"
                    ) {
                        Text("⚡ PAKAI", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color.White)
                    }
                }

                if (item.sellPrice > 0) {
                    PixelButton(
                        onClick = onSellOne,
                        backgroundColor = Color(0xFFF57F17),
                        modifier = Modifier.weight(1f),
                        testTag = "inspector_sell_one_btn"
                    ) {
                        Text("JUAL 1x (+${item.sellPrice}G)", fontFamily = PressStartFontFamily, fontSize = 6.sp, color = Color.White)
                    }

                    if (item.count > 1) {
                        PixelButton(
                            onClick = onSellAll,
                            backgroundColor = Color(0xFFD84315),
                            modifier = Modifier.weight(1f),
                            testTag = "inspector_sell_all_btn"
                        ) {
                            Text("JUAL SEMUA (+${item.sellPrice * item.count}G)", fontFamily = PressStartFontFamily, fontSize = 6.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
