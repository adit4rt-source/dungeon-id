package com.example.data.repository

import com.example.data.local.GameDao
import com.example.data.local.entity.FarmPlotEntity
import com.example.data.local.entity.FishDexEntity
import com.example.data.local.entity.InventoryItemEntity
import com.example.data.local.entity.PetEntity
import com.example.data.local.entity.PlayerProfileEntity
import com.example.data.local.entity.QuestEntity
import com.example.data.model.FishDef
import com.example.data.model.GameDatabaseRegistry
import com.example.data.model.ItemRarity
import com.example.data.model.ItemType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class GameRepository(private val dao: GameDao) {

    val playerProfile: Flow<PlayerProfileEntity?> = dao.getPlayerProfile()
    val allPlots: Flow<List<FarmPlotEntity>> = dao.getAllPlots()
    val allPets: Flow<List<PetEntity>> = dao.getAllPets()
    val equippedPet: Flow<PetEntity?> = dao.getEquippedPet()
    val inventory: Flow<List<InventoryItemEntity>> = dao.getAllInventory()
    val fishDex: Flow<List<FishDexEntity>> = dao.getAllFishDex()
    val dailyQuests: Flow<List<QuestEntity>> = dao.getAllQuests()

    suspend fun initializeGameIfNeeded() = withContext(Dispatchers.IO) {
        val currentProfile = dao.getPlayerProfileSync()
        if (currentProfile == null) {
            val starterProfile = PlayerProfileEntity(
                id = 1,
                name = "Petualang Muda",
                level = 1,
                exp = 0,
                maxExp = 100,
                hp = 100,
                maxHp = 100,
                energy = 50,
                maxEnergy = 50,
                gold = 350,
                diamonds = 20,
                upgradeStones = 4,
                fishingRodLevel = 1,
                selectedBaitId = "bait_cacing",
                lastEnergyUpdateMillis = System.currentTimeMillis()
            )
            dao.insertOrUpdatePlayer(starterProfile)

            // Initialize 8 farm plots (0..3 unlocked, 4..7 locked)
            val plots = (0 until 8).map { index ->
                FarmPlotEntity(
                    plotIndex = index,
                    isUnlocked = index < 4,
                    cropId = null,
                    plantedAtMillis = 0L,
                    harvestAtMillis = 0L,
                    isWatered = false,
                    isFertilized = false,
                    currentStage = 0
                )
            }
            dao.insertPlots(plots)

            // Starter pet: Serigala Salju
            val starterPet = PetEntity(
                id = 0,
                speciesId = "pet_wolf",
                nickname = "Lupin",
                level = 1,
                exp = 0,
                maxExp = 50,
                evolutionStage = 1,
                hunger = 90,
                happiness = 95,
                isEquipped = true
            )
            dao.insertPet(starterPet)

            // Starter inventory
            val starterItems = listOf(
                InventoryItemEntity("crop_seed_crop_corn", ItemType.SEED.name, "Benih Jagung", 5, "🌽", ItemRarity.COMMON.name, 15),
                InventoryItemEntity("crop_seed_crop_carrot", ItemType.SEED.name, "Benih Wortel", 3, "🥕", ItemRarity.COMMON.name, 28),
                InventoryItemEntity("bait_bait_cacing", ItemType.BAIT.name, "Cacing Tanah", 15, "🪱", ItemRarity.COMMON.name, 5),
                InventoryItemEntity("bait_bait_pelet", ItemType.BAIT.name, "Pelet Harum", 5, "🟤", ItemRarity.UNCOMMON.name, 15),
                InventoryItemEntity("item_potion_hp", ItemType.POTION.name, "Ramuan Darah", 3, "🧪", ItemRarity.UNCOMMON.name, 50),
                InventoryItemEntity("item_fertilizer", ItemType.MATERIAL.name, "Pupuk Organik", 3, "✨", ItemRarity.COMMON.name, 25)
            )
            starterItems.forEach { dao.insertItem(it) }

            // Initialize Fish Dex
            val initialDex = GameDatabaseRegistry.FISHES.map { fish ->
                FishDexEntity(
                    fishId = fish.id,
                    fishName = fish.name,
                    rarity = fish.rarity.name,
                    iconEmoji = fish.iconEmoji,
                    countCaught = 0,
                    maxWeightKg = 0f,
                    isDiscovered = false
                )
            }
            dao.insertAllFishDex(initialDex)

            // Initialize Daily Quests
            val initialQuests = listOf(
                QuestEntity("q_fish_1", "Mancing Ikan Perdana", "Tangkap 3 ekor ikan jenis apapun di danau.", "FISH", 3, 0, false, false, 120, 40, 2),
                QuestEntity("q_farm_1", "Panen Pertama Kebun", "Tanam dan panen 4 hasil pertanian.", "FARM", 4, 0, false, false, 150, 50, 3),
                QuestEntity("q_pet_1", "Kasih Sayang Companion", "Beri makan atau latih pet peliharaanmu 2 kali.", "PET", 2, 0, false, false, 100, 35, 2),
                QuestEntity("q_hunt_1", "Taklukkan Hutan Lumut", "Kalahkan 3 monster di dungeon petualangan.", "DUNGEON", 3, 0, false, false, 200, 70, 5)
            )
            dao.insertQuests(initialQuests)
        } else {
            // Check energy recovery
            checkAndRecoverEnergy(currentProfile)

            // Ensure any new fishes in registry are included in FishDex
            val allDex = GameDatabaseRegistry.FISHES.map { fish ->
                FishDexEntity(
                    fishId = fish.id,
                    fishName = fish.name,
                    rarity = fish.rarity.name,
                    iconEmoji = fish.iconEmoji,
                    countCaught = 0,
                    maxWeightKg = 0f,
                    isDiscovered = false
                )
            }
            dao.insertAllFishDex(allDex)
        }
    }

    private suspend fun checkAndRecoverEnergy(profile: PlayerProfileEntity) {
        val now = System.currentTimeMillis()
        val minutesPassed = ((now - profile.lastEnergyUpdateMillis) / 60000L).toInt()
        if (minutesPassed > 0 && profile.energy < profile.maxEnergy) {
            val newEnergy = min(profile.maxEnergy, profile.energy + minutesPassed)
            dao.insertOrUpdatePlayer(
                profile.copy(
                    energy = newEnergy,
                    lastEnergyUpdateMillis = now
                )
            )
        }
    }

    // --- Player Management ---
    suspend fun addGold(amount: Int) = withContext(Dispatchers.IO) {
        dao.getPlayerProfileSync()?.let {
            dao.insertOrUpdatePlayer(it.copy(gold = max(0, it.gold + amount)))
        }
    }

    suspend fun spendGold(amount: Int): Boolean = withContext(Dispatchers.IO) {
        val profile = dao.getPlayerProfileSync() ?: return@withContext false
        if (profile.gold >= amount) {
            dao.insertOrUpdatePlayer(profile.copy(gold = profile.gold - amount))
            true
        } else {
            false
        }
    }

    suspend fun spendDiamonds(amount: Int): Boolean = withContext(Dispatchers.IO) {
        val profile = dao.getPlayerProfileSync() ?: return@withContext false
        if (profile.diamonds >= amount) {
            dao.insertOrUpdatePlayer(profile.copy(diamonds = profile.diamonds - amount))
            true
        } else {
            false
        }
    }

    suspend fun useEnergy(amount: Int): Boolean = withContext(Dispatchers.IO) {
        val profile = dao.getPlayerProfileSync() ?: return@withContext false
        if (profile.energy >= amount) {
            dao.insertOrUpdatePlayer(
                profile.copy(
                    energy = profile.energy - amount,
                    lastEnergyUpdateMillis = System.currentTimeMillis()
                )
            )
            true
        } else {
            false
        }
    }

    suspend fun restoreEnergy(amount: Int) = withContext(Dispatchers.IO) {
        val profile = dao.getPlayerProfileSync() ?: return@withContext
        val newEnergy = min(profile.maxEnergy, profile.energy + amount)
        dao.insertOrUpdatePlayer(profile.copy(energy = newEnergy))
    }

    suspend fun restoreHp(amount: Int) = withContext(Dispatchers.IO) {
        val profile = dao.getPlayerProfileSync() ?: return@withContext
        val newHp = min(profile.maxHp, profile.hp + amount)
        dao.insertOrUpdatePlayer(profile.copy(hp = newHp))
    }

    suspend fun damagePlayer(amount: Int) = withContext(Dispatchers.IO) {
        val profile = dao.getPlayerProfileSync() ?: return@withContext
        val newHp = max(1, profile.hp - amount)
        dao.insertOrUpdatePlayer(profile.copy(hp = newHp))
    }

    suspend fun addPlayerExp(expGain: Int) = withContext(Dispatchers.IO) {
        val profile = dao.getPlayerProfileSync() ?: return@withContext
        var currentExp = profile.exp + expGain
        var level = profile.level
        var maxExp = profile.maxExp
        var maxHp = profile.maxHp
        var maxEnergy = profile.maxEnergy
        var currentHp = profile.hp
        var currentEnergy = profile.energy

        while (currentExp >= maxExp) {
            currentExp -= maxExp
            level += 1
            maxExp = (maxExp * 1.35f).toInt()
            maxHp += 15
            maxEnergy += 5
            currentHp = maxHp
            currentEnergy = maxEnergy
        }

        dao.insertOrUpdatePlayer(
            profile.copy(
                level = level,
                exp = currentExp,
                maxExp = maxExp,
                hp = currentHp,
                maxHp = maxHp,
                energy = currentEnergy,
                maxEnergy = maxEnergy
            )
        )
    }

    suspend fun upgradeFishingRod(): Result<Int> = withContext(Dispatchers.IO) {
        val profile = dao.getPlayerProfileSync() ?: return@withContext Result.failure(Exception("Profil tidak ditemukan"))
        val currentLevel = profile.fishingRodLevel
        if (currentLevel >= GameDatabaseRegistry.ROD_TIERS.size) {
            return@withContext Result.failure(Exception("Alat pancing sudah mencapai level maksimum!"))
        }
        val nextTier = GameDatabaseRegistry.ROD_TIERS[currentLevel]
        if (profile.gold < nextTier.upgradeCostGold) {
            return@withContext Result.failure(Exception("Koin tidak cukup! Butuh ${nextTier.upgradeCostGold} Gold"))
        }
        if (profile.upgradeStones < nextTier.upgradeCostStones) {
            return@withContext Result.failure(Exception("Batu Peningkat tidak cukup! Butuh ${nextTier.upgradeCostStones} Batu"))
        }

        dao.insertOrUpdatePlayer(
            profile.copy(
                gold = profile.gold - nextTier.upgradeCostGold,
                upgradeStones = profile.upgradeStones - nextTier.upgradeCostStones,
                fishingRodLevel = currentLevel + 1
            )
        )
        Result.success(currentLevel + 1)
    }

    // --- Inventory Management ---
    suspend fun addItem(
        itemId: String,
        itemType: ItemType,
        name: String,
        count: Int,
        iconEmoji: String,
        rarity: ItemRarity,
        sellPrice: Int
    ) = withContext(Dispatchers.IO) {
        val existing = dao.getItemById(itemId)
        if (existing != null) {
            dao.insertItem(existing.copy(count = existing.count + count))
        } else {
            dao.insertItem(
                InventoryItemEntity(
                    id = itemId,
                    itemType = itemType.name,
                    name = name,
                    count = count,
                    iconEmoji = iconEmoji,
                    rarity = rarity.name,
                    sellPrice = sellPrice
                )
            )
        }
    }

    suspend fun removeItem(itemId: String, count: Int): Boolean = withContext(Dispatchers.IO) {
        val existing = dao.getItemById(itemId) ?: return@withContext false
        if (existing.count > count) {
            dao.insertItem(existing.copy(count = existing.count - count))
            true
        } else if (existing.count == count) {
            dao.deleteItemById(itemId)
            true
        } else {
            false
        }
    }

    suspend fun getItemCount(itemId: String): Int = withContext(Dispatchers.IO) {
        dao.getItemById(itemId)?.count ?: 0
    }

    suspend fun useConsumableItem(item: InventoryItemEntity): Result<String> = withContext(Dispatchers.IO) {
        val itemId = item.id
        when {
            itemId == "item_potion_hp" -> {
                if (!removeItem(itemId, 1)) return@withContext Result.failure(Exception("Item tidak ditemukan"))
                restoreHp(70)
                Result.success("🧪 Meminum Ramuan Darah! Pulih +70 HP.")
            }
            itemId == "item_potion_hp_large" -> {
                if (!removeItem(itemId, 1)) return@withContext Result.failure(Exception("Item tidak ditemukan"))
                restoreHp(200)
                Result.success("🍷 Meminum Elixir Darah Murni! Pulih +200 HP.")
            }
            itemId == "item_potion_stamina" -> {
                if (!removeItem(itemId, 1)) return@withContext Result.failure(Exception("Item tidak ditemukan"))
                restoreEnergy(25)
                Result.success("⚡ Meminum Ramuan Stamina! Pulih +25 Stamina.")
            }
            itemId == "item_scroll_exp" -> {
                if (!removeItem(itemId, 1)) return@withContext Result.failure(Exception("Item tidak ditemukan"))
                addPlayerExp(250)
                Result.success("📜 Membaca Gulungan Berkah EXP! Mendapatkan +250 EXP!")
            }
            itemId == "item_dungeon_ticket" -> {
                if (!removeItem(itemId, 1)) return@withContext Result.failure(Exception("Item tidak ditemukan"))
                restoreEnergy(30)
                Result.success("🎟️ Menggunakan Tiket Dungeon! Pulih +30 Stamina petualangan!")
            }
            itemId == "pet_food_kibble" || itemId == "pet_food_jerky" || itemId == "pet_food_nectar" -> {
                val pet = dao.getEquippedPetSync() ?: return@withContext Result.failure(Exception("Pasang pet peliharaan terlebih dahulu!"))
                if (!removeItem(itemId, 1)) return@withContext Result.failure(Exception("Item habis"))
                val hungerBoost = when (itemId) {
                    "pet_food_kibble" -> 35
                    "pet_food_jerky" -> 60
                    else -> 100
                }
                val expBoost = when (itemId) {
                    "pet_food_kibble" -> 30
                    "pet_food_jerky" -> 70
                    else -> 160
                }
                feedPet(pet.id, itemId, hungerBoost, expBoost)
                Result.success("🍖 Memberi makan ${pet.nickname}! (+${hungerBoost}% Kenyang & +${expBoost} EXP Pet)")
            }
            itemId == "item_pet_potion_energy" -> {
                val pet = dao.getEquippedPetSync() ?: return@withContext Result.failure(Exception("Pasang pet peliharaan terlebih dahulu!"))
                if (!removeItem(itemId, 1)) return@withContext Result.failure(Exception("Item habis"))
                dao.updatePet(pet.copy(hunger = 100, happiness = 100, exp = pet.exp + 50))
                Result.success("⚡ Kebugaran dan kegembiraan ${pet.nickname} pulih 100%!")
            }
            itemId == "item_pet_evo_crystal" -> {
                val pet = dao.getEquippedPetSync() ?: return@withContext Result.failure(Exception("Pasang pet peliharaan terlebih dahulu!"))
                if (!removeItem(itemId, 1)) return@withContext Result.failure(Exception("Item habis"))
                dao.updatePet(pet.copy(exp = pet.exp + 300, happiness = 100))
                Result.success("🔮 Menyalurkan Kristal Jiwa ke ${pet.nickname}! Mendapatkan +300 EXP Pet!")
            }
            itemId == "item_pet_awakening_stone" -> {
                val pet = dao.getEquippedPetSync() ?: return@withContext Result.failure(Exception("Pasang pet peliharaan terlebih dahulu!"))
                if (!removeItem(itemId, 1)) return@withContext Result.failure(Exception("Item habis"))
                dao.updatePet(pet.copy(exp = pet.exp + 800, happiness = 100))
                Result.success("💎 Menyalurkan Batu Kebangkitan ke ${pet.nickname}! Mendapatkan +800 EXP Pet!")
            }
            itemId == "item_chest_wooden" -> {
                val keyCount = getItemCount("item_key_wooden")
                if (keyCount <= 0) return@withContext Result.failure(Exception("Butuh 1 Kunci Kayu (🗝️) untuk membuka peti ini!"))
                removeItem("item_chest_wooden", 1)
                removeItem("item_key_wooden", 1)
                val goldReward = kotlin.random.Random.nextInt(150, 320)
                addGold(goldReward)
                addPlayerExp(25)
                addItem("crop_seed_crop_carrot", ItemType.SEED, "Benih Wortel", 2, "🥕", ItemRarity.COMMON, 28)
                Result.success("📦 Peti Kayu Terbuka! Mendapatkan +$goldReward Gold, 2 Benih Wortel & +25 EXP!")
            }
            itemId == "item_chest_iron" -> {
                val keyCount = getItemCount("item_key_iron")
                if (keyCount <= 0) return@withContext Result.failure(Exception("Butuh 1 Kunci Besi (🔑) untuk membuka peti ini!"))
                removeItem("item_chest_iron", 1)
                removeItem("item_key_iron", 1)
                val goldReward = kotlin.random.Random.nextInt(400, 750)
                addGold(goldReward)
                addPlayerExp(50)
                addItem("item_upgrade_stone", ItemType.MATERIAL, "Batu Peningkat Tempa", 1, "🪨", ItemRarity.RARE, 100)
                addItem("item_fertilizer", ItemType.MATERIAL, "Pupuk Organik", 2, "✨", ItemRarity.COMMON, 25)
                Result.success("🧰 Peti Besi Terbuka! Mendapatkan +$goldReward Gold, 1 Batu Tempa & 2 Pupuk!")
            }
            itemId == "item_chest_gold" -> {
                val keyCount = getItemCount("item_key_gold")
                if (keyCount <= 0) return@withContext Result.failure(Exception("Butuh 1 Kunci Emas (✨🔑) untuk membuka peti ini!"))
                removeItem("item_chest_gold", 1)
                removeItem("item_key_gold", 1)
                val goldReward = kotlin.random.Random.nextInt(1500, 2500)
                addGold(goldReward)
                addPlayerExp(150)
                val profile = dao.getPlayerProfileSync()
                if (profile != null) {
                    dao.insertOrUpdatePlayer(profile.copy(diamonds = profile.diamonds + 15, upgradeStones = profile.upgradeStones + 3))
                }
                addItem("item_gem_ruby", ItemType.MATERIAL, "Permata Rubi Membara", 1, "🔻", ItemRarity.EPIC, 750)
                Result.success("🎁 Peti Emas Terbuka! Hadiah Megah: +$goldReward Gold, +15 Diamond, 3 Batu Tempa & 1 Permata Rubi!")
            }
            item.itemType == "FOOD" -> {
                if (!removeItem(itemId, 1)) return@withContext Result.failure(Exception("Item tidak ditemukan"))
                restoreEnergy(35)
                restoreHp(60)
                Result.success("🍲 Menikmati hidangan ${item.name}! Pulih +35 Stamina & +60 HP.")
            }
            else -> {
                Result.failure(Exception("Item ini tidak dapat digunakan langsung."))
            }
        }
    }

    suspend fun craftBlacksmithRecipe(recipeKey: String): Result<String> = withContext(Dispatchers.IO) {
        when (recipeKey) {
            "forge_stone" -> {
                val ironCount = getItemCount("item_iron_ore")
                if (ironCount < 3) return@withContext Result.failure(Exception("Butuh 3 Bijih Besi Padat (⚙️)"))
                if (!spendGold(50)) return@withContext Result.failure(Exception("Biaya tempa butuh 50 Gold"))
                removeItem("item_iron_ore", 3)
                addItem("item_upgrade_stone", ItemType.MATERIAL, "Batu Peningkat Tempa", 1, "🪨", ItemRarity.RARE, 100)
                addPlayerExp(20)
                val profile = dao.getPlayerProfileSync()
                if (profile != null) {
                    dao.insertOrUpdatePlayer(profile.copy(upgradeStones = profile.upgradeStones + 1))
                }
                Result.success("🔨 Berhasil menempa 1 Batu Peningkat Tempa!")
            }
            "forge_gold_ingot" -> {
                val stoneCount = getItemCount("item_upgrade_stone")
                if (stoneCount < 2) return@withContext Result.failure(Exception("Butuh 2 Batu Peningkat Tempa (🪨)"))
                if (!spendGold(100)) return@withContext Result.failure(Exception("Biaya tempa butuh 100 Gold"))
                removeItem("item_upgrade_stone", 2)
                addItem("item_gold_ingot", ItemType.MATERIAL, "Batangan Emas Murni", 1, "🪙", ItemRarity.RARE, 300)
                addPlayerExp(40)
                Result.success("🔨 Berhasil melebur 1 Batangan Emas Murni!")
            }
            "forge_dragon_armor" -> {
                val scaleCount = getItemCount("item_dragon_scale")
                val stoneCount = getItemCount("item_upgrade_stone")
                if (scaleCount < 1 || stoneCount < 2) return@withContext Result.failure(Exception("Butuh 1 Sisik Naga & 2 Batu Tempa"))
                if (!spendGold(250)) return@withContext Result.failure(Exception("Biaya tempa butuh 250 Gold"))
                removeItem("item_dragon_scale", 1)
                removeItem("item_upgrade_stone", 2)
                addItem("item_dragon_armor", ItemType.MATERIAL, "Zirah Sisik Naga Purba", 1, "🛡️", ItemRarity.LEGENDARY, 1600)
                addPlayerExp(100)
                Result.success("🛡️ Berhasil menempa Zirah Sisik Naga Purba legendaris!")
            }
            else -> Result.failure(Exception("Resep tidak ditemukan"))
        }
    }

    // --- Farming Management ---
    suspend fun unlockPlot(plotIndex: Int, cost: Int): Boolean = withContext(Dispatchers.IO) {
        if (!spendGold(cost)) return@withContext false
        val plots = dao.getPlotsSync()
        val target = plots.find { it.plotIndex == plotIndex } ?: return@withContext false
        dao.updatePlot(target.copy(isUnlocked = true))
        true
    }

    suspend fun plantSeed(plotIndex: Int, cropId: String): Boolean = withContext(Dispatchers.IO) {
        val cropDef = GameDatabaseRegistry.CROPS.find { it.id == cropId } ?: return@withContext false
        val seedItemId = "crop_seed_$cropId"
        if (!removeItem(seedItemId, 1)) return@withContext false

        val plots = dao.getPlotsSync()
        val target = plots.find { it.plotIndex == plotIndex } ?: return@withContext false
        val now = System.currentTimeMillis()
        val durationMillis = cropDef.growDurationSeconds * 1000L

        dao.updatePlot(
            target.copy(
                cropId = cropId,
                plantedAtMillis = now,
                harvestAtMillis = now + durationMillis,
                isWatered = false,
                isFertilized = false,
                currentStage = 1
            )
        )
        true
    }

    suspend fun waterPlot(plotIndex: Int): Boolean = withContext(Dispatchers.IO) {
        val plots = dao.getPlotsSync()
        val target = plots.find { it.plotIndex == plotIndex } ?: return@withContext false
        if (target.cropId == null || target.isWatered) return@withContext false

        // Watering reduces remaining time by 20%
        val now = System.currentTimeMillis()
        val remaining = max(0L, target.harvestAtMillis - now)
        val acceleratedHarvest = now + (remaining * 0.8f).toLong()

        dao.updatePlot(
            target.copy(
                isWatered = true,
                harvestAtMillis = acceleratedHarvest
            )
        )
        true
    }

    suspend fun fertilizePlot(plotIndex: Int): Boolean = withContext(Dispatchers.IO) {
        val plots = dao.getPlotsSync()
        val target = plots.find { it.plotIndex == plotIndex } ?: return@withContext false
        if (target.cropId == null || target.isFertilized) return@withContext false

        if (!removeItem("item_fertilizer", 1)) return@withContext false

        val now = System.currentTimeMillis()
        val remaining = max(0L, target.harvestAtMillis - now)
        val acceleratedHarvest = now + (remaining * 0.7f).toLong()

        dao.updatePlot(
            target.copy(
                isFertilized = true,
                harvestAtMillis = acceleratedHarvest
            )
        )
        true
    }

    suspend fun harvestPlot(plotIndex: Int): Result<String> = withContext(Dispatchers.IO) {
        val plots = dao.getPlotsSync()
        val target = plots.find { it.plotIndex == plotIndex } ?: return@withContext Result.failure(Exception("Petak tidak ada"))
        val cropId = target.cropId ?: return@withContext Result.failure(Exception("Tidak ada tanaman"))
        val cropDef = GameDatabaseRegistry.CROPS.find { it.id == cropId } ?: return@withContext Result.failure(Exception("Tanaman tidak dikenal"))

        val now = System.currentTimeMillis()
        if (now < target.harvestAtMillis) {
            return@withContext Result.failure(Exception("Tanaman belum matang untuk dipanen!"))
        }

        // Calculate yield: 1-2 crops, double if fertilized
        val baseYield = Random.nextInt(1, 3)
        val totalYield = if (target.isFertilized) baseYield * 2 else baseYield

        // Add to inventory
        addItem(
            itemId = "item_crop_${cropDef.id}",
            itemType = ItemType.CROP,
            name = cropDef.name,
            count = totalYield,
            iconEmoji = cropDef.iconEmoji,
            rarity = cropDef.rarity,
            sellPrice = cropDef.sellPrice
        )

        // Seed drop bonus chance
        if (Random.nextFloat() < 0.35f) {
            addItem(
                itemId = "crop_seed_${cropDef.id}",
                itemType = ItemType.SEED,
                name = cropDef.seedName,
                count = 1,
                iconEmoji = cropDef.iconEmoji,
                rarity = cropDef.rarity,
                sellPrice = cropDef.seedCost / 2
            )
        }

        // Add player EXP
        addPlayerExp(cropDef.expReward)

        // Reset plot
        dao.updatePlot(
            target.copy(
                cropId = null,
                plantedAtMillis = 0L,
                harvestAtMillis = 0L,
                isWatered = false,
                isFertilized = false,
                currentStage = 0
            )
        )

        // Progress quest
        incrementQuestProgress("FARM", totalYield)

        Result.success("Panen berhasil! Mendapatkan $totalYield ${cropDef.name} +${cropDef.expReward} EXP")
    }

    // --- Fishing Management ---
    suspend fun recordCatchFish(fish: FishDef, weightKg: Float): Int = withContext(Dispatchers.IO) {
        // Value adjusted for weight
        val weightMultiplier = (weightKg / fish.minWeightKg).coerceIn(1.0f, 2.5f)
        val finalPrice = (fish.basePrice * weightMultiplier).toInt()

        // Add to inventory
        addItem(
            itemId = "item_fish_${fish.id}",
            itemType = ItemType.FISH,
            name = "${fish.name} (${String.format("%.1f", weightKg)}kg)",
            count = 1,
            iconEmoji = fish.iconEmoji,
            rarity = fish.rarity,
            sellPrice = finalPrice
        )

        // Update Fish Dex
        val existingDex = dao.getFishDexById(fish.id)
        if (existingDex != null) {
            dao.insertOrUpdateFishDex(
                existingDex.copy(
                    countCaught = existingDex.countCaught + 1,
                    maxWeightKg = max(existingDex.maxWeightKg, weightKg),
                    isDiscovered = true
                )
            )
        } else {
            dao.insertOrUpdateFishDex(
                FishDexEntity(
                    fishId = fish.id,
                    fishName = fish.name,
                    rarity = fish.rarity.name,
                    iconEmoji = fish.iconEmoji,
                    countCaught = 1,
                    maxWeightKg = weightKg,
                    isDiscovered = true
                )
            )
        }

        // Add EXP
        addPlayerExp(fish.expReward)

        // Increment quest
        incrementQuestProgress("FISH", 1)

        finalPrice
    }

    // --- Pet Management ---
    suspend fun feedPet(
        petId: Int,
        foodItemId: String,
        customHungerBoost: Int = 30,
        customExpBoost: Int = 15
    ): Result<String> = withContext(Dispatchers.IO) {
        val pet = dao.getPetById(petId) ?: return@withContext Result.failure(Exception("Pet tidak ditemukan"))
        if (!removeItem(foodItemId, 1)) return@withContext Result.failure(Exception("Bahan makanan habis"))

        val newHunger = min(100, pet.hunger + customHungerBoost)
        val newHappiness = min(100, pet.happiness + 15)
        val expGain = customExpBoost

        var currentExp = pet.exp + expGain
        var level = pet.level
        var maxExp = pet.maxExp
        var stage = pet.evolutionStage

        while (currentExp >= maxExp) {
            currentExp -= maxExp
            level += 1
            maxExp = (maxExp * 1.3f).toInt()
        }

        // Evolution check: Level 10 -> Stage 2, Level 25 -> Stage 3
        if (level >= 25 && stage < 3) {
            stage = 3
        } else if (level >= 10 && stage < 2) {
            stage = 2
        }

        dao.updatePet(
            pet.copy(
                hunger = newHunger,
                happiness = newHappiness,
                exp = currentExp,
                level = level,
                maxExp = maxExp,
                evolutionStage = stage
            )
        )

        incrementQuestProgress("PET", 1)
        Result.success("Pet merasa kenyang & senang! +$expGain Pet EXP")
    }

    suspend fun trainPet(petId: Int): Result<String> = withContext(Dispatchers.IO) {
        val pet = dao.getPetById(petId) ?: return@withContext Result.failure(Exception("Pet tidak ditemukan"))
        if (pet.hunger < 15) {
            return@withContext Result.failure(Exception("Pet terlalu lapar untuk berlatih! Beri makan terlebih dahulu."))
        }
        if (!useEnergy(5)) {
            return@withContext Result.failure(Exception("Stamina pemain tidak cukup (butuh 5)"))
        }

        val newHunger = max(0, pet.hunger - 15)
        val newHappiness = min(100, pet.happiness + 20)
        val expGain = 35

        var currentExp = pet.exp + expGain
        var level = pet.level
        var maxExp = pet.maxExp
        var stage = pet.evolutionStage

        while (currentExp >= maxExp) {
            currentExp -= maxExp
            level += 1
            maxExp = (maxExp * 1.3f).toInt()
        }

        if (level >= 25 && stage < 3) {
            stage = 3
        } else if (level >= 10 && stage < 2) {
            stage = 2
        }

        dao.updatePet(
            pet.copy(
                hunger = newHunger,
                happiness = newHappiness,
                exp = currentExp,
                level = level,
                maxExp = maxExp,
                evolutionStage = stage
            )
        )

        incrementQuestProgress("PET", 1)
        Result.success("Latihan selesai! Pet semakin kuat. +$expGain Pet EXP")
    }

    suspend fun evolvePetManual(petId: Int): Result<String> = withContext(Dispatchers.IO) {
        val pet = dao.getPetById(petId) ?: return@withContext Result.failure(Exception("Pet tidak ditemukan"))
        val profile = dao.getPlayerProfileSync() ?: return@withContext Result.failure(Exception("Profil kosong"))

        val requiredLevel = if (pet.evolutionStage == 1) 10 else 25
        if (pet.level < requiredLevel) {
            return@withContext Result.failure(Exception("Pet harus mencapai Level $requiredLevel untuk berevolusi!"))
        }
        if (pet.evolutionStage >= 3) {
            return@withContext Result.failure(Exception("Pet sudah mencapai evolusi puncak!"))
        }

        val requiredStones = if (pet.evolutionStage == 1) 2 else 5
        if (profile.upgradeStones < requiredStones) {
            return@withContext Result.failure(Exception("Butuh $requiredStones Batu Peningkat untuk evolusi!"))
        }

        dao.insertOrUpdatePlayer(profile.copy(upgradeStones = profile.upgradeStones - requiredStones))
        val nextStage = pet.evolutionStage + 1
        dao.updatePet(pet.copy(evolutionStage = nextStage))

        Result.success("✨ Selamat! Pet berhasil berevolusi ke Stage $nextStage!")
    }

    suspend fun equipPet(petId: Int) = withContext(Dispatchers.IO) {
        dao.clearAllEquipped()
        dao.setEquippedPet(petId)
    }

    suspend fun adoptPet(speciesId: String, nickname: String, costGold: Int): Boolean = withContext(Dispatchers.IO) {
        if (!spendGold(costGold)) return@withContext false
        val newPet = PetEntity(
            id = 0,
            speciesId = speciesId,
            nickname = nickname,
            level = 1,
            exp = 0,
            maxExp = 50,
            evolutionStage = 1,
            hunger = 80,
            happiness = 90,
            isEquipped = false
        )
        dao.insertPet(newPet)
        true
    }

    // --- Quests ---
    suspend fun incrementQuestProgress(type: String, amount: Int) = withContext(Dispatchers.IO) {
        val quests = dao.getQuestsSync()
        for (q in quests) {
            if (q.questType == type && !q.isCompleted) {
                val newCount = min(q.targetCount, q.currentCount + amount)
                val completed = newCount >= q.targetCount
                dao.updateQuest(
                    q.copy(
                        currentCount = newCount,
                        isCompleted = completed
                    )
                )
            }
        }
    }

    suspend fun claimQuest(questId: String): Boolean = withContext(Dispatchers.IO) {
        val quests = dao.getQuestsSync()
        val target = quests.find { it.id == questId } ?: return@withContext false
        if (!target.isCompleted || target.isClaimed) return@withContext false

        addGold(target.rewardGold)
        addPlayerExp(target.rewardExp)
        if (target.rewardDiamonds > 0) {
            val p = dao.getPlayerProfileSync()
            if (p != null) {
                dao.insertOrUpdatePlayer(p.copy(diamonds = p.diamonds + target.rewardDiamonds))
            }
        }

        dao.updateQuest(target.copy(isClaimed = true))
        true
    }
}
